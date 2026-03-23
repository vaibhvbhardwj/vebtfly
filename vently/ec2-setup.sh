#!/bin/bash
# EC2 Instance Setup Script (run once on a fresh Ubuntu 22.04 instance)

set -e

echo "==> Updating system..."
sudo apt-get update && sudo apt-get upgrade -y

echo "==> Installing Java 21..."
sudo apt-get install -y openjdk-21-jre-headless

echo "==> Installing Nginx..."
sudo apt-get install -y nginx

echo "==> Creating app directories..."
sudo mkdir -p /opt/vently /var/log/vently
sudo chown ubuntu:ubuntu /opt/vently /var/log/vently

echo "==> Copying systemd service..."
sudo cp vently.service /etc/systemd/system/vently.service
sudo systemctl daemon-reload
sudo systemctl enable vently

echo "==> Setup complete."
echo "Next steps:"
echo "  1. Copy /opt/vently/.env with your environment variables (see env.example)"
echo "  2. Run ./deploy.sh prod to deploy the JAR"
echo "  3. Configure Nginx (see nginx.conf)"
