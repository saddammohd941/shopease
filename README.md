# ShopEase

ShopEase is an e-commerce application with a Java Servlet backend and a static frontend, deployed using Tomcat and Nginx.

## Directory Structure
- `backend/`: Java Servlets, Hibernate for database, Maven build.
- `frontend/`: HTML, CSS, JavaScript for the UI.
- `deployment/`: Scripts and configs for server setup and deployment.

## Setup
1. Run `deployment/scripts/setup.sh` to install dependencies and configure the server.
2. Run `deployment/scripts/deploy.sh` to build and deploy the app.
3. Access at `http://your-domain.com`.

## Features
- Product listing with infinite scrolling
- User signup and signin with JWT
- Stripe payment integration
- Support contact form
