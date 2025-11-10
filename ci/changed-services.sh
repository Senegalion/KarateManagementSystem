#!/bin/bash

set -euo pipefail

MICRO_ROOT="${1:-microservices}"
BASE_REF="${2:-origin/dev}"
HEAD_REF="${3:-HEAD}"

mapfile -t ALL_SERVICES < <(find "${MICRO_ROOT}" -maxdepth 1 -mindepth 1 -type d -printf "%f\n" | sort)

if [ "${BASE_REF}" = "ALL" ]; then
  printf "%s\n" "${ALL_SERVICES[@]}"
  exit 0
fi

if ! git rev-parse --verify -q "${BASE_REF}" >/dev/null; then
  printf "%s\n" "${ALL_SERVICES[@]}"
  exit 0
fi

CHANGED_PATHS="$(git diff --name-only "${BASE_REF}...${HEAD_REF}" || true)"

CHANGED_SERVICES=()
for svc in "${ALL_SERVICES[@]}"; do
  if grep -qE "^${MICRO_ROOT}/${svc}/" <<< "${CHANGED_PATHS}"; then
    CHANGED_SERVICES+=("${svc}")
  fi
done

if [ "${#CHANGED_SERVICES[@]}" -eq 0 ]; then
  if grep -qE "^(ci/|docker-compose|Jenkinsfile|infra/)" <<< "${CHANGED_PATHS}"; then
    printf "%s\n" "${ALL_SERVICES[@]}"
  else
    :
  fi
else
  printf "%s\n" "${CHANGED_SERVICES[@]}"
fi
