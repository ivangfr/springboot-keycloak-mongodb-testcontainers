#!/usr/bin/env bash

DOCKER_IMAGE_PREFIX="ivanfranchin"
APP_NAME="book-service"
APP_VERSION="1.0.0"
DOCKER_IMAGE_NAME="${DOCKER_IMAGE_PREFIX}/${APP_NAME}:${APP_VERSION}"

./gradlew \
  "$APP_NAME":clean \
  "$APP_NAME":bootBuildImage \
  -x test \
  -x integrationTest \
  --imageName="$DOCKER_IMAGE_NAME"
