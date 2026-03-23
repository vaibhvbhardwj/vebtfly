#!/bin/bash

# Deploy Razorpay Integration to EC2
# This script builds the application and deploys it to the EC2 server

echo "🚀 Deploying Razorpay Integration..."

# Build the application
echo "📦 Building application..."
./mvnw clean package -DskipTests

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "❌ Build failed. Deployment aborted."
    exit 1
fi

echo "✅ Build successful!"

# Copy to EC2 (replace with your actual deployment method)
echo "📤 Deploying to EC2..."
scp target/vently-0.0.1-SNAPSHOT.jar ec2-user@100.53.250.27:/home/ec2-user/vently.jar

# Restart the service on EC2
echo "🔄 Restarting service..."
ssh ec2-user@100.53.250.27 "sudo systemctl restart vently"

# Check service status
echo "🔍 Checking service status..."
ssh ec2-user@100.53.250.27 "sudo systemctl status vently --no-pager"

echo "🎉 Deployment complete!"
echo "💡 Test the subscription endpoints:"
echo "   - GET  https://api.ventfly.com/api/v1/subscriptions/current"
echo "   - POST https://api.ventfly.com/api/v1/subscriptions/create-order"
echo "   - POST https://api.ventfly.com/api/v1/subscriptions/verify-payment"