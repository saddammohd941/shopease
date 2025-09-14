package com.shopease;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.entity.Product;
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

@WebServlet("/api/products/*")
public class ProductServlet extends HttpServlet {

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
            if (path == null || "/".equals(path)) {
                String pageStr = req.getParameter("page");
                String search = req.getParameter("search");
                String category = req.getParameter("category");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = 10;

                String query = "FROM Product";
                if (search != null && !search.isEmpty()) {
                    query += " WHERE name LIKE :search OR description LIKE :search";
                }
                if (category != null && !category.isEmpty()) {
                    query += (query.contains("WHERE") ? " AND" : " WHERE") + " category = :category";
                }

                List<Product> products = session.createQuery(query, Product.class)
                        .setParameter("search", search != null ? "%" + search + "%" : null)
                        .setParameter("category", category)
                        .setFirstResult((page - 1) * pageSize)
                        .setMaxResults(pageSize)
                        .list();

                long total = session.createQuery("SELECT COUNT(*) FROM Product", Long.class).uniqueResult();
                resp.getWriter().write(mapper.writeValueAsString(new PageResponse(products, page, (int) Math.ceil((double) total / pageSize)));
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    public static class PageResponse {
        public List<Product> products;
        public int page;
        public int totalPages;

        public PageResponse(List<Product> products, int page, int totalPages) {
            this.products = products;
            this.page = page;
            this.totalPages = totalPages;
        }
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
