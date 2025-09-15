# ShopEase: Full-Stack E-Commerce Platform

ShopEase is a complete e-commerce application designed for seamless online shopping experiences. It features a Java-based backend with Servlets, Hibernate ORM, PostgreSQL database, JWT authentication, Stripe payments, and Google OAuth integration. The frontend is built with React, HTML/CSS (using Tailwind for styling), and JavaScript, providing responsive pages for product browsing, user auth, support, FAQ, privacy policy, and terms of service. Deployment is supported via Tomcat for the backend and Nginx for the frontend, with scripts for setup and deployment.

This project includes admin tools for product management, cart functionality, order processing, and more. It's suitable for local development or production deployment on Windows/Linux.

[![Java](https://img.shields.io/badge/Java-11-blue.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.3.2-orange.svg)](https://maven.apache.org/)
[![Tomcat](https://img.shields.io/badge/Tomcat-10-red.svg)](https://tomcat.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.3-brightgreen.svg)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3-purple.svg)](https://tailwindcss.com/)

## Project Structure
- **backend/**: Java Servlet backend.
  - `pom.xml`: Maven configuration with dependencies (Jakarta Servlet, Jackson, JWT, Stripe, PostgreSQL, Hibernate, jBCrypt, Google API).
  - `resources/hibernate.cfg.xml`: Hibernate configuration for PostgreSQL.
  - `src/com/shopease/`: Servlets for admin, auth, cart, products, payments, support.
  - `src/com/shopease/entity/`: Entity classes (User, Product, Order, OrderItem, Review).
- **frontend/**: React + static HTML frontend.
  - `public/`: Static pages (index.html, products.html, support.html, faq.html, privacy.html, terms.html).
  - `src/App.css`: Global styling with light/dark themes.
  - `src/App.jsx`: Main React app with routing, state management, API calls for products/cart/auth.
  - Features infinite scrolling, modals for signup/signin/cart, theme toggle.
- **deployment/**: Configs and scripts.
  - `apache/shopease.conf`: Optional Apache config.
  - `nginx/shopease.conf`: Nginx config for static serving and API proxy.
  - `scripts/deploy.sh`: Bash script for build/deploy (Windows-adjusted).
  - `scripts/setup.sh`: Setup instructions for tools.
  - `ssl/README.md`: SSL setup with Certbot.
  - `systemd/tomcat.service`: Systemd service for Tomcat.

## Features
- **Authentication**: Signup/signin with email/password (BCrypt hashing, JWT tokens); Google OAuth.
- **Products**: Listing with pagination/search/categories; Admin CRUD.
- **Cart**: Add items, view, clear (session-based).
- **Orders/Reviews**: Entities for orders, items, reviews (not fully implemented in servlets).
- **Payments**: Stripe checkout sessions.
- **Support/Static Pages**: Contact form; Accordion-style FAQ, privacy, terms pages with toggleable sections.
- **Frontend UI**: Responsive design, hero video, product cards, modals, toasts, dark/light mode.
- **Security**: JWT for auth, Hibernate for SQL injection prevention.
- **Deployment**: Tomcat WAR + Nginx static/proxy; SSL support.

## Prerequisites
- Java 11 (JDK)
- Maven
- Apache Tomcat 10
- PostgreSQL
- Nginx (for frontend)
- Node.js (for React dev server, optional)
- Git

## Setup and Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/shopease.git  # Replace with your repo URL
cd shopease
```

### 2. Backend Setup
1. Navigate to backend:
   ```bash
   cd backend
   ```
2. Install dependencies and build:
   ```bash
   mvn clean install
   mvn clean package
   ```
3. Database configuration:
   - Create database: `psql -U postgres -c "CREATE DATABASE shopease;"`
   - Update `resources/hibernate.cfg.xml` with DB URL, username, password.
   - (Optional) Seed database: Create a `seed.sql` with tables from entities and sample data, then `psql -U postgres -d shopease -f seed.sql`.
4. Configure secrets:
   - Replace `JWT_SECRET` in servlets with an environment variable or secure key.
   - Add Stripe/Google API keys in relevant servlets (e.g., PaymentServlet, GoogleAuthServlet—not fully shown in files).
5. Deploy WAR:
   - Copy `target/shopease.war` to Tomcat's `webapps/`.
   - Start Tomcat: `sudo systemctl start tomcat` (or `bin/startup.sh`).

Test API: `curl http://localhost:8080/shopease/api/hello`

### 3. Frontend Setup
1. Navigate to frontend:
   ```bash
   cd ../frontend
   ```
2. Install dependencies (if using React build):
   ```bash
   npm install
   ```
3. Development server:
   ```bash
   npm start  # Runs at http://localhost:3000
   ```
4. Production: Serve static files from `public/` via Nginx (see deployment below).
5. Assets: Place images in `public/assets/` (e.g., logo.png, placeholder.jpg).

Test: Open `http://localhost:3000/index.html` for login page.

### 4. Database Schema
Use Hibernate's `hbm2ddl.auto=update` to auto-generate tables from entities. Manual schema:
- **users**: id (PK), email, password_hash, role (USER/ADMIN).
- **product**: id (PK), name, price, image, description, category.
- **orders**: id (PK), user_id (FK), total, order_date, status.
- **order_items**: id (PK), order_id (FK), product_id (FK), quantity, price.
- **reviews**: id (PK), product_id (FK), user_id (FK), rating, comment.

### 5. Deployment
- **Local**:
  - Run `deployment/scripts/setup.sh` for tool installation instructions.
  - Run `deployment/scripts/deploy.sh` for build/deploy.
  - Configure Nginx (`deployment/nginx/shopease.conf`): Proxy /api/ to Tomcat; serve frontend from root.
  - Start services: Tomcat for backend, Nginx for frontend.
- **SSL**: Follow `deployment/ssl/README.md` for Certbot.
- **Systemd**: Use `deployment/systemd/tomcat.service` for Tomcat service.
- **Cloud**: Deploy backend WAR to Heroku/AWS; frontend to Vercel/Netlify; DB to Supabase/RDS.

Test full app: `http://localhost/` (frontend proxies API to backend).

## API Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/hello` | Test | No |
| GET | `/api/products?page=X` | Paginated products | No |
| POST | `/api/auth/signup` | User registration | No |
| POST | `/api/auth/signin` | Login (returns JWT) | No |
| POST | `/api/cart/add` | Add to cart | JWT |
| GET | `/api/cart` | Get cart | JWT |
| DELETE | `/api/cart/clear` | Clear cart | JWT |
| POST | `/api/payment/create-checkout-session` | Stripe session | JWT |
| POST | `/api/support/contact` | Support request | No |
| POST | `/api/admin/products` | Add product | Admin JWT |
| PUT | `/api/admin/products/{id}` | Update product | Admin JWT |
| DELETE | `/api/admin/products/{id}` | Delete product | Admin JWT |

## Frontend Pages
- `index.html`: Login/signup.
- `products.html`: Product listing.
- `support.html`: Contact form.
- `faq.html`: Accordion FAQs.
- `privacy.html`: Accordion privacy policy.
- `terms.html`: Accordion terms of service.

## Troubleshooting
- **Maven Build Fails**: Check Java version (`java -version` = 11); run `mvn -v`.
- **Hibernate Errors**: Verify DB creds/url in `hibernate.cfg.xml`; set `show_sql=true` for debug.
- **JWT Invalid**: Ensure `JWT_SECRET` matches; token expiry is 24h.
- **Stripe/Google Issues**: Add keys in servlets; test with dev accounts.
- **Frontend API Calls Fail**: Check proxy in Nginx; CORS if needed (add in servlets).
- **Accordion Not Toggling**: Check JS console for errors; ensure no CSS overrides.

## Contributing
Fork, branch, PR. Follow code style (e.g., Java conventions, React hooks).

## License
MIT License (add LICENSE file).

For questions, open an issue or email support@shopease.com. Built on September 15, 2025.
