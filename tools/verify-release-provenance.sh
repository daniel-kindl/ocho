#!/usr/bin/env bash
set -euo pipefail

main_ref="${1:-origin/main}"
tagged_commit="${2:-${GITHUB_SHA:-HEAD}}"

if ! git rev-parse --verify "${main_ref}^{commit}" >/dev/null 2>&1; then
  echo "::error::Cannot resolve stable reference '${main_ref}'." >&2
  exit 1
fi

if ! git rev-parse --verify "${tagged_commit}^{commit}" >/dev/null 2>&1; then
  echo "::error::Cannot resolve tagged commit '${tagged_commit}'." >&2
  exit 1
fi

if ! git merge-base --is-ancestor "${tagged_commit}^{commit}" "${main_ref}^{commit}"; then
  echo "::error::Tagged commit ${tagged_commit} is not contained in ${main_ref}. Refusing to release." >&2
  exit 1
fi

echo "Release provenance passed: ${tagged_commit} is contained in ${main_ref}."
