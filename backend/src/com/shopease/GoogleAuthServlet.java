```java
package com.shopease;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.Collections;
import java.util.Date;

@WebServlet("/api/auth/google/callback")
public class GoogleAuthCallbackServlet extends HttpServlet {
    private static final String CLIENT_ID = "your_actual_client_id"; // Replace with your Google OAuth Client ID
    private static final String CLIENT_SECRET = "your_actual_client_secret"; // Replace with your Google OAuth Client Secret
    private static final String REDIRECT_URI = "http://localhost:8080/shopease/api/auth/google/callback";
    private static final String JWT_SECRET = "your_jwt_secret_key"; // Replace with a secure key (store in env/config)

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String code = req.getParameter("code");
            if (code == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing authorization code");
                return;
            }

            // Exchange authorization code for tokens
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    CLIENT_ID,
                    CLIENT_SECRET,
                    code,
                    REDIRECT_URI
            ).execute();

            String idTokenString = tokenResponse.getIdToken();
            if (idTokenString == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing ID token");
                return;
            }

            // Verify Google ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid ID token");
                return;
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            String email = payload.getEmail();

            // Generate JWT (replace with your UserService to store/check user)
            String jwt = Jwts.builder()
                    .setSubject(email)
                    .claim("userId", userId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiry
                    .signWith(SignatureAlgorithm.HS256, JWT_SECRET.getBytes())
                    .compact();

            // Redirect to products page with JWT
            resp.sendRedirect("/products.html?token=" + jwt);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failed: " + e.getMessage());
        }
    }
}
```

**Changes Made**:
- Added token verification using `GoogleIdTokenVerifier`.
- Implemented JWT generation with `jjwt` library.
- Added error handling for missing code or invalid tokens.
- Replaced placeholder `jwt` with proper generation.
- Kept `CLIENT_ID` and `CLIENT_SECRET` placeholders (to be updated).

**Action**:
- Replace `your_actual_client_id` and `your_actual_client_secret` with your Google OAuth credentials from `https://console.developers.google.com`.
- Replace `your_jwt_secret_key` with a secure key (e.g., generate with `openssl rand -base64 32` and store securely).
- Save in `shopease/backend/src/com/shopease/GoogleAuthCallbackServlet.java`.

#### 2. Create `GoogleAuthServlet.java`
You need a servlet to handle `/api/auth/google` to initiate the OAuth flow. Create a new file:

<xaiArtifact artifact_id="0d87834d-c146-4df6-a097-7607adf22bef" artifact_version_id="f752938b-23bc-4f9f-8263-90f98a434d58" title="GoogleAuthServlet.java" contentType="text/x-java">
```java
package com.shopease;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@WebServlet("/api/auth/google")
public class GoogleAuthServlet extends HttpServlet {
    private static final String CLIENT_ID = "your_actual_client_id"; // Replace with your Google OAuth Client ID
    private static final String CLIENT_SECRET = "your_actual_client_secret"; // Replace with your Google OAuth Client Secret
    private static final String REDIRECT_URI = "http://localhost:8080/shopease/api/auth/google/callback";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
            details.setClientId(CLIENT_ID);
            details.setClientSecret(CLIENT_SECRET);
            GoogleClientSecrets secrets = new GoogleClientSecrets().setWeb(details);
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    secrets,
                    Collections.singleton("https://www.googleapis.com/auth/userinfo.email")
            ).build();
            String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
            resp.sendRedirect(url);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to initiate Google OAuth: " + e.getMessage());
        }
    }
}
```

**Action**:
- Save in `shopease/backend/src/com/shopease/GoogleAuthServlet.java`.
- Update `CLIENT_ID` and `CLIENT_SECRET` with the same values as in `GoogleAuthCallbackServlet.java`.

#### 3. Update `pom.xml`
Ensure the required dependencies are in `shopease/backend/pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.google.api-client</groupId>
        <artifactId>google-api-client</artifactId>
        <version>2.4.0</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.9.1</version>
    </dependency>
    <!-- Existing dependencies (e.g., javax.servlet, hibernate) -->
</dependencies>
```

**Action**:
- Update `pom.xml`.
- Rebuild backend:
  ```bash
  cd shopease/backend
  mvn clean package
  cp target/shopease.war /c/tomcat/webapps/
  /c/tomcat/bin/startup.bat
  ```

#### 4. Store JWT in Frontend
Update `index.html`, `signup.html`, and `products.html` to store the JWT from the Google OAuth redirect. Add this to the `<script>` section of each file:

```javascript
// Handle JWT from Google OAuth redirect
window.onload = () => {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    if (token) {
        localStorage.setItem('token', token);
        window.history.replaceState({}, document.title, window.location.pathname); // Clean URL
    }
    // Existing onload logic (e.g., loadProducts in products.html)
};
```

**Action**:
- Add this code to `index.html`, `signup.html`, and `products.html` in `shopease/frontend/public/`.

### Step 2: Verify Frontend Files
The `Cannot GET /signup.html`, `/products.html`, and `/support.html` errors are resolved by the provided files in the previous response. Ensure they’re in place:

- **Files**:
  - `shopease/frontend/public/index.html`
  - `shopease/frontend/public/signup.html`
  - `shopease/frontend/public/products.html`
  - `shopease/frontend/public/support.html`
- **Assets**:
  - `shopease/frontend/public/logo.png`
  - `shopease/frontend/public/favicon.png`
  - `shopease/frontend/public/assets/placeholder.jpg`
- **Verify**:
  ```bash
  ls shopease/frontend/public/
  ```
  Expected: `index.html signup.html products.html support.html logo.png favicon.png assets/`

- **Logo Fix**:
  If the logo still doesn’t work:
  ```bash
  cd shopease/frontend/public
  curl -o logo.png https://via.placeholder.com/150x50/1e40af/ffffff?text=ShopEase
  curl -o favicon.png https://via.placeholder.com/32x32/1e40af/ffffff?text=S
  mkdir -p assets
  curl -o assets/placeholder.jpg https://via.placeholder.com/200x200/cccccc/666666?text=Product
  ```

### Step 3: Test Google OAuth
1. **Start Backend**:
   ```bash
   /c/tomcat/bin/startup.bat
   ```
   Verify: `http://localhost:8080/shopease`.

2. **Start Frontend**:
   ```bash
   cd shopease/frontend
   npm start
   ```
   Access: `http://localhost:3000`.

3. **Test OAuth**:
   - Open `http://localhost:3000`.
   - Click "Google Sign In".
   - You should be redirected to Google’s login page, then back to `/products.html?token=...`.
   - Check browser console (F12 > Console) for errors.
   - Verify `localStorage.getItem('token')` in the console.

### Step 4: Nginx Deployment (Optional)
If deploying with Nginx:
- Build frontend:
  ```bash
  cd shopease/frontend
  npm run build
  cp -r build/* /c/nginx/html/shopease/
  ```
- Update `C:/nginx/conf/shopease.conf`:
  ```nginx
  server {
      listen 80;
      server_name localhost;
      root C:/nginx/html/shopease;
      index index.html;
      location / {
          try_files $uri $uri/ /index.html;
      }
      location /api/ {
          proxy_pass http://localhost:8080/shopease/api/;
          proxy_set_header Host $host;
          proxy_set_header X-Real-IP $remote_addr;
      }
  }
  ```
- Start Nginx: `C:/nginx/nginx.exe`.

### Step 5: UI Enhancements
Your `index.html`, `signup.html`, `products.html`, and `support.html` already include:
- **Branding**: Blue gradient (#1e3a8a to #3b82f6), Inter font, logo, favicon.
- **UX**: Search bar, category filter, password toggle, toast notifications, theme toggle.
- **Customization**: Light/dark mode, responsive design, animated product cards.

To further customize:
- **Logo Animation**: Already included hover effect in `index.html` (`transform: scale(1.1)`).
- **Add Social Logins**: Want Facebook or Twitter login? I can add endpoints.
- **Colors**: Prefer a different palette (e.g., green, purple)?
- **Features**: Need a cart icon in the header or product filters?

### Step 6: Fix "Failed to load products"
If `products.html` still shows "Failed to load products":
- **Test API**:
  ```bash
  curl -H "Authorization: Bearer $(cat ~/.shopease_token)" http://localhost:8080/shopease/api/products?page=1
  ```
- **Database**:
  - Ensure `seed.sql` is run:
    ```bash
    psql -U postgres -d shopease -f shopease/backend/resources/seed.sql
    ```
  - Verify: `psql -U postgres -d shopease -c "SELECT * FROM product;"`.
- **Backend Logs**: `cat /c/tomcat/logs/catalina.out`.

### If Issues Persist
- **Console Errors**: Open `http://localhost:3000`, press F12, check Console/Network tabs.
- **Backend Errors**: Check Tomcat logs.
- **UI Feedback**: Specify if the UI needs more polish (e.g., animations, layout).
- **Specific Errors**: Share exact error messages or screenshots.

Let me know if the `/api/auth/google` error is resolved or if you need further UI customizations!