#!/usr/bin/env bash

./gradlew book-service:clean book-service:bootBuildImage -x test -x integrationTest
