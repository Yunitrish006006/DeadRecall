#!/usr/bin/env bash

set -euo pipefail

readonly ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
readonly MANIFEST="${ROOT}/.github/staging/modrinth-standalone-projects.json"
readonly MODULE_DIR="${ROOT}/standalone-modules"
readonly API='https://api.modrinth.com/v2'
readonly DRY_RUN="${MODRINTH_STANDALONE_DRY_RUN:-true}"
readonly TOKEN="${MODRINTH_TOKEN:-}"
readonly USER_AGENT="Yunitrish006006/Totem-Standalone-Reconcile/${GITHUB_RUN_ID:-local}"

fail() { printf 'error: %s\n' "$*" >&2; exit 1; }

command -v jq >/dev/null || fail 'jq is required'
command -v curl >/dev/null || fail 'curl is required'
command -v sha512sum >/dev/null || fail 'sha512sum is required'
command -v python3 >/dev/null || fail 'python3 is required'
[[ -n "${TOKEN}" ]] || fail 'MODRINTH_TOKEN is required'
[[ -f "${MANIFEST}" ]] || fail "missing manifest: ${MANIFEST}"
[[ -d "${MODULE_DIR}" ]] || fail "missing standalone module directory: ${MODULE_DIR}"
[[ "${DRY_RUN}" == 'true' || "${DRY_RUN}" == 'false' ]] || fail 'MODRINTH_STANDALONE_DRY_RUN must be true or false'

jq -e '
  .schema_version == 1
  and .minecraft == "26.2"
  and .loader == "fabric"
  and (.projects | length == 11)
  and (([.projects[].id] | unique | length) == 11)
  and (([.projects[].slug] | unique | length) == 11)
  and (([.projects[].version] | length) == 11)
  and (([.projects[] | select(.allow_upload == true)] | length) == 1)
  and ([.projects[] | select(.allow_upload == true)][0].slug == "totem-villagers-workforce")
' "${MANIFEST}" >/dev/null || fail 'invalid standalone Modrinth manifest'

auth=(-H "Authorization: ${TOKEN}" -H "User-Agent: ${USER_AGENT}")

api_get() {
  local url="$1" output="$2"
  curl --fail --silent --show-error --retry 3 "${auth[@]}" --output "${output}" "${url}"
}

patch_project() {
  local project_id="$1" payload="$2" response
  response="$(mktemp)"
  local code
  code="$(curl --silent --show-error --retry 3 \
    "${auth[@]}" -H 'Content-Type: application/json' \
    -X PATCH --data "${payload}" \
    --output "${response}" --write-out '%{http_code}' \
    "${API}/project/${project_id}")"
  if [[ "${code}" != '204' ]]; then
    echo "Project PATCH ${project_id} failed with HTTP ${code}." >&2
    cat "${response}" >&2 || true
    rm -f "${response}"
    exit 1
  fi
  rm -f "${response}"
}

jar_content_fingerprint() {
  python3 - "$1" <<'PY'
import hashlib
import sys
import zipfile

path = sys.argv[1]
h = hashlib.sha256()
with zipfile.ZipFile(path) as jar:
    names = sorted(name for name in jar.namelist() if not name.endswith('/'))
    for name in names:
        h.update(name.encode('utf-8'))
        h.update(b'\0')
        h.update(hashlib.sha256(jar.read(name)).digest())
print(h.hexdigest())
PY
}

print_jar_content_diff() {
  python3 - "$1" "$2" <<'PY'
import hashlib
import sys
import zipfile

left_path, right_path = sys.argv[1:]
def entries(path):
    with zipfile.ZipFile(path) as jar:
        return {
            name: hashlib.sha256(jar.read(name)).hexdigest()
            for name in jar.namelist()
            if not name.endswith('/')
        }
left = entries(left_path)
right = entries(right_path)
all_names = sorted(set(left) | set(right))
changes = []
for name in all_names:
    if name not in left:
        changes.append(f'only-remote {name}')
    elif name not in right:
        changes.append(f'only-local  {name}')
    elif left[name] != right[name]:
        changes.append(f'changed     {name}')
for line in changes[:80]:
    print(line)
if len(changes) > 80:
    print(f'... {len(changes) - 80} more differing entries')
PY
}

verify_remote_metadata() {
  local versions_file="$1" version="$2" environment="$3" jar="$4"
  jq -e \
    --arg version "${version}" \
    --arg environment "${environment}" \
    --arg jar "${jar}" '
      ([.[] | select(.version_number == $version)] | length) == 1
      and any(.[];
        .version_number == $version
        and .status == "listed"
        and .version_type == "release"
        and .environment == $environment
        and .game_versions == ["26.2"]
        and .loaders == ["fabric"]
        and (([.files[] | select(.primary == true)][0] // .files[0]).filename == $jar)
      )
    ' "${versions_file}" >/dev/null
}

verify_existing_artifact_content() {
  local versions_file="$1" version="$2" artifact="$3" local_sha512="$4"
  local remote_sha512 remote_url remote_jar local_fp remote_fp
  remote_sha512="$(jq -er --arg version "${version}" '.[] | select(.version_number == $version) | (([.files[] | select(.primary == true)][0] // .files[0]).hashes.sha512)' "${versions_file}")"
  if [[ "${remote_sha512}" == "${local_sha512}" ]]; then
    printf 'Verified byte-identical SHA-512: %s\n' "${local_sha512}"
    return 0
  fi

  remote_url="$(jq -er --arg version "${version}" '.[] | select(.version_number == $version) | (([.files[] | select(.primary == true)][0] // .files[0]).url)' "${versions_file}")"
  remote_jar="$(mktemp --suffix=.jar)"
  curl --fail --silent --show-error --retry 3 --output "${remote_jar}" "${remote_url}"
  local_fp="$(jar_content_fingerprint "${artifact}")"
  remote_fp="$(jar_content_fingerprint "${remote_jar}")"
  if [[ "${local_fp}" == "${remote_fp}" ]]; then
    printf 'Verified content-equivalent JAR (archive SHA differs): remote=%s local=%s content=%s\n' \
      "${remote_sha512}" "${local_sha512}" "${local_fp}"
    rm -f "${remote_jar}"
    return 0
  fi

  printf 'Remote and canonical JAR content differ: remote_sha512=%s local_sha512=%s\n' "${remote_sha512}" "${local_sha512}" >&2
  print_jar_content_diff "${artifact}" "${remote_jar}" >&2
  rm -f "${remote_jar}"
  return 1
}

verify_uploaded_byte_identity() {
  local versions_file="$1" version="$2" sha512="$3"
  jq -e --arg version "${version}" --arg sha512 "${sha512}" '
    any(.[];
      .version_number == $version
      and (([.files[] | select(.primary == true)][0] // .files[0]).hashes.sha512 == $sha512)
    )
  ' "${versions_file}" >/dev/null
}

upload_missing_version() {
  local id="$1" slug="$2" title="$3" version="$4" environment="$5" artifact="$6" jar="$7"
  local dependencies='[]'
  local changelog="Lockstep standalone release used by DeadRecall 2.4.19."
  if [[ "${slug}" == 'totem-villagers-workforce' ]]; then
    dependencies='[{"project_id":"ZggYV6cS","dependency_type":"required"},{"project_id":"dhochP34","dependency_type":"required"}]'
    changelog='Totem Villagers 0.1.30 preserves the current workforce/economy release and fixes village lumberyard and mine terrain placement by locking utility pools to terrain-matching projection.'
  fi

  local payload response
  payload="$(jq -nc \
    --arg name "${title} ${version}" \
    --arg version "${version}" \
    --arg changelog "${changelog}" \
    --arg project_id "${id}" \
    --arg jar "${jar}" \
    --arg environment "${environment}" \
    --argjson dependencies "${dependencies}" '
      {
        name:$name,
        version_number:$version,
        changelog:$changelog,
        dependencies:$dependencies,
        game_versions:["26.2"],
        version_type:"release",
        loaders:["fabric"],
        featured:false,
        status:"listed",
        project_id:$project_id,
        file_parts:["primary"],
        primary_file:"primary",
        environment:$environment
      }
    ')"
  response="$(mktemp)"
  curl --fail --silent --show-error --retry 3 "${auth[@]}" \
    -X POST "${API}/version" \
    -F "data=${payload};type=application/json" \
    -F "primary=@${artifact};filename=${jar};type=application/java-archive" \
    > "${response}"
  printf 'Created %s %s on Modrinth: %s\n' "${slug}" "${version}" "$(jq -er '.id' "${response}")"
  rm -f "${response}"
}

count="$(jq '.projects | length' "${MANIFEST}")"
for ((i = 0; i < count; i++)); do
  item="$(jq -c ".projects[${i}]" "${MANIFEST}")"
  id="$(jq -r '.id' <<<"${item}")"
  slug="$(jq -r '.slug' <<<"${item}")"
  title="$(jq -r '.title' <<<"${item}")"
  repo="$(jq -r '.repo' <<<"${item}")"
  version="$(jq -r '.version' <<<"${item}")"
  jar="$(jq -r '.jar' <<<"${item}")"
  environment="$(jq -r '.environment' <<<"${item}")"
  client_side="$(jq -r '.client_side' <<<"${item}")"
  server_side="$(jq -r '.server_side' <<<"${item}")"
  submit="$(jq -r '.submit' <<<"${item}")"
  allow_upload="$(jq -r '.allow_upload' <<<"${item}")"
  source_url="https://github.com/Yunitrish006006/${repo}"
  issues_url="${source_url}/issues"
  artifact="${MODULE_DIR}/${jar}"
  project_file="$(mktemp)"
  versions_file="$(mktemp)"

  echo
  echo "===== ${title} ${version} (${slug}) ====="
  [[ -f "${artifact}" ]] || fail "missing canonical artifact: ${artifact}"
  local_sha512="$(sha512sum "${artifact}" | awk '{print $1}')"

  api_get "${API}/project/${id}" "${project_file}"
  api_get "${API}/project/${id}/version?include_changelog=false" "${versions_file}"

  jq -e \
    --arg id "${id}" --arg slug "${slug}" --arg title "${title}" \
    --arg client "${client_side}" --arg server "${server_side}" '
      .id == $id
      and .slug == $slug
      and .title == $title
      and .icon_url != null
      and (.body | length) >= 1000
      and .license.id == "Apache-2.0"
      and .client_side == $client
      and .server_side == $server
      and .game_versions == ["26.2"]
      and .loaders == ["fabric"]
      and (.status == "draft" or .status == "processing" or .status == "approved")
    ' "${project_file}" >/dev/null || fail "project metadata validation failed for ${slug}"

  remote_count="$(jq --arg version "${version}" '[.[] | select(.version_number == $version)] | length' "${versions_file}")"
  if [[ "${remote_count}" == '0' ]]; then
    [[ "${allow_upload}" == 'true' ]] || fail "${slug} is missing required release ${version}"
    if [[ "${DRY_RUN}" == 'true' ]]; then
      printf 'Dry run: would upload missing %s as %s (SHA-512 %s).\n' "${version}" "${jar}" "${local_sha512}"
    else
      upload_missing_version "${id}" "${slug}" "${title}" "${version}" "${environment}" "${artifact}" "${jar}"
      api_get "${API}/project/${id}/version?include_changelog=false" "${versions_file}"
      verify_remote_metadata "${versions_file}" "${version}" "${environment}" "${jar}" \
        || fail "uploaded release metadata verification failed for ${slug} ${version}"
      verify_uploaded_byte_identity "${versions_file}" "${version}" "${local_sha512}" \
        || fail "uploaded release SHA-512 verification failed for ${slug} ${version}"
    fi
  elif [[ "${remote_count}" == '1' ]]; then
    verify_remote_metadata "${versions_file}" "${version}" "${environment}" "${jar}" \
      || fail "remote ${slug} ${version} metadata does not match the lockstep manifest"
    verify_existing_artifact_content "${versions_file}" "${version}" "${artifact}" "${local_sha512}" \
      || fail "remote ${slug} ${version} JAR content differs from canonical source build"
  else
    fail "duplicate version_number ${version} exists for ${slug}"
  fi

  status="$(jq -r '.status' "${project_file}")"
  if [[ "${submit}" == 'true' ]]; then
    if [[ "${DRY_RUN}" == 'true' ]]; then
      printf 'Dry run: would set source/issues and submit %s from status %s.\n' "${slug}" "${status}"
    else
      if [[ "${status}" == 'draft' ]]; then
        payload="$(jq -nc --arg source "${source_url}" --arg issues "${issues_url}" '{source_url:$source,issues_url:$issues,status:"processing",requested_status:"approved"}')"
      else
        payload="$(jq -nc --arg source "${source_url}" --arg issues "${issues_url}" '{source_url:$source,issues_url:$issues}')"
      fi
      patch_project "${id}" "${payload}"
    fi
  fi

  if [[ "${DRY_RUN}" == 'false' ]]; then
    for attempt in $(seq 1 15); do
      api_get "${API}/project/${id}" "${project_file}"
      status="$(jq -r '.status' "${project_file}")"
      if [[ "${status}" == 'processing' || "${status}" == 'approved' ]]; then
        jq -e --arg source "${source_url}" --arg issues "${issues_url}" '
          .source_url == $source
          and .issues_url == $issues
          and (.status == "approved" or (.status == "processing" and .requested_status == "approved" and .queued != null))
        ' "${project_file}" >/dev/null || fail "submission state invalid for ${slug}"
        break
      fi
      sleep 2
    done
    [[ "${status}" == 'processing' || "${status}" == 'approved' ]] || fail "${slug} did not enter processing/approved state"
  fi

  jq '{id,slug,title,status,requested_status,queued,published,approved,source_url,issues_url,gallery_count:(.gallery|length)}' "${project_file}"
  rm -f "${project_file}" "${versions_file}"
done

printf '\nStandalone Modrinth reconciliation %s for all 11 Totem projects.\n' "$([[ "${DRY_RUN}" == 'true' ]] && echo 'validated (dry run)' || echo 'completed')"
