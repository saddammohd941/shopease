package com.shopease;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.entity.Product;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/payment/*")
public class PaymentServlet extends HttpServlet {

    private SessionFactory sessionFactory;

    @Override
    public void init() {
        Stripe.apiKey = "your-stripe-secret-key";
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");

        if ("/create-checkout-session".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session == null) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\": \"Unauthorized\"}");
                return;
            }

            List<CartServlet.CartItem> cart = (List<CartServlet.CartItem>) session.getAttribute("cart");
            if (cart == null || cart.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\": \"Cart is empty\"}");
                return;
            }

            try {
                List<SessionCreateParams.LineItem> lineItems = cart.stream().map(item -> 
                    SessionCreateParams.LineItem.builder()
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount((long) (item.getPrice() * 100))
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getName())
                                        .build()
                                )
                                .build()
                        )
                        .setQuantity((long) item.getQuantity())
                        .build()
                ).collect(Collectors.toList());

                SessionCreateParams params = SessionCreateParams.builder()
                        .addAllLineItems(lineItems)
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://your-domain.com/success")
                        .setCancelUrl("http://your-domain.com/cancel")
                        .build();

                Session stripeSession = Session.create(params);
                resp.getWriter().write("{\"id\": \"" + stripeSession.getId() + "\"}");
            } catch (Exception e) {
                resp.setStatus(500);
                resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    public void destroy() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
