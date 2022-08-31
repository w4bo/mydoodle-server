#!/bin/bash
set -exo

git pull

if [ -f .env ]; then
  set -a
  source ./.env
  set +a
fi

./gradlew clean war
rm -rf "${TOMCAT_PATH}\MyDoodle"
cp build/libs/MyDoodle.war "${TOMCAT_PATH}\webapps"
echo "Done."