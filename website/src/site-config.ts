/** URLs that are shared by page components and kept in one place for launch changes. */
export const githubReleaseUrl = 'https://github.com/daniel-kindl/ocho/releases/latest';

type PlayListing =
  | {
      available: false;
      url: undefined;
      status: 'coming soon';
      ariaLabel: 'Google Play coming soon';
    }
  | {
      available: true;
      url: string;
      status: 'available now';
      ariaLabel: 'Download Ocho from Google Play';
    };

/** Update URL, availability, and user-facing copy together when Play launches. */
export const playListing: PlayListing = {
  available: false,
  url: undefined,
  status: 'coming soon',
  ariaLabel: 'Google Play coming soon',
};
