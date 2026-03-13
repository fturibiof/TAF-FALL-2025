#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

CONTAINER="taf-mongodb"
DB="taf"
USER="admin"
PASS="admin123"

echo "Copying mockData.json into container..."
docker cp ./mockData.json "${CONTAINER}:/tmp/mockData.json"

echo "Copying import_mock_data.js into container..."
docker cp ./import_mock_data.js "${CONTAINER}:/tmp/import_mock_data.js"

# If you pass --wipe, we set WIPE=1 for the mongosh session
WIPE_ENV="0"
if [[ "${1:-}" == "--wipe" ]]; then
  WIPE_ENV="1"
fi

echo "Running import in Mongo container (WIPE=${WIPE_ENV})..."
docker exec -it -e WIPE="${WIPE_ENV}" "${CONTAINER}" \
  mongosh -u "${USER}" -p "${PASS}" --authenticationDatabase admin "${DB}" \
  /tmp/import_mock_data.js
