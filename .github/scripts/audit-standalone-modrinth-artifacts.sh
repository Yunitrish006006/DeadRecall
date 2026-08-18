#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MANIFEST="${ROOT}/.github/staging/modrinth-standalone-projects.json"
MODULE_DIR="${ROOT}/standalone-modules"
API='https://api.modrinth.com/v2'
TOKEN="${MODRINTH_TOKEN:-}"
UA="Yunitrish006006/Totem-Standalone-Audit/${GITHUB_RUN_ID:-local}"

fail() { printf 'error: %s\n' "$*" >&2; exit 1; }
[[ -n "${TOKEN}" ]] || fail 'MODRINTH_TOKEN is required'
for cmd in jq curl sha512sum python3; do command -v "$cmd" >/dev/null || fail "$cmd is required"; done
[[ -f "${MANIFEST}" ]] || fail "missing manifest: ${MANIFEST}"
[[ -d "${MODULE_DIR}" ]] || fail "missing module dir: ${MODULE_DIR}"

auth=(-H "Authorization: ${TOKEN}" -H "User-Agent: ${UA}")

fingerprint() {
  python3 - "$1" <<'PY'
import hashlib, sys, zipfile
h = hashlib.sha256()
with zipfile.ZipFile(sys.argv[1]) as z:
    for name in sorted(n for n in z.namelist() if not n.endswith('/')):
        h.update(name.encode()); h.update(b'\0'); h.update(hashlib.sha256(z.read(name)).digest())
print(h.hexdigest())
PY
}

diff_entries() {
  python3 - "$1" "$2" <<'PY'
import hashlib, sys, zipfile
def e(path):
    with zipfile.ZipFile(path) as z:
        return {n: hashlib.sha256(z.read(n)).hexdigest() for n in z.namelist() if not n.endswith('/')}
a,b=e(sys.argv[1]),e(sys.argv[2])
for n in sorted(set(a)|set(b)):
    if n not in a: print('only-remote', n)
    elif n not in b: print('only-local ', n)
    elif a[n] != b[n]: print('changed    ', n)
PY
}

mismatches=0
missing_allowed=0
verified=0
count="$(jq '.projects|length' "${MANIFEST}")"
for ((i=0;i<count;i++)); do
  item="$(jq -c ".projects[$i]" "${MANIFEST}")"
  id="$(jq -r '.id' <<<"$item")"
  slug="$(jq -r '.slug' <<<"$item")"
  version="$(jq -r '.version' <<<"$item")"
  jar="$(jq -r '.jar' <<<"$item")"
  environment="$(jq -r '.environment' <<<"$item")"
  allow_upload="$(jq -r '.allow_upload' <<<"$item")"
  local_jar="${MODULE_DIR}/${jar}"
  versions="$(mktemp)"
  [[ -f "$local_jar" ]] || fail "missing $local_jar"
  curl --fail --silent --show-error --retry 3 "${auth[@]}" "${API}/project/${id}/version?include_changelog=false" > "$versions"
  remote_count="$(jq --arg v "$version" '[.[]|select(.version_number==$v)]|length' "$versions")"
  echo "===== ${slug} ${version} ====="
  if [[ "$remote_count" == 0 ]]; then
    if [[ "$allow_upload" == true ]]; then
      echo "PLANNED-MISSING: may upload ${jar}"
      missing_allowed=$((missing_allowed+1))
    else
      echo "ERROR-MISSING: required current version is absent"
      mismatches=$((mismatches+1))
    fi
    rm -f "$versions"
    continue
  fi
  if [[ "$remote_count" != 1 ]]; then
    echo "ERROR-DUPLICATE: ${remote_count} versions share version_number ${version}"
    mismatches=$((mismatches+1)); rm -f "$versions"; continue
  fi

  if ! jq -e --arg v "$version" --arg e "$environment" --arg j "$jar" '
      any(.[]; .version_number==$v and .status=="listed" and .version_type=="release" and .environment==$e and .game_versions==["26.2"] and .loaders==["fabric"] and (([.files[]|select(.primary==true)][0]//.files[0]).filename==$j))
    ' "$versions" >/dev/null; then
    echo "ERROR-METADATA: current version metadata differs"
    mismatches=$((mismatches+1)); rm -f "$versions"; continue
  fi

  remote_sha="$(jq -r --arg v "$version" '.[]|select(.version_number==$v)|(([.files[]|select(.primary==true)][0]//.files[0]).hashes.sha512)' "$versions")"
  local_sha="$(sha512sum "$local_jar"|awk '{print $1}')"
  if [[ "$remote_sha" == "$local_sha" ]]; then
    echo "BYTE-IDENTICAL ${local_sha}"
    verified=$((verified+1)); rm -f "$versions"; continue
  fi

  remote_url="$(jq -r --arg v "$version" '.[]|select(.version_number==$v)|(([.files[]|select(.primary==true)][0]//.files[0]).url)' "$versions")"
  remote_jar="$(mktemp --suffix=.jar)"
  curl --fail --silent --show-error --retry 3 -o "$remote_jar" "$remote_url"
  lfp="$(fingerprint "$local_jar")"; rfp="$(fingerprint "$remote_jar")"
  if [[ "$lfp" == "$rfp" ]]; then
    echo "CONTENT-IDENTICAL archive SHA differs: remote=${remote_sha} local=${local_sha} content=${lfp}"
    verified=$((verified+1))
  else
    echo "CONTENT-DRIFT remote=${remote_sha} local=${local_sha}"
    diff_entries "$local_jar" "$remote_jar" | sed -n '1,120p'
    mismatches=$((mismatches+1))
  fi
  rm -f "$versions" "$remote_jar"
done

echo
printf 'SUMMARY verified=%d planned_missing=%d mismatches=%d\n' "$verified" "$missing_allowed" "$mismatches"
[[ "$mismatches" == 0 ]] || exit 1
