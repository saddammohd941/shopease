package com.shopease;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/payment/webhook")
public class StripeWebhookServlet extends HttpServlet {

    private static final String WEBHOOK_SECRET = "your-stripe-webhook-secret";

    @Override
    public void init() {
        Stripe.apiKey = "your-stripe-secret-key";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String payload = getBody(req);
        String sigHeader = req.getHeader("Stripe-Signature");
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, WEBHOOK_SECRET);
        } catch (SignatureVerificationException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\": \"Invalid webhook signature\"}");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) mapper.treeToValue(event.getDataObjectDeserializer().deserialize(null), Session.class);
                // TODO: Save order, clear cart, send email receipt
                System.out.println("Payment completed for session: " + session.getId());
                break;
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }

        resp.setStatus(200);
    }

    private String getBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = req.getReader();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
