#!/bin/bash

# ShopEase Server Setup Script for Windows
# Run in Git Bash; assumes manual installation of tools

echo "Install Java 11 from https://adoptium.net/temurin/releases/ (JDK 11)"
echo "Install Maven from https://maven.apache.org/download.cgi"
echo "Install Tomcat from https://tomcat.apache.org/download-10.cgi (extract to C:/tomcat)"
echo "Install PostgreSQL from https://www.postgresql.org/download/windows/"
echo "Install Nginx from https://nginx.org/en/download.html (extract to C:/nginx)"

echo "Add to PATH (Environment Variables):"
echo " - C:/Program Files/Java/jdk-11/bin"
echo " - C:/maven/bin"
echo " - C:/tomcat/bin"
echo " - C:/Program Files/PostgreSQL/16/bin"
echo " - C:/nginx"

echo "Start PostgreSQL: pg_ctl -D \"C:/Program Files/PostgreSQL/16/data\" start"
echo "Create PostgreSQL database and user:"
echo "psql -U postgres -c \"CREATE USER shopease WITH PASSWORD 'yourpassword';\""
echo "psql -U postgres -c \"CREATE DATABASE shopease OWNER shopease;\""

echo "Setup complete! Run deploy.sh to deploy the application."