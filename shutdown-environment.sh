#!/usr/bin/env bash

echo
echo "Starting the environment shutdown"
echo "================================="

echo
echo "Removing containers"
echo "-------------------"
docker rm -fv mongodb keycloak

echo
echo "Removing network"
echo "----------------"
docker network rm springboot-keycloak-mongodb-testcontainers-net

echo
echo "Environment shutdown successfully"
echo "================================="
echo