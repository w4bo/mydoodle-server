#!/bin/bash
set -exo

chmod +x *.sh
chmod +x ./gradlew
cp .env.example .env

rm src/main/resources/.env || true
ln .env src/main/resources/.env