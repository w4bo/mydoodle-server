#!/bin/bash
set -exo

git pull

if [ -f .env ]; then
  set -a
  source ./.env
  set +a
fi
yes | cp .env src/main/resources
./gradlew clean war
rm -rf "${TOMCAT_PATH}/${ARTIFACT}"
cp "build/libs/${ARTIFACT}.war" "${TOMCAT_PATH}"
echo "Done."