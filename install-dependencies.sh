#!/usr/bin/env sh

git clone -b hotfix/0.5 https://github.com/opencb/opencga.git
git clone -b develop https://github.com/ebivariation/variation-commons.git

cd opencga && mvn install -DskipTests
cd ..
cd variation-commons && mvn install
