#!/bin/bash
# Usage: ./deploy-docker.sh <EC2_HOST> <SSH_KEY_PATH>
# Example: ./deploy-docker.sh ubuntu@ec2-xx-xx-xx-xx.compute.amazonaws.com ~/.ssh/vently.pem

set -e

EC2_HOST=${1:?Usage: ./deploy-docker.sh <EC2_HOST> <SSH_KEY_PATH>}
SSH_KEY=${2:?Usage: ./deploy-docker.sh <EC2_HOST> <SSH_KEY_PATH>}
APP_DIR="/opt/vently-app"

echo "==> Deploying to $EC2_HOST..."

# 1. Ensure Docker + docker-compose are installed on EC2
ssh -i "$SSH_KEY" "$EC2_HOST" bash <<'REMOTE'
  if ! command -v docker &>/dev/null; then
    echo "Installing Docker..."
    curl -fsSL https://get.docker.com | sh
    sudo usermod -aG docker ubuntu
    newgrp docker
  fi
  if ! command -v docker compose &>/dev/null; then
    echo "Installing docker compose plugin..."
    sudo apt-get install -y docker-compose-plugin
  fi
  sudo mkdir -p /opt/vently-app
  sudo chown ubuntu:ubuntu /opt/vently-app
REMOTE

# 2. Sync code to EC2 (exclude node_modules, target, .git)
echo "==> Syncing code..."
rsync -az --delete \
  --exclude='.git' \
  --exclude='vently/target' \
  --exclude='vently-ui/node_modules' \
  --exclude='vently-ui/dist' \
  -e "ssh -i $SSH_KEY" \
  ./ "$EC2_HOST:$APP_DIR/"

# 3. Build and restart containers on EC2
echo "==> Building and starting containers..."
ssh -i "$SSH_KEY" "$EC2_HOST" bash <<REMOTE
  cd $APP_DIR
  docker compose build --no-cache
  docker compose up -d
  echo "==> Containers running:"
  docker compose ps
REMOTE

echo ""
echo "==> Deploy complete!"
echo "    Backend:  http://$EC2_HOST:8080"
echo "    Frontend: http://$EC2_HOST:3000"
echo ""
echo "    Nginx on EC2 proxies these to ventfly.com / api.ventfly.com"
echo "    (nginx.conf already handles SSL termination)"
