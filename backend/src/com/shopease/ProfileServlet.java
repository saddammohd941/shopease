package com.shopease;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.entity.Order;
import com.shopease.entity.User;
import io.jsonwebtoken.Jwts;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/profile/*")
public class ProfileServlet extends HttpServlet {

    private SessionFactory sessionFactory;
    private static final String JWT_SECRET = "your_jwt_secret_key";

    @Override
    public void init() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        String token = authHeader.substring(7);
        String email;
        try {
            email = Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\": \"Invalid token\"}");
            return;
        }

        try (Session session = sessionFactory.openSession()) {
            User user = session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();

            if (user == null) {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\": \"User not found\"}");
                return;
            }

            if ("/orders".equals(path)) {
                List<Order> orders = session.createQuery("FROM Order WHERE userId = :userId", Order.class)
                        .setParameter("userId", user.getId())
                        .list();
                resp.getWriter().write(mapper.writeValueAsString(orders));
            } else {
                resp.getWriter().write(mapper.writeValueAsString(user));
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
