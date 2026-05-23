#!/bin/bash
# ============================================================
# Shoppy Backend — VM Startup Script
# Runs automatically when the VM first boots via Terraform
# ============================================================

set -e  # Exit immediately on any error
exec > /var/log/shoppy-startup.log 2>&1  # Log everything

echo "========================================"
echo " Shoppy Backend Startup Script"
echo " $(date)"
echo "========================================"

# ── Read metadata passed from Terraform ──────────────────
METADATA_URL="http://metadata.google.internal/computeMetadata/v1/instance/attributes"
METADATA_HEADER="Metadata-Flavor: Google"

DB_USER=$(curl -sf -H "$METADATA_HEADER" "$METADATA_URL/DB_USER")
DB_PASSWORD=$(curl -sf -H "$METADATA_HEADER" "$METADATA_URL/DB_PASSWORD")
DB_NAME=$(curl -sf -H "$METADATA_HEADER" "$METADATA_URL/DB_NAME")
REPO_URL=$(curl -sf -H "$METADATA_HEADER" "$METADATA_URL/REPO_URL")
GITHUB_TOKEN=$(curl -sf -H "$METADATA_HEADER" "$METADATA_URL/GITHUB_TOKEN")
GITHUB_BRANCH=$(curl -sf -H "$METADATA_HEADER" "$METADATA_URL/GITHUB_BRANCH")

echo "Config loaded: DB=$DB_NAME, REPO=$REPO_URL, BRANCH=$GITHUB_BRANCH"

# ── System update ─────────────────────────────────────────
echo "[1/6] Updating system packages..."
apt-get update -y && apt-get upgrade -y

# ── Install dependencies ──────────────────────────────────
echo "[2/6] Installing Git, Java 21, Maven, Docker..."
apt-get install -y git curl ca-certificates gnupg lsb-release

# Java 21
apt-get install -y openjdk-21-jdk
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

# Maven
apt-get install -y maven

# Docker Engine
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
  > /etc/apt/sources.list.d/docker.list
apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

systemctl enable docker
systemctl start docker

echo "Java version: $(java -version 2>&1)"
echo "Maven version: $(mvn -version 2>&1)"
echo "Docker version: $(docker --version)"

# ── Clone repository ──────────────────────────────────────
echo "[3/6] Cloning repository..."
APP_DIR="/opt/shoppy"
rm -rf "$APP_DIR"

# Handle private repository authentication if GITHUB_TOKEN is provided
if [ -n "$GITHUB_TOKEN" ]; then
  CLONE_URL=$(echo "$REPO_URL" | sed "s|https://|https://${GITHUB_TOKEN}@|")
else
  CLONE_URL="$REPO_URL"
fi

# Clone specific branch if specified
if [ -n "$GITHUB_BRANCH" ]; then
  git clone -b "$GITHUB_BRANCH" "$CLONE_URL" "$APP_DIR"
else
  git clone "$CLONE_URL" "$APP_DIR"
fi

cd "$APP_DIR/shoppy-backend"

# ── Start PostgreSQL via Docker ───────────────────────────
echo "[4/6] Starting PostgreSQL..."
docker run -d \
  --name shoppy_postgres \
  --restart always \
  -e POSTGRES_USER="$DB_USER" \
  -e POSTGRES_PASSWORD="$DB_PASSWORD" \
  -e POSTGRES_DB="$DB_NAME" \
  -p 5432:5432 \
  -v shoppy_postgres_data:/var/lib/postgresql/data \
  postgres:15-alpine

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker exec shoppy_postgres pg_isready -U "$DB_USER" 2>/dev/null; do
  echo "  PostgreSQL not ready yet, retrying in 2s..."
  sleep 2
done
echo "PostgreSQL is ready!"

# ── Override application.yml with production config ───────
echo "[5/6] Writing production application config..."
cat > src/main/resources/application.yml <<EOF
spring:
  application:
    name: shoppy-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080
EOF

# ── Build and run the Spring Boot app ────────────────────
echo "[6/6] Building and starting Shoppy backend..."
mvn clean package -DskipTests

# Create a systemd service so the app auto-restarts on reboot
# NOTE: We expand the variables NOW (at write time) using a non-quoted heredoc
JAR_PATH="$APP_DIR/shoppy-backend/target/shoppy-backend-0.0.1-SNAPSHOT.jar"
WORK_DIR="$APP_DIR/shoppy-backend"

cat > /etc/systemd/system/shoppy.service <<EOF
[Unit]
Description=Shoppy Spring Boot Backend
After=network.target docker.service
Requires=docker.service

[Service]
Type=simple
WorkingDirectory=${WORK_DIR}
ExecStart=/usr/lib/jvm/java-21-openjdk-amd64/bin/java -jar ${JAR_PATH}
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=shoppy
Environment="JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64"

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable shoppy
systemctl start shoppy

echo "========================================"
echo " Shoppy backend started successfully!"
echo " API available at: http://$(curl -sf http://metadata.google.internal/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip -H 'Metadata-Flavor: Google'):8080/api/v1/products"
echo " Logs: journalctl -u shoppy -f"
echo "========================================"
