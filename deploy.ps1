$PEM = "C:\Users\vaibh\Downloads\vently.pem"
$EC2 = "ubuntu@100.53.250.27"
$LOCAL = "C:\Users\vaibh\OneDrive\Desktop\deliverables\vently"

Write-Host "Step 1: Packaging files..." -ForegroundColor Cyan
Set-Location $LOCAL
Compress-Archive -Path vently, vently-ui, docker-compose.yml -DestinationPath deploy.zip -Force

Write-Host "Step 2: Uploading to EC2..." -ForegroundColor Cyan
scp -i $PEM deploy.zip "${EC2}:/opt/vently-app/"

Write-Host "Step 3: Unzipping on EC2..." -ForegroundColor Cyan
ssh -i $PEM $EC2 "cd /opt/vently-app && sudo apt-get install -y unzip -q && unzip -o deploy.zip && rm deploy.zip"

Write-Host "Step 4: Copying .env file..." -ForegroundColor Cyan
scp -i $PEM "$LOCAL\vently\.env" "${EC2}:/opt/vently-app/vently/.env"

Write-Host "Step 5: Building and starting containers (this takes 10-15 mins)..." -ForegroundColor Cyan
ssh -i $PEM $EC2 "cd /opt/vently-app && sudo docker compose down; sudo docker compose build --no-cache && sudo docker compose up -d"

Write-Host "Step 6: Checking container status..." -ForegroundColor Cyan
ssh -i $PEM $EC2 "sudo docker compose -f /opt/vently-app/docker-compose.yml ps"

Write-Host "Step 7: Reloading nginx..." -ForegroundColor Cyan
ssh -i $PEM $EC2 "sudo cp /opt/vently-app/vently/nginx.conf /etc/nginx/sites-available/vently && sudo nginx -t && sudo systemctl reload nginx"

Write-Host "Done! Your app should be live." -ForegroundColor Green
Remove-Item "$LOCAL\deploy.zip" -ErrorAction SilentlyContinue
