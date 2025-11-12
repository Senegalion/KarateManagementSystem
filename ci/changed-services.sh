#!/bin/bash

set -euo pipefail

MICRO_ROOT="${1:-microservices}"
BASE_REF="${2:-origin/dev}"
HEAD_REF="${3:-HEAD}"

mapfile -t ALL_SERVICES < <(find "${MICRO_ROOT}" -maxdepth 1 -mindepth 1 -type d -printf "%f\n" | sort)

exists_ref() {
  git rev-parse --verify -q "$1" >/dev/null 2>&1
}

print_all_and_exit() {
  printf "%s\n" "${ALL_SERVICES[@]}"
  exit 0
}

if [ "${BASE_REF}" = "ALL" ]; then
  print_all_and_exit
fi

if [ "${BASE_REF}" = "${HEAD_REF}" ]; then
  if exists_ref "origin/dev"; then
    BASE_REF="origin/dev"
  else
    if exists_ref "${HEAD_REF}^"; then
      BASE_REF="${HEAD_REF}^"
    fi
  fi
fi

if ! exists_ref "${BASE_REF}"; then
  if exists_ref "origin/dev"; then
    BASE_REF="origin/dev"
  else
    print_all_and_exit
  fi
fi

CHANGED_PATHS="$(git diff --name-only "${BASE_REF}...${HEAD_REF}" || true)"

CHANGED_SERVICES=()
if [ -n "${CHANGED_PATHS}" ]; then
  for svc in "${ALL_SERVICES[@]}"; do
    if grep -qE "^${MICRO_ROOT}/${svc}/" <<< "${CHANGED_PATHS}"; then
      CHANGED_SERVICES+=("${svc}")
    fi
  done
fi

if [ "${#CHANGED_SERVICES[@]}" -eq 0 ]; then
  if grep -qE "^(ci/|docker-compose|Jenkinsfile|infra/)" <<< "${CHANGED_PATHS}"; then
    print_all_and_exit
  fi
fi

if [ "${#CHANGED_SERVICES[@]}" -gt 0 ]; then
  printf "%s\n" "${CHANGED_SERVICES[@]}"
fi
