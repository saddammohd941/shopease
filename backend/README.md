# ShopEase Backend

This is the backend for the ShopEase application, built with Java Servlets and deployed on Apache Tomcat.

## Setup

1. **Install Dependencies**:
   - Java 11
   - Maven
   - Apache Tomcat 10
   - PostgreSQL

2. **Build**:
   ```bash
   mvn clean package
   ```

3. **Deploy**:
   - Copy `target/shopease.war` to Tomcat's `webapps/` directory.
   - Start Tomcat: `sudo systemctl start tomcat`

4. **Database**:
   - Create a PostgreSQL database named `shopease`.
   - Update `resources/hibernate.cfg.xml` with your database credentials.

## API Endpoints
- `GET /api/hello`: Test endpoint.
- `GET /api/products?page=X`: Fetch products with pagination.
- `POST /api/auth/signup`: Register a new user.
- `POST /api/auth/signin`: Login and get JWT.
- `POST /api/payment/create-checkout-session`: Initiate Stripe payment.
- `POST /api/support/contact`: Submit support request.
