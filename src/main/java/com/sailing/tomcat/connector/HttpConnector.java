package com.sailing.tomcat.connector;

import com.sailing.tomcat.container.Container;
import com.sailing.tomcat.logger.Logger;
import com.sailing.tomcat.request.HttpRequestImpl;
import com.sailing.tomcat.response.HttpResponseImpl;
import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;
import com.sailing.tomcat.life.Lifecycle;
import com.sailing.tomcat.life.LifecycleException;
import com.sailing.tomcat.life.LifecycleListener;
import com.sailing.tomcat.life.LifecycleSupport;
import com.sailing.tomcat.processor.HttpProcessor;
import com.sailing.tomcat.socket.DefaultServerSocketFactory;
import com.sailing.tomcat.socket.ServerSocketFactory;
import com.sailing.tomcat.util.Constants;
import com.sailing.tomcat.util.StringManager;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Stack;
import java.util.Vector;


public final class HttpConnector implements Connector, Lifecycle, Runnable {

    //    private Service service = null;

    protected Container container = null;
    //other info, use for other object
    private int debug = 0;
    private static final String info = "com.sailing.tomcat.connector.HttpConnector";
    private int bufferSize = 2048;

    /**
     * The "enable DNS lookups" flag for this Connector.
     */
    private boolean enableLookups = false;


    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    //for processor
    private Vector created = new Vector();
    protected int minProcessors = 5;
    private int curProcessors = 0;
    private int maxProcessors = 20;
    private Stack processors = new Stack();

    //for socket server
    private ServerSocket serverSocket = null;
    private String address = null;
    private ServerSocketFactory factory = null;
    private int port = 8080;
    private int acceptCount = 10;
    private int connectionTimeout = Constants.DEFAULT_CONNECTION_TIMEOUT;

    private StringManager sm = StringManager.getManager(Constants.Package);
    private boolean started = false;

    /**
     * The server name to which we should pretend requests to this Connector
     * were directed.  This is useful when operating Tomcat behind a proxy
     * server, so that redirects get constructed accurately.  If not specified,
     * the server name included in the <code>Host</code> header is used.
     */
    private String proxyName = null;


    /**
     * The server port to which we should pretent requests to this Connector
     * were directed.  This is useful when operating Tomcat behind a proxy
     * server, so that redirects get constructed accurately.  If not specified,
     * the port number specified by the <code>port</code> property is used.
     */
    private int proxyPort = 0;


    /**
     * The redirect port for non-SSL to SSL redirects.
     */
    private int redirectPort = 443;


    /**
     * The request scheme that will be set on all requests received
     * through this connector.
     */
    private String scheme = "http";


    /**
     * The secure connection flag that will be set on all requests received
     * through this connector.
     */
    private boolean secure = false;

    /**
     * Has this component been initialized yet?
     */
    private boolean initialized = false;


    /**
     * The shutdown signal to our background thread
     */
    private boolean stopped = false;


    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The name to register for the background thread.
     */
    private String threadName = null;


    /**
     * The thread synchronization object.
     */
    private Object threadSync = new Object();


    /**
     * Is chunking allowed ?
     */
    private boolean allowChunking = true;


    /**
     * Use TCP no delay ?
     */
    private boolean tcpNoDelay = true;


    // ------------------------------------------------------------- Properties

//
//    /**
//     * Return the <code>Service</code> with which we are associated (if any).
//     */
//    public Service getService() {
//
//        return (this.service);
//
//    }
//
//
//    /**
//     * Set the <code>Service</code> with which we are associated (if any).
//     *
//     * @param service The service that owns this Engine
//     */
//    public void setService(Service service) {
//
//        this.service = service;
//
//    }


    /**
     * Return the connection timeout for this Connector.
     */
    public int getConnectionTimeout() {

        return (connectionTimeout);

    }


    /**
     * Set the connection timeout for this Connector.
     *
     */
    public void setConnectionTimeout(int connectionTimeout) {

        this.connectionTimeout = connectionTimeout;

    }


    /**
     * Return the accept count for this Connector.
     */
    public int getAcceptCount() {

        return (acceptCount);

    }


    /**
     * Set the accept count for this Connector.
     *
     * @param count The new accept count
     */
    public void setAcceptCount(int count) {

        this.acceptCount = count;

    }


    /**
     * Get the allow chunking flag.
     */
    public boolean isChunkingAllowed() {

        return (allowChunking);

    }


    /**
     * Get the allow chunking flag.
     */
    public boolean getAllowChunking() {

        return isChunkingAllowed();

    }


    /**
     * Set the allow chunking flag.
     *
     * @param allowChunking Allow chunking flag
     */
    public void setAllowChunking(boolean allowChunking) {

        this.allowChunking = allowChunking;

    }


    /**
     * Return the bind IP address for this Connector.
     */
    public String getAddress() {

        return (this.address);

    }


    /**
     * Set the bind IP address for this Connector.
     *
     * @param address The bind IP address
     */
    public void setAddress(String address) {

        this.address = address;

    }


    /**
     * Is this connector available for processing requests?
     */
    public boolean isAvailable() {

        return (started);

    }


    public int getBufferSize() {

        return (this.bufferSize);

    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }


    /**
     * Return the Container used for processing requests received by this
     * Connector.
     */
    public Container getContainer() {

        return (container);

    }


    /**
     * Set the Container used for processing requests received by this
     * Connector.
     *
     * @param container The new Container to use
     */
    public void setContainer(Container container) {

        this.container = container;

    }


    /**
     * Return the current number of processors that have been created.
     */
    public int getCurProcessors() {

        return (curProcessors);

    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return the "enable DNS lookups" flag.
     */
    public boolean getEnableLookups() {

        return (this.enableLookups);

    }


    /**
     * Set the "enable DNS lookups" flag.
     *
     * @param enableLookups The new "enable DNS lookups" flag value
     */
    public void setEnableLookups(boolean enableLookups) {

        this.enableLookups = enableLookups;

    }


    /**
     * Return the server socket factory used by this Container.
     */
    public ServerSocketFactory getFactory() {

        if (this.factory == null) {
            synchronized (this) {
                this.factory = new DefaultServerSocketFactory();
            }
        }
        return (this.factory);

    }


    /**
     * Set the server socket factory used by this Container.
     *
     * @param factory The new server socket factory
     */
    public void setFactory(ServerSocketFactory factory) {

        this.factory = factory;

    }


    /**
     * Return descriptive information about this Connector implementation.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the minimum number of processors to start at initialization.
     */
    public int getMinProcessors() {

        return (minProcessors);

    }


    /**
     * Set the minimum number of processors to start at initialization.
     *
     * @param minProcessors The new minimum processors
     */
    public void setMinProcessors(int minProcessors) {

        this.minProcessors = minProcessors;

    }


    /**
     * Return the maximum number of processors allowed, or <0 for unlimited.
     */
    public int getMaxProcessors() {

        return (maxProcessors);

    }


    /**
     * Set the maximum number of processors allowed, or <0 for unlimited.
     *
     * @param maxProcessors The new maximum processors
     */
    public void setMaxProcessors(int maxProcessors) {

        this.maxProcessors = maxProcessors;

    }


    /**
     * Return the port number on which we listen for HTTP requests.
     */
    public int getPort() {

        return (this.port);

    }


    /**
     * Set the port number on which we listen for HTTP requests.
     *
     * @param port The new port number
     */
    public void setPort(int port) {

        this.port = port;

    }


    /**
     * Return the proxy server name for this Connector.
     */
    public String getProxyName() {

        return (this.proxyName);

    }


    /**
     * Set the proxy server name for this Connector.
     *
     * @param proxyName The new proxy server name
     */
    public void setProxyName(String proxyName) {

        this.proxyName = proxyName;

    }


    /**
     * Return the proxy server port for this Connector.
     */
    public int getProxyPort() {

        return (this.proxyPort);

    }


    /**
     * Set the proxy server port for this Connector.
     *
     * @param proxyPort The new proxy server port
     */
    public void setProxyPort(int proxyPort) {

        this.proxyPort = proxyPort;

    }


    /**
     * Return the port number to which a request should be redirected if
     * it comes in on a non-SSL port and is subject to a security constraint
     * with a transport guarantee that requires SSL.
     */
    public int getRedirectPort() {

        return (this.redirectPort);

    }


    /**
     * Set the redirect port number.
     *
     * @param redirectPort The redirect port number (non-SSL to SSL)
     */
    public void setRedirectPort(int redirectPort) {

        this.redirectPort = redirectPort;

    }


    /**
     * Return the scheme that will be assigned to requests received
     * through this connector.  Default value is "http".
     */
    public String getScheme() {

        return (this.scheme);

    }


    /**
     * Set the scheme that will be assigned to requests received through
     * this connector.
     *
     * @param scheme The new scheme
     */
    public void setScheme(String scheme) {

        this.scheme = scheme;

    }


    /**
     * Return the secure connection flag that will be assigned to requests
     * received through this connector.  Default value is "false".
     */
    public boolean getSecure() {

        return (this.secure);

    }


    /**
     * Set the secure connection flag that will be assigned to requests
     * received through this connector.
     *
     * @param secure The new secure connection flag
     */
    public void setSecure(boolean secure) {

        this.secure = secure;

    }


    /**
     * Return the TCP no delay flag value.
     */
    public boolean getTcpNoDelay() {

        return (this.tcpNoDelay);

    }


    /**
     * Set the TCP no delay flag which will be set on the socket after
     * accepting a connection.
     *
     * @param tcpNoDelay The new TCP no delay flag
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {

        this.tcpNoDelay = tcpNoDelay;

    }


    public Request createRequest() {
        HttpRequestImpl request = new HttpRequestImpl();
        request.setConnector(this);
        return request;
    }

    public Response createResponse() {
        HttpResponseImpl response = new HttpResponseImpl();
        response.setConnector(this);
        return response;
    }


    // -------------------------------------------------------- Package Methods


    /**
     * Recycle the specified Processor so that it can be used again.
     *
     * @param processor The processor to be recycled
     */
     public void recycle(HttpProcessor processor) {

        if (debug >= 2)
            log("recycle: Recycling processor " + processor);
        processors.push(processor);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Create (or allocate) and return an available processor for use in
     * processing a specific HTTP request, if possible.  If the maximum
     * allowed processors have already been created and are in use, return
     * <code>null</code> instead.
     */
    private HttpProcessor createProcessor() {

        synchronized (processors) {
            if (processors.size() > 0) {
                return ((HttpProcessor) processors.pop());
            }
            if ((maxProcessors > 0) && (curProcessors < maxProcessors)) {
                return (newProcessor());
            } else {
                if (maxProcessors < 0) {
                    return (newProcessor());
                } else {
                    return (null);
                }
            }
        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        Logger logger = container.getLogger();
        String localName = threadName;
        if (localName == null)
            localName = "HttpConnector";
        if (logger != null)
            logger.log(localName + " " + message);
        else
            System.out.println(localName + " " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = container.getLogger();
        String localName = threadName;
        if (localName == null)
            localName = "HttpConnector";
        if (logger != null)
            logger.log(localName + " " + message, throwable);
        else {
            System.out.println(localName + " " + message);
            throwable.printStackTrace(System.out);
        }

    }

    private HttpProcessor newProcessor() {
        HttpProcessor processor = new HttpProcessor(this, curProcessors++);
        if (processor instanceof Lifecycle) {
            try {
                ((Lifecycle) processor).start();
            } catch (LifecycleException e) {
                log("newProcessor", e);
                return (null);
            }
        }
        created.addElement(processor);
        return (processor);

    }


    /**
     * Open and return the server socket for this Connector.  If an IP
     * address has been specified, the socket will be opened only on that
     * address; otherwise it will be opened on all addresses.
     *
     * @exception IOException                input/output or network error
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file (SSL only)
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider (SSL only)
     * @exception CertificateException       general certificate error (SSL only)
     * @exception UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate (SSL only)
     * @exception KeyManagementException     problem in the key management
     *                                       layer (SSL only)
     */
    private ServerSocket open()
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException, UnrecoverableKeyException,
           KeyManagementException
    {

        // Acquire the server socket factory for this Connector
        ServerSocketFactory factory = getFactory();

        // If no address is specified, open a connection on all addresses
        if (address == null) {
            log(sm.getString("httpConnector.allAddresses"));
            try {
                return (factory.createSocket(port, acceptCount));
            } catch (BindException be) {
                throw new BindException(be.getMessage() + ":" + port);
            }
        }

        // Open a server socket on the specified address
        try {
            InetAddress is = InetAddress.getByName(address);
            log(sm.getString("httpConnector.anAddress", address));
            try {
                return (factory.createSocket(port, acceptCount, is));
            } catch (BindException be) {
                throw new BindException(be.getMessage() + ":" + address +
                                        ":" + port);
            }
        } catch (Exception e) {
            log(sm.getString("httpConnector.noAddress", address));
            try {
                return (factory.createSocket(port, acceptCount));
            } catch (BindException be) {
                throw new BindException(be.getMessage() + ":" + port);
            }
        }

    }


    // ---------------------------------------------- Background Thread Methods


    /**
     * The background thread that listens for incoming TCP/IP connections and
     * hands them off to an appropriate processor.
     */
    public void run() {

        // Loop until we receive a shutdown command
        while (!stopped) {

            // Accept the next incoming connection from the server socket
            Socket socket = null;
            try {
                //                if (debug >= 3)
                //                    log("run: Waiting on serverSocket.accept()");
                socket = serverSocket.accept();
                //                if (debug >= 3)
                //                    log("run: Returned from serverSocket.accept()");
                if (connectionTimeout > 0)
                    socket.setSoTimeout(connectionTimeout);
                socket.setTcpNoDelay(tcpNoDelay);
            } catch (AccessControlException ace) {
                log("socket accept security exception", ace);
                continue;
            } catch (IOException e) {
                //                if (debug >= 3)
                //                    log("run: Accept returned IOException", e);
                try {
                    // If reopening fails, exit
                    synchronized (threadSync) {
                        if (started && !stopped)
                            log("accept error: ", e);
                        if (!stopped) {
                            //                    if (debug >= 3)
                            //                        log("run: Closing server socket");
                            serverSocket.close();
                            //                        if (debug >= 3)
                            //                            log("run: Reopening server socket");
                            serverSocket = open();
                        }
                    }
                    //                    if (debug >= 3)
                    //                        log("run: IOException processing completed");
                } catch (IOException ioe) {
                    log("socket reopen, io problem: ", ioe);
                    break;
                } catch (KeyStoreException kse) {
                    log("socket reopen, keystore problem: ", kse);
                    break;
                } catch (NoSuchAlgorithmException nsae) {
                    log("socket reopen, keystore algorithm problem: ", nsae);
                    break;
                } catch (CertificateException ce) {
                    log("socket reopen, certificate problem: ", ce);
                    break;
                } catch (UnrecoverableKeyException uke) {
                    log("socket reopen, unrecoverable key: ", uke);
                    break;
                } catch (KeyManagementException kme) {
                    log("socket reopen, key management problem: ", kme);
                    break;
                }

                continue;
            }

            // Hand this socket off to an appropriate processor
            HttpProcessor processor = createProcessor();
            if (processor == null) {
                try {
                    log(sm.getString("httpConnector.noProcessor"));
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }
            processor.assign(socket);

            // The processor will recycle itself when it finishes

        }

        // Notify the threadStop() method that we have shut ourselves down
        //        if (debug >= 3)
        //            log("run: Notifying threadStop() that we have shut down");
        synchronized (threadSync) {
            threadSync.notifyAll();
        }

    }


    /**
     * Start the background processing thread.
     */
    private void threadStart() {

        log(sm.getString("httpConnector.starting"));

        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();

    }


    /**
     * Stop the background processing thread.
     */
    private void threadStop() {
        log(sm.getString("httpConnector.stopping"));
        stopped = true;
        try {
            threadSync.wait(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread = null;
    }

    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }


    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }


    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }


    /**
     * Initialize this connector (create ServerSocket here!)
     */
    public void initialize() throws LifecycleException {
        if (initialized)
            throw new LifecycleException (
                sm.getString("httpConnector.alreadyInitialized"));

        this.initialized=true;
        Exception eRethrow = null;

        // Establish a server socket on the specified port
        try {
            serverSocket = open();
        } catch (IOException ioe) {
            log("httpConnector, io problem: ", ioe);
            eRethrow = ioe;
        } catch (KeyStoreException kse) {
            log("httpConnector, keystore problem: ", kse);
            eRethrow = kse;
        } catch (NoSuchAlgorithmException nsae) {
            log("httpConnector, keystore algorithm problem: ", nsae);
            eRethrow = nsae;
        } catch (CertificateException ce) {
            log("httpConnector, certificate problem: ", ce);
            eRethrow = ce;
        } catch (UnrecoverableKeyException uke) {
            log("httpConnector, unrecoverable key: ", uke);
            eRethrow = uke;
        } catch (KeyManagementException kme) {
            log("httpConnector, key management problem: ", kme);
            eRethrow = kme;
        }

        if (eRethrow != null )
            throw new LifecycleException(threadName + ".open", eRethrow);

    }


    public void start() throws LifecycleException {
        // Validate and update our current state
        if (started) {
            throw new LifecycleException(sm.getString("httpConnector.alreadyStarted"));
        }

        threadName = "HttpConnector[" + port + "]";
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Start our background thread
        threadStart();

        // Create the specified minimum number of processors
        while (curProcessors < minProcessors) {
            if ((maxProcessors > 0) && (curProcessors >= maxProcessors)) {
                break;
            }
            HttpProcessor processor = newProcessor();
            recycle(processor);
        }
    }


    public void stop() throws LifecycleException {
        // Validate and update our current state
        if (!started) {
            throw new LifecycleException(sm.getString("httpConnector.notStarted"));
        }
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Gracefully shut down all processors we have created
        for (int i = created.size() - 1; i >= 0; i--) {
            HttpProcessor processor = (HttpProcessor) created.elementAt(i);
            if (processor instanceof Lifecycle) {
                try {
                    ((Lifecycle) processor).stop();
                } catch (LifecycleException e) {
                    log("HttpConnector.stop", e);
                }
            }
        }

        synchronized (threadSync) {
            // Close the server socket we were using
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Stop our background thread
            threadStop();
        }
        serverSocket = null;
    }
}
