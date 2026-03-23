# ─────────────────────────────────────────────────────────────────────────────
# Vently — One-command deploy to AWS EC2
# Usage: .\deploy.ps1
# ─────────────────────────────────────────────────────────────────────────────

$PEM      = "C:\Users\vaibh\Downloads\vently.pem"
$EC2      = "ubuntu@100.53.250.27"
$APP_DIR  = "/opt/vently-app"
$ROOT     = $PSScriptRoot   # directory this script lives in

# ── 1. Package ────────────────────────────────────────────────────────────────
Write-Host "`n[1/6] Packaging source..." -ForegroundColor Cyan
$zip = "$ROOT\deploy.zip"
Remove-Item $zip -ErrorAction SilentlyContinue
Compress-Archive -Path "$ROOT\vently", "$ROOT\vently-ui", "$ROOT\docker-compose.yml" `
                 -DestinationPath $zip -Force
Write-Host "     deploy.zip created." -ForegroundColor Green

# ── 2. Upload ─────────────────────────────────────────────────────────────────
Write-Host "`n[2/6] Uploading to EC2..." -ForegroundColor Cyan
ssh -i $PEM $EC2 "mkdir -p $APP_DIR"
scp -i $PEM $zip "${EC2}:${APP_DIR}/deploy.zip"
Write-Host "     Upload complete." -ForegroundColor Green

# ── 3. Unzip on EC2 ───────────────────────────────────────────────────────────
Write-Host "`n[3/6] Extracting on EC2..." -ForegroundColor Cyan
ssh -i $PEM $EC2 @"
  cd $APP_DIR
  sudo apt-get install -y unzip -q 2>/dev/null
  unzip -o deploy.zip
  rm deploy.zip
"@
Write-Host "     Extraction done." -ForegroundColor Green

# ── 4. Copy .env ──────────────────────────────────────────────────────────────
Write-Host "`n[4/6] Syncing .env..." -ForegroundColor Cyan
scp -i $PEM "$ROOT\vently\.env" "${EC2}:${APP_DIR}/vently/.env"
Write-Host "     .env synced." -ForegroundColor Green

# ── 5. Build & start containers ───────────────────────────────────────────────
Write-Host "`n[5/6] Building & starting Docker containers (may take 5-10 min)..." -ForegroundColor Cyan
ssh -i $PEM $EC2 @"
  cd $APP_DIR
  sudo docker compose down --remove-orphans
  sudo docker compose build --no-cache
  sudo docker compose up -d
  sudo docker compose ps
"@
Write-Host "     Containers started." -ForegroundColor Green

# ── 6. Reload Nginx ───────────────────────────────────────────────────────────
Write-Host "`n[6/6] Reloading Nginx..." -ForegroundColor Cyan
ssh -i $PEM $EC2 @"
  sudo cp $APP_DIR/vently/nginx.conf /etc/nginx/sites-available/vently
  sudo ln -sf /etc/nginx/sites-available/vently /etc/nginx/sites-enabled/vently
  sudo nginx -t && sudo systemctl reload nginx
"@
Write-Host "     Nginx reloaded." -ForegroundColor Green

# ── Done ──────────────────────────────────────────────────────────────────────
Remove-Item $zip -ErrorAction SilentlyContinue
Write-Host "`n✅ Deploy complete! Visit https://ventfly.com" -ForegroundColor Green
