#!/bin/bash
# Vently Frontend Deployment Script
# Deploys built React app to EC2 via rsync, or to S3

set -e

MODE=${1:-ec2}  # ec2 or s3

echo "==> Building frontend..."
npm run build

if [ "$MODE" = "s3" ]; then
  # Option A: S3 + CloudFront static hosting
  BUCKET=${S3_FRONTEND_BUCKET:-vently-frontend}
  echo "==> Deploying to S3 bucket: $BUCKET"
  aws s3 sync dist/ s3://$BUCKET/ --delete --cache-control "max-age=31536000,public,immutable" \
    --exclude "index.html"
  aws s3 cp dist/index.html s3://$BUCKET/index.html \
    --cache-control "no-cache,no-store,must-revalidate"
  echo "==> S3 deploy complete. Invalidate CloudFront if configured."

elif [ "$MODE" = "ec2" ]; then
  # Option B: Copy to Nginx web root on EC2
  EC2_HOST=${EC2_HOST:-ubuntu@your-ec2-ip}
  echo "==> Deploying to EC2: $EC2_HOST"
  ssh $EC2_HOST "sudo mkdir -p /var/www/vently && sudo chown ubuntu:ubuntu /var/www/vently"
  rsync -avz --delete dist/ $EC2_HOST:/var/www/vently/
  echo "==> EC2 deploy complete."
fi
