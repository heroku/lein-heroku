#!/bin/bash

set -e

TEST_DIR=`pwd`/it

_setup() {
  cd `mktemp -d -t lein-heroku`
  echo "Running '$1' in $(pwd)"
}

_cleanup() {
  echo "SUCCESS!"
  heroku destroy --confirm $1
  cd $TEST_DIR
}

_fail() {
  echo "FAILURE: $@"
  exit 1
}

test_happy_path() {
  _setup "happy path"
  cp -R  $TEST_DIR/happy-path/ .
  appName=`echo "lein-test-$(date +%Y%m%d%H%M%S)"`
  git init
  heroku create $appName
  lein uberjar
  lein heroku deploy
  sleep 10
  curl -sL https://$appName.herokuapp.com | grep "Welcome to happy-path"
  _cleanup $appName
}

lein test

echo ""
echo "Running Integration Tests..."
echo "=============================="
cd $TEST_DIR

test_happy_path

cd ..
echo "=============================="
echo "ALL TESTS PASSED!"
