package com.shopease;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;

// Assume email sending library or service (e.g., JavaMail)

@WebServlet("/api/support/contact")
public class SupportServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        // Assume body: { "name": "", "email": "", "message": "" }

        // TODO: Implement email sending logic, e.g., using JavaMail or external service like SendGrid

        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\": \"Support request sent successfully\"}");
    }
}
