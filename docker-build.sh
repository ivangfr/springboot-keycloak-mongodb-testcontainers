#!/usr/bin/env bash

./gradlew book-service:clean book-service:jibDockerBuild -x test -x integrationTest
