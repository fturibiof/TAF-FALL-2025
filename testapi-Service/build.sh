#!/bin/bash
source .env
REGISTRY_URL=$DOCKER_URL
USERNAME=$DOCKER_USER
PASSWORD=$DOCKER_PASSWORD
REGISTRY=$REGISTRY_BASE

docker compose build backend
docker compose build frontend
docker compose build selenium
docker compose build testapi

docker login $REGISTRY_URL -u $USERNAME -p $PASSWORD
docker push ${REGISTRY}/lionel-test-team2-backend:${BACKEND_VERSION}
docker push ${REGISTRY}/lionel-test-team2-frontend:${FRONTEND_VERSION}
docker push ${REGISTRY}/lionel-test-team2-selenium:${SELENIUM_VERSION}
docker push ${REGISTRY}/lionel-test-team2-testapi:${TEST_API_VERSION}

read -p "Press Enter to exit..."