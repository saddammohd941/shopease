package com.shopease;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.entity.Product;
import com.shopease.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/admin/products/*")
public class AdminServlet extends HttpServlet {

    private SessionFactory sessionFactory;
    private static final String JWT_SECRET = "your_jwt_secret_key"; // Replace with a secure key, e.g., from environment variable

    @Override
    public void init() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write("{\"error\": \"Admin access required\"}");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Product product = mapper.readValue(req.getInputStream(), Product.class);
            session.save(product);
            session.getTransaction().commit();
            resp.getWriter().write("{\"message\": \"Product added\"}");
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write("{\"error\": \"Admin access required\"}");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");

        String path = req.getPathInfo();
        Long id = Long.parseLong(path.substring(1));

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Product product = session.get(Product.class, id);
            if (product != null) {
                mapper.readerForUpdating(product).readValue(req.getInputStream());
                session.update(product);
                session.getTransaction().commit();
                resp.getWriter().write("{\"message\": \"Product updated\"}");
            } else {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\": \"Product not found\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write("{\"error\": \"Admin access required\"}");
            return;
        }

        String path = req.getPathInfo();
        Long id = Long.parseLong(path.substring(1));

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Product product = session.get(Product.class, id);
            if (product != null) {
                session.delete(product);
                session.getTransaction().commit();
                resp.getWriter().write("{\"message\": \"Product deleted\"}");
            } else {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\": \"Product not found\"}");
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private boolean isAdmin(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;

        String token = authHeader.substring(7);
        try {
            String email = Jwts.parserBuilder().setSigningKey(JWT_SECRET.getBytes()).build().parseClaimsJws(token).getBody().getSubject();
            try (Session session = sessionFactory.openSession()) {
                User user = session.createQuery("FROM User WHERE email = :email", User.class)
                        .setParameter("email", email)
                        .uniqueResult();
                return user != null && "ADMIN".equals(user.getRole());
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}