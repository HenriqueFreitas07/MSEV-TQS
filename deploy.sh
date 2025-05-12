#!/bin/bash

# login to hashcorp vault 
hcp auth login --client-id $HCP_CLIENT --client-secret $HCP_SECRET

SECRETS=("SECRET_KEY")
BACKEND_SECRETS=("DB_HOST" "POSTGRES_USER"  "POSTGRES_PASSWORD"  "POSTGRES_DB" )

ENV_FILE=".env"
BACK_ENV_FILE="./backend/.env"
echo "" > $ENV_FILE
echo "" > $BACK_ENV_FILE

for SECRET_NAME in "${SECRETS[@]}"; do
  VALUE=$(hcp vault-secrets secrets open "$SECRET_NAME" | awk -F': ' '/^Value:/ { print $2 }' | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
  echo "$SECRET_NAME=$VALUE" >> $ENV_FILE

done


for SECRET_NAME in "${BACKEND_SECRETS[@]}"; do
  VALUE=$(hcp vault-secrets secrets open "$SECRET_NAME" | awk -F': ' '/^Value:/ { print $2 }' | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
  echo "$SECRET_NAME=$VALUE" >> $BACK_ENV_FILE
done

# copy certificates into build context 
cp -r ../certificates/  .
docker-compose down
# to prevent a container config error
docker rm -f $(docker ps -aq) 
docker-compose -f docker-compose.prod.yaml --project-name msev up --build -d
