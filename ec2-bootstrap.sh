#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# Run ONCE on a fresh EC2 instance to install Docker, Nginx, Certbot
# ssh -i vently.pem ubuntu@100.53.250.27 'bash -s' < ec2-bootstrap.sh
# ─────────────────────────────────────────────────────────────────────────────
set -e

echo "==> Updating packages..."
sudo apt-get update -q && sudo apt-get upgrade -y -q

echo "==> Installing Docker..."
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
  sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update -q
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker ubuntu
sudo systemctl enable docker

echo "==> Installing Nginx..."
sudo apt-get install -y nginx
sudo systemctl enable nginx

echo "==> Installing Certbot (Let's Encrypt SSL)..."
sudo apt-get install -y certbot python3-certbot-nginx

echo "==> Creating app directory..."
sudo mkdir -p /opt/vently-app
sudo chown ubuntu:ubuntu /opt/vently-app

echo ""
echo "✅ Bootstrap complete!"
echo ""
echo "Next steps:"
echo "  1. Point ventfly.com DNS A record → $(curl -s ifconfig.me)"
echo "  2. Run: sudo certbot --nginx -d ventfly.com -d www.ventfly.com -d api.ventfly.com"
echo "  3. Run deploy.ps1 from your local machine"
