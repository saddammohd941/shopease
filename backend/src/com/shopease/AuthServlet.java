package com.shopease;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.mindrot.jbcrypt.BCrypt;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Date;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private SessionFactory sessionFactory;
    private static final String JWT_SECRET = "your_jwt_secret_key"; // Replace with a secure key, e.g., from environment variable

    @Override
    public void init() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            if ("/signup".equals(path)) {
                User user = mapper.readValue(req.getInputStream(), User.class);
                user.setPasswordHash(BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt()));
                session.save(user);
                session.getTransaction().commit();
                resp.getWriter().write("{\"message\": \"Signup successful\"}");
            } else if ("/signin".equals(path)) {
                User input = mapper.readValue(req.getInputStream(), User.class);
                User user = session.createQuery("FROM User WHERE email = :email", User.class)
                        .setParameter("email", input.getEmail())
                        .uniqueResult();
                if (user != null && BCrypt.checkpw(input.getPasswordHash(), user.getPasswordHash())) {
                    String jwt = Jwts.builder()
                            .setSubject(user.getEmail())
                            .setIssuedAt(new Date())
                            .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                            .signWith(SignatureAlgorithm.HS256, JWT_SECRET.getBytes())
                            .compact();
                    resp.getWriter().write("{\"token\": \"" + jwt + "\"}");
                } else {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\": \"Invalid credentials\"}");
                }
                session.getTransaction().commit();
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}