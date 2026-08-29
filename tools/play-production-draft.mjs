import { createSign } from 'node:crypto';

const API_ROOT = 'https://androidpublisher.googleapis.com/androidpublisher/v3';
const TOKEN_URL = 'https://oauth2.googleapis.com/token';
const PUBLISHER_SCOPE = 'https://www.googleapis.com/auth/androidpublisher';

function base64Url(value) {
  return Buffer.from(value)
    .toString('base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
}

export function parsePositiveVersionCode(value) {
  if (!/^\d+$/.test(String(value))) {
    throw new Error(`Version code '${value}' is not a positive integer.`);
  }
  const parsed = Number(value);
  if (!Number.isSafeInteger(parsed) || parsed <= 0) {
    throw new Error(`Version code '${value}' is not a positive integer.`);
  }
  return String(parsed);
}

export function highestBundleVersionCode(bundles = []) {
  if (!Array.isArray(bundles) || bundles.length === 0) return null;
  return bundles.reduce((max, bundle) => {
    const code = Number(bundle.versionCode);
    return Number.isSafeInteger(code) && code > max ? code : max;
  }, 0) || null;
}

export function selectSourceRelease(track, versionCode) {
  const code = parsePositiveVersionCode(versionCode);
  const releases = Array.isArray(track?.releases) ? track.releases : [];
  const matches = releases.filter((release) =>
    Array.isArray(release.versionCodes) && release.versionCodes.map(String).includes(code),
  );

  if (matches.length !== 1) {
    throw new Error(
      `Expected exactly one release containing versionCode ${code} on '${track?.track ?? 'source'}', found ${matches.length}.`,
    );
  }

  const [release] = matches;
  if (release.status !== 'completed') {
    throw new Error(
      `Source release for versionCode ${code} must be completed before production promotion; found '${release.status ?? 'unspecified'}'.`,
    );
  }

  return release;
}

export function buildProductionTrack(existingTrack, sourceRelease, versionCode, releaseName) {
  const code = parsePositiveVersionCode(versionCode);
  const releases = Array.isArray(existingTrack?.releases) ? existingTrack.releases : [];

  if (releases.some((release) => Array.isArray(release.versionCodes) && release.versionCodes.map(String).includes(code))) {
    throw new Error(`versionCode ${code} is already present on the production track.`);
  }

  const blocking = releases.filter((release) => release.status && release.status !== 'completed');
  if (blocking.length > 0) {
    const states = blocking.map((release) => `${release.name ?? 'unnamed'}:${release.status}`).join(', ');
    throw new Error(`Production already has a non-completed release (${states}). Resolve it in Play Console first.`);
  }

  const draft = {
    name: releaseName,
    versionCodes: [code],
    status: 'draft',
  };

  if (Array.isArray(sourceRelease?.releaseNotes) && sourceRelease.releaseNotes.length > 0) {
    draft.releaseNotes = sourceRelease.releaseNotes;
  }
  if (Number.isInteger(sourceRelease?.inAppUpdatePriority)) {
    draft.inAppUpdatePriority = sourceRelease.inAppUpdatePriority;
  }

  return {
    track: 'production',
    releases: [...releases, draft],
  };
}

export function createServiceAccountAssertion(serviceAccount, nowSeconds = Math.floor(Date.now() / 1000)) {
  if (!serviceAccount?.client_email || !serviceAccount?.private_key) {
    throw new Error('PLAY_SERVICE_ACCOUNT_JSON must contain client_email and private_key.');
  }

  const header = base64Url(JSON.stringify({ alg: 'RS256', typ: 'JWT' }));
  const payload = base64Url(
    JSON.stringify({
      iss: serviceAccount.client_email,
      scope: PUBLISHER_SCOPE,
      aud: TOKEN_URL,
      exp: nowSeconds + 3600,
      iat: nowSeconds,
    }),
  );
  const unsigned = `${header}.${payload}`;
  const signer = createSign('RSA-SHA256');
  signer.update(unsigned);
  signer.end();
  const signature = signer
    .sign(serviceAccount.private_key)
    .toString('base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
  return `${unsigned}.${signature}`;
}

async function responseJson(response, description) {
  const text = await response.text();
  let body;
  try {
    body = text ? JSON.parse(text) : {};
  } catch {
    body = { raw: text };
  }
  if (!response.ok) {
    const detail = body?.error?.message ?? body?.raw ?? `HTTP ${response.status}`;
    throw new Error(`${description} failed (${response.status}): ${detail}`);
  }
  return body;
}

async function accessToken(serviceAccount, fetchImpl = fetch) {
  const assertion = createServiceAccountAssertion(serviceAccount);
  const response = await fetchImpl(TOKEN_URL, {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion,
    }),
  });
  const body = await responseJson(response, 'OAuth token exchange');
  if (!body.access_token) throw new Error('OAuth token exchange returned no access_token.');
  return body.access_token;
}

function encoded(value) {
  return encodeURIComponent(value);
}

async function publisherRequest(token, path, { method = 'GET', body, fetchImpl = fetch } = {}) {
  const response = await fetchImpl(`${API_ROOT}${path}`, {
    method,
    headers: {
      authorization: `Bearer ${token}`,
      ...(body ? { 'content-type': 'application/json' } : {}),
    },
    ...(body ? { body: JSON.stringify(body) } : {}),
  });
  return responseJson(response, `${method} ${path}`);
}

export async function inspectAndMaybeCreateDraft({
  serviceAccount,
  packageName,
  sourceTrack,
  versionCode,
  releaseName,
  createDraft,
  fetchImpl = fetch,
}) {
  const code = parsePositiveVersionCode(versionCode);
  const token = await accessToken(serviceAccount, fetchImpl);
  const appPath = `/applications/${encoded(packageName)}`;
  let editId;
  let committed = false;

  try {
    const edit = await publisherRequest(token, `${appPath}/edits`, {
      method: 'POST',
      fetchImpl,
    });
    editId = edit.id;
    if (!editId) throw new Error('Android Publisher API returned an edit without an id.');

    const [bundles, source] = await Promise.all([
      publisherRequest(token, `${appPath}/edits/${encoded(editId)}/bundles`, { fetchImpl }),
      publisherRequest(token, `${appPath}/edits/${encoded(editId)}/tracks/${encoded(sourceTrack)}`, { fetchImpl }),
    ]);

    const highest = highestBundleVersionCode(bundles.bundles ?? []);
    if (highest !== null && Number(code) < highest) {
      throw new Error(`Requested versionCode ${code} is lower than Play's highest uploaded bundle versionCode ${highest}.`);
    }
    if (!(bundles.bundles ?? []).some((bundle) => String(bundle.versionCode) === code)) {
      throw new Error(`versionCode ${code} is not present in Play's current app bundles.`);
    }

    const sourceRelease = selectSourceRelease(source, code);
    const summary = {
      packageName,
      sourceTrack,
      versionCode: code,
      highestUploadedVersionCode: highest,
      sourceReleaseName: sourceRelease.name ?? null,
      sourceReleaseStatus: sourceRelease.status,
      mode: createDraft ? 'create-draft' : 'check',
    };

    if (!createDraft) return summary;

    const production = await publisherRequest(
      token,
      `${appPath}/edits/${encoded(editId)}/tracks/production`,
      { fetchImpl },
    );
    const productionTrack = buildProductionTrack(production, sourceRelease, code, releaseName);
    summary.productionExistingReleases = Array.isArray(production.releases) ? production.releases.length : 0;

    await publisherRequest(token, `${appPath}/edits/${encoded(editId)}/tracks/production`, {
      method: 'PUT',
      body: productionTrack,
      fetchImpl,
    });
    await publisherRequest(token, `${appPath}/edits/${encoded(editId)}:validate`, {
      method: 'POST',
      fetchImpl,
    });
    await publisherRequest(token, `${appPath}/edits/${encoded(editId)}:commit`, {
      method: 'POST',
      fetchImpl,
    });
    committed = true;
    return { ...summary, productionDraftCreated: true };
  } finally {
    if (editId && !committed) {
      try {
        await publisherRequest(token, `${appPath}/edits/${encoded(editId)}`, {
          method: 'DELETE',
          fetchImpl,
        });
      } catch (error) {
        console.error(`Warning: failed to delete temporary Play edit ${editId}: ${error.message}`);
      }
    }
  }
}

async function main() {
  const mode = process.env.PLAY_PRODUCTION_MODE ?? 'check';
  if (!['check', 'create-draft'].includes(mode)) {
    throw new Error(`Unsupported PLAY_PRODUCTION_MODE '${mode}'.`);
  }
  const serviceAccount = JSON.parse(process.env.PLAY_SERVICE_ACCOUNT_JSON ?? '{}');
  const result = await inspectAndMaybeCreateDraft({
    serviceAccount,
    packageName: process.env.PLAY_PACKAGE_NAME ?? 'dev.danielkindl.ocho',
    sourceTrack: process.env.PLAY_SOURCE_TRACK,
    versionCode: process.env.PLAY_VERSION_CODE,
    releaseName: process.env.PLAY_RELEASE_NAME,
    createDraft: mode === 'create-draft',
  });

  const output = JSON.stringify(result, null, 2);
  console.log(output);
  if (process.env.GITHUB_STEP_SUMMARY) {
    const fs = await import('node:fs/promises');
    await fs.appendFile(
      process.env.GITHUB_STEP_SUMMARY,
      `## Google Play production gate\n\n\`\`\`json\n${output}\n\`\`\`\n`,
    );
  }
}

if (import.meta.url === `file://${process.argv[1]}`) {
  main().catch((error) => {
    console.error(`::error::${error.message}`);
    process.exitCode = 1;
  });
}
