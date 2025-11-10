set -euo pipefail

SERVICE="$1"
MICRO_ROOT="${MICRO_ROOT:-microservices}"
IMAGE_REPO="${IMAGE_REPO:-your-docker-user/kms}"
TAG="${TAG:-latest}"
GIT_SHA="${GIT_SHA:-}"

BUILD_DIR="${MICRO_ROOT}/${SERVICE}"

if [ ! -d "${BUILD_DIR}" ]; then
  echo "Service dir not found: ${BUILD_DIR}" >&2
  exit 1
fi

docker build -t "${IMAGE_REPO}-${SERVICE}:${TAG}" "${BUILD_DIR}"

if [ -n "${GIT_SHA}" ]; then
  docker tag "${IMAGE_REPO}-${SERVICE}:${TAG}" "${IMAGE_REPO}-${SERVICE}:${GIT_SHA}"
fi
