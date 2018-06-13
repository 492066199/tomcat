package com.sailing.tomcat.processor;

import com.sailing.tomcat.io.*;
import com.sailing.tomcat.connector.HttpConnector;
import com.sailing.tomcat.http.HttpHeader;
import com.sailing.tomcat.http.HttpRequestLine;
import com.sailing.tomcat.life.Lifecycle;
import com.sailing.tomcat.life.LifecycleException;
import com.sailing.tomcat.life.LifecycleListener;
import com.sailing.tomcat.life.LifecycleSupport;
import com.sailing.tomcat.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

/**
 * yangyang 2018-06-07
 */
public class HttpProcessor implements Lifecycle, Runnable{
    protected final StringManager sm = StringManager.getManager(Constants.Package);
    public HttpProcessor(HttpConnector httpConnector, int id) {
        this.id = id;
        this.connector = httpConnector;
        this.request = (HttpRequestImpl) connector.createRequest();
        this.response = (HttpResponseImpl) connector.createResponse();
        this.serverPort = httpConnector.getPort();
        this.threadName =
                "HttpProcessor[" + connector.getPort() + "][" + id + "]";
    }



    /**
     * The actual server port for our Connector.
     */
    private int serverPort = 0;
    private void parseConnection(Socket socket)
            throws IOException, ServletException {

        if (debug >= 2)
            log("  parseConnection: address=" + socket.getInetAddress() +
                    ", port=" + connector.getPort());
        ((HttpRequestImpl) request).setInet(socket.getInetAddress());
//        if (proxyPort != 0)
//            request.setServerPort(proxyPort);
//        else
            request.setServerPort(serverPort);
        request.setSocket(socket);

    }

    /**
     * The HttpConnector with which this processor is associated.
     */
    private HttpConnector connector = null;
    private HttpRequestLine requestLine = new HttpRequestLine();
    private int id;
    private boolean keepAlive = false;
    private static final String SERVER_INFO = "sailing server (HTTP/1.1 Connector)";
    private boolean http11 = true;
    private boolean sendAck = false;
    private static final byte[] ack = (new String("HTTP/1.1 100 Continue\r\n\r\n")).getBytes();
    private HttpRequestImpl request = null;
    private HttpResponseImpl response = null;
    private int status = Constants.PROCESSOR_IDLE;
    private static final String match = ";" + "jsessionid" + "=";
    private StringParser parser = new StringParser();

    private void ackRequest(OutputStream output)
            throws IOException {
        if (sendAck)
            output.write(ack);
    }


    public void process(Socket socket) {
        boolean ok = true;
        boolean finishResponse = true;
        SocketInputStream input = null;
        OutputStream output = null;

        // Construct and initialize the objects we will need
        try {
            input = new SocketInputStream(socket.getInputStream(),
                    connector.getBufferSize());
        } catch (Exception e) {
            log("process.create :" + e.getMessage());
            ok = false;
        }

        keepAlive = true;

        while (!stopped && ok && keepAlive) {

            finishResponse = true;

            try {
                request.setStream(input);
                request.setResponse(response);
                output = socket.getOutputStream();
                response.setStream(output);
                response.setRequest(request);
                ((HttpServletResponse) response.getResponse()).setHeader
                        ("Server", SERVER_INFO);
            } catch (Exception e) {
                log("process.create:" + e.getMessage());
                ok = false;
            }

            // Parse the incoming request
            try {
                if (ok) {
                    parseConnection(socket);
                    parseRequest(input, output);
                    if (!request.getRequest().getProtocol()
                            .startsWith("HTTP/0"))
                        parseHeaders(input);
                    if (http11) {
                        // Sending a request acknowledge back to the client if
                        // requested.
                        ackRequest(output);
                        // If the protocol is HTTP/1.1, chunking is allowed.
                        if (connector.isChunkingAllowed())
                            response.setAllowChunking(true);
                    }
                }
            } catch (EOFException e) {
                // It's very likely to be a socket disconnect on either the
                // client or the server
                ok = false;
                finishResponse = false;
            } catch (ServletException e) {
                ok = false;
                try {
                    ((HttpServletResponse) response.getResponse())
                            .sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }
            } catch (InterruptedIOException e) {
                if (debug > 1) {
                    try {
                        log("process.parse:" + e.getMessage());
                        ((HttpServletResponse) response.getResponse())
                                .sendError(HttpServletResponse.SC_BAD_REQUEST);
                    } catch (Exception f) {
                        ;
                    }
                }
                ok = false;
            } catch (Exception e) {
                try {
                    log("process.parse" + e);
                    ((HttpServletResponse) response.getResponse()).sendError
                            (HttpServletResponse.SC_BAD_REQUEST);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            }

            // Ask our Container to process this request
            try {
                ((HttpServletResponse) response).setHeader
                        ("Date", FastHttpDateFormat.getCurrentDate());
                if (ok) {
                    connector.getContainer().invoke(request, response);
                }
            } catch (ServletException e) {
                log("process.invoke" + e);
                try {
                    ((HttpServletResponse) response.getResponse()).sendError
                            (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            } catch (InterruptedIOException e) {
                ok = false;
            } catch (Throwable e) {
                log("process.invoke" + e);
                try {
                    ((HttpServletResponse) response.getResponse()).sendError
                            (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (Exception f) {
                    ;
                }
                ok = false;
            }

            // Finish up the handling of the request
            if (finishResponse) {
                try {
                    response.finishResponse();
                } catch (IOException e) {
                    ok = false;
                } catch (Throwable e) {
                    log("process.invoke" + e);
                    ok = false;
                }
                try {
                    request.finishRequest();
                } catch (IOException e) {
                    ok = false;
                } catch (Throwable e) {
                    log("process.invoke"+ e);
                    ok = false;
                }
                try {
                    if (output != null)
                        output.flush();
                } catch (IOException e) {
                    ok = false;
                }
            }

            // We have to check if the connection closure has been requested
            // by the application or the response stream (in case of HTTP/1.0
            // and keep-alive).
            if ( "close".equals(response.getHeader("Connection")) ) {
                keepAlive = false;
            }

            // End of request processing
            status = Constants.PROCESSOR_IDLE;

            // Recycling the request and the response objects
            request.recycle();
            response.recycle();

        }

        try {
            shutdownInput(input);
            socket.close();
        } catch (IOException e) {
            ;
        } catch (Throwable e) {
            log("process.invoke"+ e);
        }
        socket = null;
    }

    protected void shutdownInput(InputStream input) {
        try {
            int available = input.available();
            // skip any unread (bogus) bytes
            if (available > 0) {
                input.skip(available);
            }
        } catch (Throwable e) {
            ;
        }
    }

    //private for headers
    private void parseHeaders(SocketInputStream input)
            throws IOException, ServletException {

        while (true) {

            HttpHeader header = request.allocateHeader();

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

            String value = new String(header.value, 0, header.valueEnd);
            if (debug >= 1)
                log(" Header " + new String(header.name, 0, header.nameEnd)
                        + " = " + value);

            // Set the corresponding request headers
            if (header.equals(DefaultHeaders.AUTHORIZATION_NAME)) {
                request.setAuthorization(value);
            } else if (header.equals(DefaultHeaders.ACCEPT_LANGUAGE_NAME)) {
                parseAcceptLanguage(value);
            } else if (header.equals(DefaultHeaders.COOKIE_NAME)) {
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals
                            (Globals.SESSION_COOKIE_NAME)) {
                        // Override anything requested in the URL
                        if (!request.isRequestedSessionIdFromCookie()) {
                            // Accept only the first session id cookie
                            request.setRequestedSessionId
                                    (cookies[i].getValue());
                            request.setRequestedSessionCookie(true);
                            request.setRequestedSessionURL(false);
                            if (debug >= 1)
                                log(" Requested cookie session id is " +
                                        ((HttpServletRequest) request.getRequest())
                                                .getRequestedSessionId());
                        }
                    }
                    if (debug >= 1)
                        log(" Adding cookie " + cookies[i].getName() + "=" +
                                cookies[i].getValue());
                    request.addCookie(cookies[i]);
                }
            } else if (header.equals(DefaultHeaders.CONTENT_LENGTH_NAME)) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException
                            (sm.getString
                                    ("httpProcessor.parseHeaders.contentLength"));
                }
                request.setContentLength(n);
            } else if (header.equals(DefaultHeaders.CONTENT_TYPE_NAME)) {
                request.setContentType(value);
            } else if (header.equals(DefaultHeaders.HOST_NAME)) {
                int n = value.indexOf(':');
                if (n < 0) {
                    if (connector.getScheme().equals("http")) {
                        request.setServerPort(80);
                    } else if (connector.getScheme().equals("https")) {
                        request.setServerPort(443);
                    }

                    //no proxy
//                    if (proxyName != null)
//                        request.setServerName(proxyName);
//                    else
//                        request.setServerName(value);
                } else {
                    //no proxy
//                    if (proxyName != null)
//                        request.setServerName(proxyName);
//                    else
//                        request.setServerName(value.substring(0, n).trim());
//                    if (proxyPort != 0)
//                        request.setServerPort(proxyPort);
//                    else {
                        int port = 80;
                        try {
                            port =
                                    Integer.parseInt(value.substring(n+1).trim());
                        } catch (Exception e) {
                            throw new ServletException
                                    (sm.getString
                                            ("httpProcessor.parseHeaders.portNumber"));
                        }
                        request.setServerPort(port);
//                    }
                }
            } else if (header.equals(DefaultHeaders.CONNECTION_NAME)) {
                if (header.valueEquals
                        (DefaultHeaders.CONNECTION_CLOSE_VALUE)) {
                    keepAlive = false;
                    response.setHeader("Connection", "close");
                }
                //request.setConnection(header);
                /*
                  if ("keep-alive".equalsIgnoreCase(value)) {
                  keepAlive = true;
                  }
                */
            } else if (header.equals(DefaultHeaders.EXPECT_NAME)) {
                if (header.valueEquals(DefaultHeaders.EXPECT_100_VALUE))
                    sendAck = true;
                else
                    throw new ServletException
                            (sm.getString
                                    ("httpProcessor.parseHeaders.unknownExpectation"));
            } else if (header.equals(DefaultHeaders.TRANSFER_ENCODING_NAME)) {
                //request.setTransferEncoding(header);
            }

            request.nextHeader();

        }

    }

    private void parseAcceptLanguage(String value) {

        // Store the accumulated languages that have been requested in
        // a local collection, sorted by the quality value (so we can
        // add Locales in descending order).  The values will be ArrayLists
        // containing the corresponding Locales to be added
        TreeMap locales = new TreeMap();

        // Preprocess the value to remove all whitespace
        int white = value.indexOf(' ');
        if (white < 0)
            white = value.indexOf('\t');
        if (white >= 0) {
            StringBuffer sb = new StringBuffer();
            int len = value.length();
            for (int i = 0; i < len; i++) {
                char ch = value.charAt(i);
                if ((ch != ' ') && (ch != '\t'))
                    sb.append(ch);
            }
            value = sb.toString();
        }

        // Process each comma-delimited language specification
        parser.setString(value);        // ASSERT: parser is available to us
        int length = parser.getLength();
        while (true) {

            // Extract the next comma-delimited entry
            int start = parser.getIndex();
            if (start >= length)
                break;
            int end = parser.findChar(',');
            String entry = parser.extract(start, end).trim();
            parser.advance();   // For the following entry

            // Extract the quality factor for this entry
            double quality = 1.0;
            int semi = entry.indexOf(";q=");
            if (semi >= 0) {
                try {
                    quality = Double.parseDouble(entry.substring(semi + 3));
                } catch (NumberFormatException e) {
                    quality = 0.0;
                }
                entry = entry.substring(0, semi);
            }

            // Skip entries we are not going to keep track of
            if (quality < 0.00005)
                continue;       // Zero (or effectively zero) quality factors
            if ("*".equals(entry))
                continue;       // FIXME - "*" entries are not handled

            // Extract the language and country for this entry
            String language = null;
            String country = null;
            String variant = null;
            int dash = entry.indexOf('-');
            if (dash < 0) {
                language = entry;
                country = "";
                variant = "";
            } else {
                language = entry.substring(0, dash);
                country = entry.substring(dash + 1);
                int vDash = country.indexOf('-');
                if (vDash > 0) {
                    String cTemp = country.substring(0, vDash);
                    variant = country.substring(vDash + 1);
                    country = cTemp;
                } else {
                    variant = "";
                }
            }

            // Add a new Locale to the list of Locales for this quality level
            Locale locale = new Locale(language, country, variant);
            Double key = new Double(-quality);  // Reverse the order
            ArrayList values = (ArrayList) locales.get(key);
            if (values == null) {
                values = new ArrayList();
                locales.put(key, values);
            }
            values.add(locale);

        }

        // Process the quality values in highest->lowest order (due to
        // negating the Double value when creating the key)
        Iterator keys = locales.keySet().iterator();
        while (keys.hasNext()) {
            Double key = (Double) keys.next();
            ArrayList list = (ArrayList) locales.get(key);
            Iterator values = list.iterator();
            while (values.hasNext()) {
                Locale locale = (Locale) values.next();
                if (debug >= 1)
                    log(" Adding locale '" + locale + "'");
                request.addLocale(locale);
            }
        }

    }


    //private for request
    private void parseRequest(SocketInputStream input, OutputStream output)
            throws IOException, ServletException {

        // Parse the incoming request line
        input.readRequestLine(requestLine);

        // When the previous method returns, we're actually processing a
        // request
        status = Constants.PROCESSOR_ACTIVE;

        String method =
                new String(requestLine.method, 0, requestLine.methodEnd);
        String uri = null;
        String protocol = new String(requestLine.protocol, 0,
                requestLine.protocolEnd);

        //System.out.println(" Method:" + method + "_ Uri:" + uri
        //                   + "_ Protocol:" + protocol);

        if (protocol.length() == 0)
            protocol = "HTTP/0.9";

        // Now check if the connection should be kept alive after parsing the
        // request.
        if ( protocol.equals("HTTP/1.1") ) {
            http11 = true;
            sendAck = false;
        } else {
            http11 = false;
            sendAck = false;
            // For HTTP/1.0, connection are not persistent by default,
            // unless specified with a Connection: Keep-Alive header.
            keepAlive = false;
        }

        // Validate the incoming request line
        if (method.length() < 1) {
            throw new ServletException
                    (sm.getString("httpProcessor.parseRequest.method"));
        } else if (requestLine.uriEnd < 1) {
            throw new ServletException
                    (sm.getString("httpProcessor.parseRequest.uri"));
        }

        // Parse any query parameters out of the request URI
        int question = requestLine.indexOf("?");
        if (question >= 0) {
            request.setQueryString
                    (new String(requestLine.uri, question + 1,
                            requestLine.uriEnd - question - 1));
            if (debug >= 1)
                log(" Query string is " +
                        ((HttpServletRequest) request.getRequest())
                                .getQueryString());
            uri = new String(requestLine.uri, 0, question);
        } else {
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
                } else {
                    uri = uri.substring(pos);
                }
            }
        }

        // Parse any requested session ID out of the request URI
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            } else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            request.setRequestedSessionURL(true);
            uri = uri.substring(0, semicolon) + rest;
            if (debug >= 1)
                log(" Requested URL session id is " +
                        ((HttpServletRequest) request.getRequest())
                                .getRequestedSessionId());
        } else {
            request.setRequestedSessionId(null);
            request.setRequestedSessionURL(false);
        }

        // Normalize URI (using String operations at the moment)
        String normalizedUri = RequestUtil.normalize(uri);
        if (debug >= 1)
            log("Normalized: '" + uri + "' to '" + normalizedUri + "'");

        // Set the corresponding request properties
        ((HttpRequest) request).setMethod(method);
        request.setProtocol(protocol);
        if (normalizedUri != null) {
            ((HttpRequest) request).setRequestURI(normalizedUri);
        } else {
            ((HttpRequest) request).setRequestURI(uri);
        }
        request.setSecure(connector.getSecure());
        request.setScheme(connector.getScheme());

        if (normalizedUri == null) {
            log(" Invalid request URI: '" + uri + "'");
            throw new ServletException("Invalid URI: " + uri + "'");
        }

        if (debug >= 1)
            log(" Request is '" + method + "' for '" + uri +
                    "' with protocol '" + protocol + "'");

    }

    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

//        Logger logger = connector.getContainer().getLogger();
//        if (logger != null)
//            logger.log(threadName + " " + message);
        System.out.println(message);
    }

    /**
     * The thread synchronization object.
     */
    private Object threadSync = new Object();
    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    /**
     * The shutdown signal to our background thread
     */
    private boolean stopped = false;


    public void run() {
        // Process requests until we receive a shutdown signal
        while (!stopped) {
            // Wait for the next socket to be assigned
            Socket socket = await();
            if (socket == null)
                continue;
            // Process the request from this socket
            try {
                process(socket);
            }
            catch (Throwable t) {
                System.out.println("process.invoke" + t.getMessage());
            }
            // Finish up this request
            connector.recycle(this);
        }
        // Tell threadStop() we have shut ourselves down successfully
        synchronized (threadSync) {
            threadSync.notifyAll();
        }
    }

    /**
     * Is there a new socket available?
     */
    private boolean available = false;
    /**
     * The socket we are currently processing a request for.  This object
     * is used for inter-thread communication only.
     */
    private Socket socket = null;
    private int debug = 1;


    public synchronized void assign(Socket socket) {
        // Wait for the Processor to get the previous Socket
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // Store the newly available Socket and notify our thread
        this.socket = socket;
        available = true;
        notifyAll();

        if ((debug >= 1) && (socket != null))
            System.out.println(" An incoming request is being assigned");
    }

    /**
     * Await a newly assigned Socket from our Connector, or <code>null</code>
     * if we are supposed to shut down.
     */
    private synchronized Socket await() {

        // Wait for the Connector to provide a new Socket
        while (!available) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // Notify the Connector that we have received this Socket
        Socket socket = this.socket;
        available = false;
        notifyAll();

        if ((debug >= 1) && (socket != null))
            System.out.println("  The incoming request has been awaited");

        return (socket);

    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {

    }

    private LifecycleSupport lifecycle = new LifecycleSupport(this);
    @Override
    public void start() throws LifecycleException {
        if (started)
            throw new LifecycleException
                    (sm.getString("httpProcessor.alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        threadStart();
    }

    private Thread thread = null;
    private String threadName = null;

    private void threadStart() {

        log(sm.getString("httpProcessor.starting"));

        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();

        if (debug >= 1)
            log(" Background thread has been started");

    }


    @Override
    public void stop() throws LifecycleException {

    }
}
