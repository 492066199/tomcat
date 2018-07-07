package com.sailing.tomcat.request;


import com.google.common.collect.Lists;
import com.sailing.tomcat.http.HttpHeader;
import com.sailing.tomcat.response.HttpResponseImpl;
import com.sailing.tomcat.util.Enumerator;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * Implementation of <b>HttpRequest</b> specific to the HTTP connector.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.14 $ $Date: 2004/08/26 21:28:57 $
 * @deprecated
 */

final public class HttpRequestImpl
    extends HttpRequestBase {


    // -------------------------------------------------------------- Constants


    /**
     * Initial pool size.
     */
    protected static final int INITIAL_POOL_SIZE = 10;


    /**
     * Pool size increment.
     */
    protected static final int POOL_SIZE_INCREMENT = 5;


    // ----------------------------------------------------- Instance Variables


    /**
     * The InetAddress of the remote client of ths request.
     */
    protected InetAddress inet = null;


    /**
     * Descriptive information about this Request implementation.
     */
    protected static final String info = "com.sailing.tomcat.request.HttpRequestImpl/1.0";


    /**
     * Headers pool.
     */
    protected List<HttpHeader> headerPool = Lists.newArrayList();


    /**
     * Position of the next available header in the pool.
     */
    protected int nextHeader = 0;


    /**
     * Connection header.
     */
    protected HttpHeader connectionHeader = null;


    /**
     * Transfer encoding header.
     */
    protected HttpHeader transferEncodingHeader = null;


    // ------------------------------------------------------------- Properties


    /**
     * [Package Private] Return the InetAddress of the remote client of
     * this request.
     */
    public InetAddress getInet() {

        return (inet);

    }


    /**
     * [Package Private] Set the InetAddress of the remote client of
     * this request.
     *
     * @param inet The new InetAddress
     */
    public void setInet(InetAddress inet) {

        this.inet = inet;

    }


    /**
     * Return descriptive information about this Request implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        super.recycle();
        inet = null;
        nextHeader = 0;
        connectionHeader = null;

    }


    /**
     * Create and return a ServletInputStream to read the content
     * associated with this Request.  The default implementation creates an
     * instance of RequestStream associated with this request, but this can
     * be overridden if necessary.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletInputStream createInputStream() throws IOException {

        return (new HttpRequestStream(this, (HttpResponseImpl) response));

    }

    /**
     * Go to the next header.
     */
    public void nextHeader() {
        nextHeader++;
    }


    /**
     * Add a Header to the set of Headers associated with this Request.
     *
     * @param name The new header name
     * @param value The new header value
     * @deprecated Don't use
     */
    public void addHeader(String name, String value) {
        headerPool.add(new HttpHeader(name, value));
    }


    /**
     * Clear the collection of Headers associated with this Request.
     */
    public void clearHeaders() {

        nextHeader = 0;

    }


    /**
     * Return the first value of the specified header, if any; otherwise,
     * return <code>null</code>
     *
     * @param header Header we want to retrieve
     */
    public HttpHeader getHeader(HttpHeader header) {

        for(HttpHeader httpHeader : headerPool){
            if (httpHeader.equals(header))
                return httpHeader;
        }
        return null;
    }


    /**
     * Return the first value of the specified header, if any; otherwise,
     * return <code>null</code>
     *
     * @param headerName Name of the requested header
     */
    public HttpHeader getHeader(char[] headerName) {
        for(HttpHeader httpHeader : headerPool){
            if (httpHeader.equals(headerName))
                return httpHeader;
        }
        return null;
    }


    /**
     * Perform whatever actions are required to flush and close the input
     * stream or reader, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishRequest() throws IOException {

        // If neither a reader or an is have been opened, do it to consume
        // request bytes, if any
        if ((reader == null) && (stream == null) && (getContentLength() != 0)
            && (getProtocol() != null) && (getProtocol().equals("HTTP/1.1")))
            getInputStream();

        super.finishRequest();

    }


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Return the Internet Protocol (IP) address of the client that sent
     * this request.
     */
    public String getRemoteAddr() {

        return (inet.getHostAddress());

    }


    /**
     * Return the fully qualified name of the client that sent this request,
     * or the IP address of the client if the name cannot be determined.
     */
    public String getRemoteHost() {

        if (connector.getEnableLookups())
            return (inet.getHostName());
        else
            return (getRemoteAddr());

    }


    // --------------------------------------------- HttpServletRequest Methods


    /**
     * Return the first value of the specified header, if any; otherwise,
     * return <code>null</code>
     *
     * @param name Name of the requested header
     */
    public String getHeader(String name) {

        name = name.toLowerCase();
        for(HttpHeader httpHeader : headerPool){
            if (httpHeader.equals(name)) {
                return new String(httpHeader.value, 0, httpHeader.valueEnd);
            }
        }
        return null;


    }


    /**
     * Return all of the values of the specified header, if any; otherwise,
     * return an empty enumeration.
     *
     * @param name Name of the requested header
     */
    public Enumeration getHeaders(String name) {
        name = name.toLowerCase();
        List<String> tempArrayList = Lists.newArrayList();
        for(HttpHeader httpHeader : headerPool){
            if(httpHeader.equals(name)){
                tempArrayList.add(new String(httpHeader.value, 0, httpHeader.valueEnd));
            }
        }
        return new Enumerator(tempArrayList);

    }


    /**
     * Return the names of all headers received with this request.
     */
    public Enumeration getHeaderNames() {
        List<String> tempArrayList = Lists.newArrayList();
        for(HttpHeader httpHeader : headerPool){
            tempArrayList.add(new String(httpHeader.name, 0, httpHeader.nameEnd));
        }
        return new Enumerator(tempArrayList);

    }

}
