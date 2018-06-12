package com.sailing.tomcat.processor;

import com.sailing.tomcat.servlet.HttpRequest;
import com.sailing.tomcat.servlet.HttpResponse;
import com.sailing.tomcat.io.SocketInputStream;
import com.sailing.tomcat.connector.HttpConnector;
import com.sailing.tomcat.http.HttpHeader;
import com.sailing.tomcat.http.HttpRequestLine;
import com.sailing.tomcat.util.Constants;
import com.sailing.tomcat.util.RequestUtil;
import com.sailing.tomcat.util.StringManager;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * yangyang 2018-06-07
 */
public class HttpProcessor {
    protected final StringManager sm = StringManager.getManager(Constants.Package);
    /**
     * The HttpConnector with which this processor is associated.
     */
    private HttpConnector connector = null;
    HttpRequest request = null;
    HttpResponse response = null;

    private HttpRequestLine requestLine = new HttpRequestLine();

    public HttpProcessor(HttpConnector httpConnector) {
        this.connector = httpConnector;
    }

    public void process(Socket socket) {
        SocketInputStream input = null;
        OutputStream output = null;
        try {
            input = new SocketInputStream(socket.getInputStream(), 2048);
            output = socket.getOutputStream();

            // create HttpRequest object and parse
            request = new HttpRequest(input);

            // create HttpResponse object
            response = new HttpResponse(output);

            response.setRequest(request);
            response.setHeader("Server", "Sailing Servlet Container");

            parseRequest(input, output);
            parseHeaders(input);
            //check if this is a request for a servlet or a static resource
            //a request for a servlet begins with "/servlet/"
            if (request.getRequestURI().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
                response.finishResponse();
            } else {
                StaticResourceProcessor processor = new
                        StaticResourceProcessor();
                processor.process(request, response);
            }
            // Close the socket
            socket.close();
            // no shutdown for this application
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //private for headers
    private void parseHeaders(SocketInputStream input) throws IOException, ServletException {
        while (true) {
            HttpHeader header = new HttpHeader();
            // Read the next header
            input.readHeader(header);

            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException
                            (sm.getString("httpProcessor.parseHeaders.colon"));
                }
            }

            String name = new String(header.name, 0, header.nameEnd);
            String value = new String(header.value, 0, header.valueEnd);

            request.addHeader(name, value);
            if (name.equals("cookie")) {
                // process cookies here
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals("jsessionid")) {
                        // Override anything requested in the URL
                        if (!request.isRequestedSessionIdFromCookie()) {
                            // Accept only the first session id cookie
                            request.setRequestedSessionId(cookies[i].getValue());
                            request.setRequestedSessionCookie(true);
                            request.setRequestedSessionURL(false);
                        }
                    }
                    request.addCookie(cookies[i]);
                }
            } else if (name.equals("content-length")) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException(sm.getString(
                            "httpProcessor.parseHeaders.contentLength"));
                }
                request.setContentLength(n);
            } else if (name.equals("content-type")) {
                request.setContentType(value);
            }
        }
    }

    //private for request
    private void parseRequest(SocketInputStream input, OutputStream output) throws IOException, ServletException {
        // Parse the incoming request line
        input.readRequestLine(requestLine);
        String method = new String(requestLine.method, 0, requestLine.methodEnd);
        String uri = null;
        String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

        // Validate the incoming request line
        if (method.length () < 1) {
            throw new ServletException("Missing HTTP request method");
        }else if (requestLine.uriEnd < 1) {
            throw new ServletException("Missing HTTP request URI");
        }

        // Parse any query parameters out of the request URI
        int question = requestLine.indexOf("?");
        if (question >= 0) {
            request.setQueryString(new String(requestLine.uri, question + 1,
                    requestLine.uriEnd - question - 1));
            uri = new String(requestLine.uri, 0, question);
        }
        else {
            request.setQueryString(null);
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
        }


        // Checking for an absolute URI (with the HTTP protocol)
        if (!uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            // Parsing out protocol and host name
            if (pos != -1) {
                pos = uri.indexOf('/', pos + 3);
                if (pos == -1) {
                    uri = "";
                }else {
                    uri = uri.substring(pos);
                }
            }
        }


        // Parse any requested session ID out of the request URI
        String match = ";jsessionid=";
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            }
            else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            request.setRequestedSessionURL(true);
            uri = uri.substring(0, semicolon) + rest;
        }
        else {
            request.setRequestedSessionId(null);
            request.setRequestedSessionURL(false);
        }

        // Normalize URI (using String operations at the moment)
        String normalizedUri = RequestUtil.normalize(uri);

        // Set the corresponding request properties
        ((HttpRequest) request).setMethod(method);
        request.setProtocol(protocol);
        if (normalizedUri != null) {
            ((HttpRequest) request).setRequestURI(normalizedUri);
        } else {
            ((HttpRequest) request).setRequestURI(uri);
        }

        if (normalizedUri == null) {
            throw new ServletException("Invalid URI: " + uri + "'");
        }
    }
}
