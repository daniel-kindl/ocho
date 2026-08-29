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

if ! git merge-base --is-ancestor "${main_ref}^{commit}" "${tagged_commit}^{commit}"; then
  echo "::error::Tagged commit ${tagged_commit} is not descended from ${main_ref}. Refusing to release." >&2
  exit 1
fi

echo "Release provenance passed: ${tagged_commit} descends from ${main_ref}."
