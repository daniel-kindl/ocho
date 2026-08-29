import test from 'node:test';
import assert from 'node:assert/strict';
import { generateKeyPairSync, verify } from 'node:crypto';
import {
  buildProductionTrack,
  createServiceAccountAssertion,
  highestBundleVersionCode,
  parsePositiveVersionCode,
  selectSourceRelease,
} from './play-production-draft.mjs';

test('parsePositiveVersionCode accepts positive integers', () => {
  assert.equal(parsePositiveVersionCode('17'), '17');
  assert.throws(() => parsePositiveVersionCode('0'));
  assert.throws(() => parsePositiveVersionCode('17.1'));
  assert.throws(() => parsePositiveVersionCode('abc'));
});

test('createServiceAccountAssertion creates a signed Android Publisher JWT', () => {
  const { privateKey, publicKey } = generateKeyPairSync('rsa', { modulusLength: 2048 });
  const privatePem = privateKey.export({ type: 'pkcs8', format: 'pem' });
  const assertion = createServiceAccountAssertion(
    { client_email: 'play@example.iam.gserviceaccount.com', private_key: privatePem },
    1_700_000_000,
  );
  const [header, payload, signature] = assertion.split('.');
  const decodedPayload = JSON.parse(Buffer.from(payload, 'base64url').toString('utf8'));

  assert.equal(decodedPayload.iss, 'play@example.iam.gserviceaccount.com');
  assert.equal(decodedPayload.scope, 'https://www.googleapis.com/auth/androidpublisher');
  assert.equal(decodedPayload.aud, 'https://oauth2.googleapis.com/token');
  assert.equal(decodedPayload.iat, 1_700_000_000);
  assert.equal(decodedPayload.exp, 1_700_003_600);
  assert.equal(
    verify('RSA-SHA256', Buffer.from(`${header}.${payload}`), publicKey, Buffer.from(signature, 'base64url')),
    true,
  );
});

test('highestBundleVersionCode returns the greatest Play bundle code', () => {
  assert.equal(highestBundleVersionCode([{ versionCode: 16 }, { versionCode: 19 }, { versionCode: 17 }]), 19);
  assert.equal(highestBundleVersionCode([]), null);
});

test('selectSourceRelease requires exactly one completed closed-test release', () => {
  const release = selectSourceRelease(
    {
      track: 'alpha',
      releases: [{ name: 'candidate', versionCodes: ['17'], status: 'completed' }],
    },
    '17',
  );
  assert.equal(release.name, 'candidate');

  assert.throws(() =>
    selectSourceRelease({ track: 'alpha', releases: [{ versionCodes: ['17'], status: 'draft' }] }, '17'),
  );
  assert.throws(() => selectSourceRelease({ track: 'alpha', releases: [] }, '17'));
});

test('buildProductionTrack appends a draft and preserves completed production releases', () => {
  const result = buildProductionTrack(
    {
      track: 'production',
      releases: [{ name: '3.7.0', versionCodes: ['16'], status: 'completed' }],
    },
    {
      name: 'candidate',
      versionCodes: ['17'],
      status: 'completed',
      releaseNotes: [{ language: 'en-US', text: 'Hardening release' }],
      inAppUpdatePriority: 0,
    },
    '17',
    'v3.7.1',
  );

  assert.equal(result.releases.length, 2);
  assert.deepEqual(result.releases[1], {
    name: 'v3.7.1',
    versionCodes: ['17'],
    status: 'draft',
    releaseNotes: [{ language: 'en-US', text: 'Hardening release' }],
    inAppUpdatePriority: 0,
  });
});

test('buildProductionTrack refuses duplicate or unfinished production state', () => {
  const source = { versionCodes: ['17'], status: 'completed' };
  assert.throws(() =>
    buildProductionTrack(
      { track: 'production', releases: [{ versionCodes: ['17'], status: 'completed' }] },
      source,
      '17',
      'v3.7.1',
    ),
  );
  assert.throws(() =>
    buildProductionTrack(
      { track: 'production', releases: [{ name: 'pending', versionCodes: ['16'], status: 'draft' }] },
      source,
      '17',
      'v3.7.1',
    ),
  );
});
