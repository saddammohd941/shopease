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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/cart/*")
public class CartServlet extends HttpServlet {

    private SessionFactory sessionFactory;

    @Override
    public void init() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");

        HttpSession session = req.getSession();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        if ("/add".equals(path)) {
            CartItem item = mapper.readValue(req.getInputStream(), CartItem.class);
            try (Session dbSession = sessionFactory.openSession()) {
                Product product = dbSession.get(Product.class, item.getProductId());
                if (product != null) {
                    CartItem existing = cart.stream().filter(c -> c.getProductId().equals(item.getProductId())).findFirst().orElse(null);
                    if (existing != null) {
                        existing.setQuantity(existing.getQuantity() + item.getQuantity());
                    } else {
                        cart.add(new CartItem(item.getProductId(), product.getName(), product.getPrice(), item.getQuantity()));
                    }
                    resp.getWriter().write("{\"message\": \"Added to cart\"}");
                } else {
                    resp.setStatus(404);
                    resp.getWriter().write("{\"error\": \"Product not found\"}");
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
            if (cart == null) cart = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            resp.setContentType("application/json");
            resp.getWriter().write(mapper.writeValueAsString(cart));
        } else {
            resp.getWriter().write("[]");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        HttpSession session = req.getSession(false);
        if (session != null && "/clear".equals(path)) {
            session.setAttribute("cart", new ArrayList<CartItem>());
            resp.getWriter().write("{\"message\": \"Cart cleared\"}");
        }
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public static class CartItem {
        private Long productId;
        private String name;
        private Double price;
        private int quantity;

        public CartItem() {}
        public CartItem(Long productId, String name, Double price, int quantity) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
