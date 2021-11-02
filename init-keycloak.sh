#!/usr/bin/env bash

KEYCLOAK_HOST_PORT=${1:-"localhost:8080"}
echo
echo "KEYCLOAK_HOST_PORT: $KEYCLOAK_HOST_PORT"

echo
echo "Getting admin access token"
echo "--------------------------"

ADMIN_TOKEN=$(curl -s -X POST "http://$KEYCLOAK_HOST_PORT/auth/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d 'password=admin' \
  -d 'grant_type=password' \
  -d 'client_id=admin-cli' | jq -r '.access_token')

echo "ADMIN_TOKEN=$ADMIN_TOKEN"
echo

echo "Creating realm"
echo "--------------"

curl -i -X POST "http://$KEYCLOAK_HOST_PORT/auth/admin/realms" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"realm": "company-services", "enabled": true}'

echo "Creating client"
echo "---------------"

CLIENT_ID=$(curl -si -X POST "http://$KEYCLOAK_HOST_PORT/auth/admin/realms/company-services/clients" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"clientId": "book-service", "directAccessGrantsEnabled": true, "redirectUris": ["http://localhost:9080/*"]}' \
  | grep -oE '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}')

echo "CLIENT_ID=$CLIENT_ID"
echo

echo "Getting client secret"
echo "---------------------"

BOOK_SERVICE_CLIENT_SECRET=$(curl -s -X POST "http://$KEYCLOAK_HOST_PORT/auth/admin/realms/company-services/clients/$CLIENT_ID/client-secret" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.value')

echo "BOOK_SERVICE_CLIENT_SECRET=$BOOK_SERVICE_CLIENT_SECRET"
echo

echo "Creating client role"
echo "--------------------"

curl -i -X POST "http://$KEYCLOAK_HOST_PORT/auth/admin/realms/company-services/clients/$CLIENT_ID/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "manage_books"}'

ROLE_ID=$(curl -s "http://$KEYCLOAK_HOST_PORT/auth/admin/realms/company-services/clients/$CLIENT_ID/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

echo "ROLE_ID=$ROLE_ID"
echo

echo "Creating user"
echo "-------------"

USER_ID=$(curl -si -X POST "http://$KEYCLOAK_HOST_PORT/auth/admin/realms/company-services/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username": "ivan.franchin", "enabled": true, "credentials": [{"type": "password", "value": "123", "temporary": false}]}' \
  | grep -oE '[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}')

echo "USER_ID=$USER_ID"
echo

echo "Setting client role to user"
echo "---------------------------"

curl -i -X POST "http://$KEYCLOAK_HOST_PORT/auth/admin/realms/company-services/users/$USER_ID/role-mappings/clients/$CLIENT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{"id":"'"$ROLE_ID"'","name":"manage_books"}]'

echo "Getting user access token"
echo "-------------------------"

curl -s -X POST "http://$KEYCLOAK_HOST_PORT/auth/realms/company-services/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=ivan.franchin" \
  -d "password=123" \
  -d "grant_type=password" \
  -d "client_secret=$BOOK_SERVICE_CLIENT_SECRET" \
  -d "client_id=book-service" | jq -r .access_token
echo

echo "---------"
echo "BOOK_SERVICE_CLIENT_SECRET=$BOOK_SERVICE_CLIENT_SECRET"
echo "---------"
