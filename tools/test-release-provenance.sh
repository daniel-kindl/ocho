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
git -C "${test_repo}" branch main

printf 'release\n' >> "${test_repo}/file"
git -C "${test_repo}" commit --quiet -am 'release'
accepted_commit="$(git -C "${test_repo}" rev-parse HEAD)"
(cd "${test_repo}" && bash "${script_dir}/verify-release-provenance.sh" \
  "refs/heads/main" "${accepted_commit}") >/dev/null

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
