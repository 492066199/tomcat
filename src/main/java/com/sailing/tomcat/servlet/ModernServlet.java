package com.sailing.tomcat.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class ModernServlet extends HttpServlet {

    public void init(ServletConfig config) {
        System.out.println("ModernServlet -- init");
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {

        InputStream inputStream = request.getInputStream();


        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Modern Servlet</title>");
        out.println("</head>");
        out.println("<body>");

        out.println("<h2>Headers</h2");
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            out.println("<br>" + header + " : " + request.getHeader(header));
        }

        out.println("<br><h2>Method</h2");
        out.println("<br>" + request.getMethod());

        out.println("<br><h2>Parameters</h2");
        Enumeration<String> parameters = request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String parameter = parameters.nextElement();
            out.println("<br>" + parameter + " : " + request.getParameter(parameter));
        }

        out.println("<br><h2>Query String</h2");
        out.println("<br>" + request.getQueryString());

        out.println("<br><h2>Request URI</h2");
        out.println("<br>" + request.getRequestURI());

        out.println("</body>");
        out.println("</html>");

    }
}