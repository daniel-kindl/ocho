import { en, type SiteCopy } from './en';

export const defaultLocale = 'en' as const;
export const siteCopy: SiteCopy = en;
const copies: Record<string, SiteCopy> = { en };

/** Returns the requested copy when it exists, otherwise the English default. */
export function getSiteCopy(locale = defaultLocale): SiteCopy {
  return copies[locale] ?? en;
}
