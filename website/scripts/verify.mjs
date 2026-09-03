import { existsSync, readdirSync, readFileSync } from 'node:fs';
import { join, relative } from 'node:path';

const dist = join(process.cwd(), 'dist');
const siteOrigin = 'https://daniel-kindl.github.io';
const basePath = '/ocho';
const socialImage = 'social/ocho-social-1200x630.png';
const expectedPages = [
  'index.html',
  'privacy-policy.html',
  'screenshots/mockup-export.html',
];
const trackingPattern = /(?:google-analytics|googletagmanager|gtag\s*\(|plausible\.io|matomo|umami|hotjar|segment\.com|mixpanel|clarity\.ms|static\.cloudflareinsights\.com|beacon\.min\.js)/i;
const errors = [];

function filesUnder(directory) {
  if (!existsSync(directory)) return [];
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const path = join(directory, entry.name);
    return entry.isDirectory() ? filesUnder(path) : [path];
  });
}

function fail(message) {
  errors.push(message);
}

function metaContent(html, name, value) {
  const escapedValue = value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const tag = new RegExp(`<meta\\s+[^>]*${name}=["']${escapedValue}["'][^>]*>`, 'i').exec(html)?.[0];
  return tag ? /\bcontent=["']([^"']+)["']/i.exec(tag)?.[1] : undefined;
}

function canonicalHref(html) {
  const tag = /<link\s+[^>]*rel=["']canonical["'][^>]*>/i.exec(html)?.[0];
  return tag ? /\bhref=["']([^"']+)["']/i.exec(tag)?.[1] : undefined;
}

function expectedCanonical(file) {
  const page = file === 'index.html' ? '' : file;
  return `${siteOrigin}${basePath}/${page}`;
}

function distPathForUrl(raw, pageFile) {
  if (!raw || raw.startsWith('#') || raw.startsWith('data:') || raw.startsWith('mailto:') || raw.startsWith('tel:') || raw.startsWith('javascript:')) return null;

  let url;
  try {
    const pageUrl = new URL(`${siteOrigin}${basePath}/${pageFile === 'index.html' ? '' : pageFile}`);
    url = new URL(raw, pageUrl);
  } catch {
    fail(`${pageFile}: invalid URL ${raw}`);
    return null;
  }

  if (url.origin !== siteOrigin) return null;
  if (url.pathname !== basePath && !url.pathname.startsWith(`${basePath}/`)) {
    fail(`${pageFile}: internal URL is outside ${basePath}: ${raw}`);
    return null;
  }

  let path = url.pathname.slice(basePath.length).replace(/^\//, '');
  if (!path || path.endsWith('/')) path = `${path}index.html`;
  return join(dist, path);
}

if (!existsSync(dist)) fail('website/dist does not exist; run npm run build first');

for (const page of expectedPages) {
  const path = join(dist, page);
  if (!existsSync(path)) {
    fail(`missing production page: ${page}`);
    continue;
  }

  const html = readFileSync(path, 'utf8');
  const canonical = canonicalHref(html);
  if (canonical !== expectedCanonical(page)) {
    fail(`${page}: canonical URL is ${canonical ?? 'missing'}, expected ${expectedCanonical(page)}`);
  }

  const canonicalTags = html.match(/<link\s+rel=["']canonical["'][^>]*>/gi) ?? [];
  if (canonicalTags.length !== 1) fail(`${page}: expected exactly one canonical link`);

  const expectedImage = `${siteOrigin}${basePath}/${socialImage}`;
  if (metaContent(html, 'property', 'og:image') !== expectedImage) {
    fail(`${page}: OpenGraph image is missing or incorrect`);
  }
  if (metaContent(html, 'name', 'twitter:image') !== expectedImage) {
    fail(`${page}: Twitter image is missing or incorrect`);
  }
  if (metaContent(html, 'name', 'twitter:card') !== 'summary_large_image') {
    fail(`${page}: Twitter card is not summary_large_image`);
  }

  const references = [...html.matchAll(/\b(?:href|src)=["']([^"']+)["']/gi)].map((match) => match[1]);
  for (const reference of references) {
    const target = distPathForUrl(reference, page);
    if (target && !existsSync(target)) fail(`${page}: missing referenced output ${reference}`);
  }
}

const socialPath = join(dist, socialImage);
if (!existsSync(socialPath)) {
  fail(`missing social image: ${socialImage}`);
} else {
  const bytes = readFileSync(socialPath);
  const isPng = bytes.length >= 24 && bytes.readUInt32BE(0) === 0x89504e47 && bytes.readUInt32BE(4) === 0x0d0a1a0a;
  const width = isPng ? bytes.readUInt32BE(16) : 0;
  const height = isPng ? bytes.readUInt32BE(20) : 0;
  if (!isPng || width !== 1200 || height !== 630) fail(`social image must be a 1200x630 PNG, got ${width}x${height}`);
}

for (const file of filesUnder(dist)) {
  const extension = file.split('.').pop()?.toLowerCase();
  if (!['html', 'js', 'css'].includes(extension)) continue;
  const contents = readFileSync(file, 'utf8');
  if (trackingPattern.test(contents)) {
    fail(`${relative(dist, file)}: known tracking script or beacon found in production output`);
  }

  if (extension === 'css') {
    for (const match of contents.matchAll(/url\(["']?([^"')]+)["']?\)/gi)) {
      const sourcePath = relative(dist, file).replace(/\\/g, '/');
      const target = distPathForUrl(match[1], sourcePath);
      if (target && !existsSync(target)) fail(`${relative(dist, file)}: missing referenced asset ${match[1]}`);
    }
  }
}

if (errors.length > 0) {
  console.error(errors.map((error) => `- ${error}`).join('\n'));
  process.exit(1);
}

console.log(`Verified ${expectedPages.length} production pages, canonical metadata, social metadata, assets, links, and tracking-script absence.`);
