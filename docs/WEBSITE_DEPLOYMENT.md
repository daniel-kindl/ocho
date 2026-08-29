# Website deployment and analytics gate

The website is intended to be a static, analytics-free GitHub Pages deployment.
The repository build contains no tracking scripts. `website/npm run verify` checks
that every built page has its expected links and assets, that canonical and social
metadata use the `/ocho` base path, and that known analytics/beacon URLs are absent.
The Pages workflow runs this check before uploading the artifact.

## Deployed-site investigation

On 2026-08-29, a clean unauthenticated request with no cookies or browser state to

`https://daniel-kindl.github.io/ocho/`

returned HTTP 200 HTML containing this script:

`https://static.cloudflareinsights.com/beacon.min.js/v3d52b47920f24c319d37e2661827c42b1787588026925`

The response also carried Cloudflare headers (`server: cloudflare`, `cf-cache-status`,
and `cf-ray`). The script is not present in `website/src`, `astro.config.mjs`, or a
fresh local `website/dist` build. This identifies the script as hosting/edge injection,
not an Astro page or repository asset. No other analytics script was found in the
deployed HTML during this check.

## Recheck requirement

The observation above requires a clean recheck after deployment. A follow-up clean
`curl` request on 2026-08-29 found no Cloudflare beacon in the response, although the
response still carried Cloudflare headers. The current evidence therefore does not
justify prescribing a hosting change. A repository change cannot remove a script added
after the Pages artifact is served.

After every website deployment, fetch the deployed home page and privacy page again in
a clean session and confirm that neither response contains:

- `static.cloudflareinsights.com`
- `beacon.min.js`
- any other analytics, advertising, or tracking script

If the beacon is reproduced, identify the hosting or edge configuration that adds it
and treat its removal or an explicit policy decision as a release gate. If both clean
responses remain free of the beacon, no hosting change is required based on this
observation. Keep the evidence with the release review because the repository privacy
policy states that Ocho does not use analytics or advertising.
