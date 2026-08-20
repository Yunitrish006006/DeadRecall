#!/usr/bin/env bash
set -euo pipefail

readonly ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
readonly LOCKSTEP="${ROOT}/.lockstep/current-bundle"
readonly OUTPUT="${ROOT}/test-artifacts/gameplay-gallery/existing-client-tests"
readonly GRADLE="${LOCKSTEP}/TotemCore/gradlew"

rm -rf "${OUTPUT}"
mkdir -p "${OUTPUT}"
chmod +x "${GRADLE}"

core_jar="${LOCKSTEP}/TotemCore/build/libs/totem-core-0.7.0.jar"
remnant_jar="${LOCKSTEP}/TotemRemnant/build/libs/totem-remnant-0.2.13.jar"
excavation_jar="${LOCKSTEP}/TotemExcavation/build/libs/totem-excavation-0.1.5.jar"

test -f "${core_jar}"
test -f "${remnant_jar}"
test -f "${excavation_jar}"

copy_screenshots() {
    local repo="$1"
    local module="$2"
    local repo_dir="${LOCKSTEP}/${repo}"
    local out_dir="${OUTPUT}/${module}"
    mkdir -p "${out_dir}"

    while IFS= read -r -d '' file; do
        local rel
        local safe
        rel="${file#${repo_dir}/}"
        safe="${rel//\//__}"
        cp "${file}" "${out_dir}/${safe}"
    done < <(find "${repo_dir}" \
        \( -path '*/build/run/clientGameTest/screenshots/*.png' \
        -o -path '*/test-artifacts/screenshots/*.png' \) \
        -type f -print0 2>/dev/null || true)

    local count
    count="$(find "${out_dir}" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
    printf 'Captured %-18s %s screenshots\n' "${module}" "${count}"
    test "${count}" -gt 0
}

run_client() {
    local repo="$1"
    local module="$2"
    shift 2
    local dir="${LOCKSTEP}/${repo}"
    printf '\n===== Fresh client capture: %s =====\n' "${repo}"
    xvfb-run -a "${GRADLE}" -p "${dir}" "$@" runClientGameTest --no-daemon --stacktrace
    copy_screenshots "${repo}" "${module}"
}

run_client TotemAlchemy totem-alchemy \
    -PtotemCoreJar="${core_jar}"

run_client TotemAutomata totem-automata \
    -PtotemCoreJar="${core_jar}" \
    -PtotemExcavationJar="${excavation_jar}"

run_client TotemNexus totem-nexus \
    -PtotemCoreJar="${core_jar}"

run_client TotemVillagers totem-villagers \
    -PtotemCoreJar="${core_jar}" \
    -PtotemRemnantJar="${remnant_jar}"

run_client TotemRemnant totem-remnant \
    -PtotemCoreJar="${core_jar}"

run_client TotemLocksmith totem-locksmith \
    -PtotemCoreJar="${core_jar}"

python3 - <<'PY'
from pathlib import Path
import json
root = Path('test-artifacts/gameplay-gallery/existing-client-tests')
summary = {}
for module in sorted(p for p in root.iterdir() if p.is_dir()):
    files = sorted(f.name for f in module.glob('*.png'))
    summary[module.name] = {"count": len(files), "files": files}
Path('test-artifacts/gameplay-gallery/existing-client-tests.json').write_text(
    json.dumps(summary, indent=2) + '\n'
)
print(json.dumps({k:v['count'] for k,v in summary.items()}, indent=2))
PY
