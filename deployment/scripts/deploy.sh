#!/bin/bash

# ShopEase Deployment Script for Windows
# Run from the root of the project

APP_NAME=shopease
BACKEND_DIR=backend
FRONTEND_DIR=frontend
WAR_FILE=target/${APP_NAME}.war
TOMCAT_WEBAPPS=C:/tomcat/webapps  <!-- Replace with your Tomcat webapps path -->
NGINX_ROOT=C:/nginx/html/shopease  <!-- Replace with your Nginx root path -->

echo "/shopease/frontend/public/Building backend with Maven..."
cd "$BACKEND_DIR" || exit 1
mvn clean package || { echo "❌ Maven build failed"; exit 1; }

echo "Copying WAR to Tomcat webapps directory..."
cp "$WAR_FILE" "$TOMCAT_WEBAPPS/" || { echo "❌ Failed to copy WAR"; exit 1; }

rm -rf "$NGINX_ROOT"
mkdir -p "$NGINX_ROOT"
cp -r build/* "$NGINX_ROOT/" || { echo "❌ Failed to copy frontend"; exit 1; }

echo "Restart Tomcat and Nginx manually on Windows."
echo "Frontend files remain in $FRONTEND_DIR/public/. Test at http://localhost/index.html"
echo "Start Tomcat: C:/tomcat/bin/startup.bat"
echo "Start Nginx: C:/nginx/nginx.exe"

echo "Deployment complete! Visit http://localhost"