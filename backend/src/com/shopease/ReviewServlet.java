package com.shopease;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.entity.Review;
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

@WebServlet("/api/reviews/*")
public class ReviewServlet extends HttpServlet {

    private SessionFactory sessionFactory;

    @Override
    public void init() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");

        try (Session session = sessionFactory.openSession()) {
            if (path != null && path.startsWith("/product/")) {
                Long productId = Long.parseLong(path.substring("/product/".length()));
                List<Review> reviews = session.createQuery("FROM Review WHERE productId = :productId", Review.class)
                        .setParameter("productId", productId)
                        .list();
                resp.getWriter().write(mapper.writeValueAsString(reviews));
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Review review = mapper.readValue(req.getInputStream(), Review.class);
            session.save(review);
            session.getTransaction().commit();
            resp.getWriter().write("{\"message\": \"Review added\"}");
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
