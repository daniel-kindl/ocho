#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
test_repo="$(mktemp -d)"
trap 'rm -rf "${test_repo}"' EXIT

git -C "${test_repo}" init --quiet
git -C "${test_repo}" config user.email test@example.invalid
git -C "${test_repo}" config user.name "Release provenance test"
printf 'base\n' > "${test_repo}/file"
git -C "${test_repo}" add file
git -C "${test_repo}" commit --quiet -m 'base'
base_commit="$(git -C "${test_repo}" rev-parse HEAD)"
git -C "${test_repo}" branch main

printf 'tag ahead\n' >> "${test_repo}/file"
git -C "${test_repo}" commit --quiet -am 'tag ahead of main'
tag_ahead_commit="$(git -C "${test_repo}" rev-parse HEAD)"
if (cd "${test_repo}" && bash "${script_dir}/verify-release-provenance.sh" \
  "refs/heads/main" "${tag_ahead_commit}") >/dev/null 2>&1; then
  echo "Expected a tag ahead of main to be rejected." >&2
  exit 1
fi

git -C "${test_repo}" switch --quiet main
printf 'stable\n' >> "${test_repo}/file"
git -C "${test_repo}" commit --quiet -am 'stable main update'
if (cd "${test_repo}" && bash "${script_dir}/verify-release-provenance.sh" \
  "refs/heads/main" "${base_commit}") >/dev/null; then
  :
else
  echo "Expected a tag contained in main to be accepted." >&2
  exit 1
fi

git -C "${test_repo}" switch --quiet --orphan unrelated
git -C "${test_repo}" rm --quiet --cached --ignore-unmatch -r .
printf 'unrelated\n' > "${test_repo}/unrelated"
git -C "${test_repo}" add unrelated
git -C "${test_repo}" commit --quiet -m 'unrelated'
rejected_commit="$(git -C "${test_repo}" rev-parse HEAD)"
if (cd "${test_repo}" && bash "${script_dir}/verify-release-provenance.sh" \
  "refs/heads/main" "${rejected_commit}") >/dev/null 2>&1; then
  echo "Expected unrelated commit to be rejected." >&2
  exit 1
fi

echo "Release provenance acceptance and rejection tests passed."
