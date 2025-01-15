#!/usr/bin/env bash

MONGO_VERSION="8.0.3"
KEYCLOAK_VERSION="26.0.7"

source scripts/my-functions.sh

echo
echo "Starting environment"
echo "===================="

echo
echo "Creating network"
echo "----------------"
docker network create springboot-keycloak-mongodb-testcontainers-net

echo
echo "Starting mongodb"
echo "----------------"
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  --restart=unless-stopped \
  --network=springboot-keycloak-mongodb-testcontainers-net \
  --health-cmd="echo 'db.stats().ok' | mongosh localhost:27017/bookdb --quiet" \
  mongo:${MONGO_VERSION}

echo
echo "Starting keycloak"
echo "-----------------"
docker run -d \
  --name keycloak \
  -p 8080:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  -e KC_DB=dev-mem \
  --restart=unless-stopped \
  --network=springboot-keycloak-mongodb-testcontainers-net \
  --health-cmd="curl -f http://localhost:8080/health/ready || exit 1" \
  quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} start-dev

echo
wait_for_container_log "mongodb" "Waiting for connections"

echo
wait_for_container_log "keycloak" "started in"

echo
echo "Environment Up and Running"
echo "=========================="
echo