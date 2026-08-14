const base = import.meta.env.BASE_URL.replace(/\/$/, '');

export function withBase(path = ''): string {
  const normalizedPath = path.replace(/^\//, '');
  return normalizedPath ? `${base}/${normalizedPath}` : `${base}/`;
}
