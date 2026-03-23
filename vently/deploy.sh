#!/bin/bash
# Vently Backend Deployment Script
# Usage: ./deploy.sh [prod|staging]

set -e

ENV=${1:-prod}
JAR_NAME="vently-0.0.1-SNAPSHOT.jar"
APP_DIR="/opt/vently"
LOG_DIR="/var/log/vently"

echo "==> Building Vently backend (profile: $ENV)..."
./mvnw clean package -DskipTests -Dspring.profiles.active=$ENV

echo "==> Build complete: target/$JAR_NAME"

if [ "$ENV" = "prod" ]; then
  echo "==> Deploying to production..."

  # Create dirs if needed
  sudo mkdir -p $APP_DIR $LOG_DIR

  # Copy JAR
  sudo cp target/$JAR_NAME $APP_DIR/app.jar

  # Restart service
  sudo systemctl restart vently

  echo "==> Deployment complete. Checking status..."
  sleep 3
  sudo systemctl status vently --no-pager
fi
