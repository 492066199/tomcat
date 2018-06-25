package com.sailing.tomcat.response;


import com.sailing.tomcat.util.Constants;
import com.sailing.tomcat.util.StringManager;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ResponseStream extends ServletOutputStream {

    public void write(int b) throws IOException {
        if (suspended)
            return;

        if (closed)
            throw new IOException(sm.getString("responseStream.write.closed"));

        if ((length > 0) && (count >= length))
            throw new IOException(sm.getString("responseStream.write.count"));

        //最终还是要写到response里面
        ((ResponseBase) response).write(b);
        count++;

    }

    //this method is more important, this the bridge link the response
    public ResponseStream(Response response) {
        super();
        closed = false;
        commit = false;
        count = 0;
        this.response = response;
        this.stream = response.getStream();
        this.suspended = response.isSuspended();
    }

    /**
     * Has this stream been closed?
     */
    protected boolean closed = false;
    protected boolean commit = false;
    protected Response response = null;
    protected OutputStream stream = null; //only for check!

    /**
     * The number of bytes which have already been written to this stream.
     */
    protected int count = 0;


    /**
     * The content length past which we will not write, or -1 if there is
     * no defined content length.
     */
    protected int length = -1;




    /**
     * The localized strings for this package.
     */
    protected static StringManager sm =
            StringManager.getManager(Constants.Package);




    /**
     * Has this response output been suspended?
     */
    protected boolean suspended = false;


    // ------------------------------------------------------------- Properties


    /**
     * [Package Private] Return the "commit response on flush" flag.
     */
    boolean getCommit() {

        return (this.commit);

    }


    /**
     * [Package Private] Set the "commit response on flush" flag.
     *
     * @param commit The new commit flag
     */
    void setCommit(boolean commit) {

        this.commit = commit;

    }


    /**
     * Set the suspended flag.
     */
    void setSuspended(boolean suspended) {

        this.suspended = suspended;

    }


    /**
     * Suspended flag accessor.
     */
    boolean isSuspended() {

        return (this.suspended);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Close this output stream, causing any buffered data to be flushed and
     * any further output data to throw an IOException.
     */
    public void close() throws IOException {

        if (suspended)
            throw new IOException
                    (sm.getString("responseStream.suspended"));

        if (closed)
            throw new IOException(sm.getString("responseStream.close.closed"));

        response.getResponse().flushBuffer();
        closed = true;

    }


    /**
     * Flush any buffered data for this output stream, which also causes the
     * response to be committed.
     */
    public void flush() throws IOException {

        if (suspended)
            throw new IOException
                    (sm.getString("responseStream.suspended"));

        if (closed)
            throw new IOException(sm.getString("responseStream.flush.closed"));

        if (commit)
            response.getResponse().flushBuffer();

    }

    /**
     * Write <code>b.length</code> bytes from the specified byte array
     * to our output stream.
     *
     * @param b The byte array to be written
     * @throws IOException if an input/output error occurs
     */
    public void write(byte b[]) throws IOException {

        if (suspended)
            return;

        write(b, 0, b.length);

    }


    /**
     * Write <code>len</code> bytes from the specified byte array, starting
     * at the specified offset, to our output stream.
     *
     * @param b   The byte array containing the bytes to be written
     * @param off Zero-relative starting offset of the bytes to be written
     * @param len The number of bytes to be written
     * @throws IOException if an input/output error occurs
     */
    public void write(byte b[], int off, int len) throws IOException {

        if (suspended)
            return;

        if (closed)
            throw new IOException(sm.getString("responseStream.write.closed"));

        int actual = len;
        if ((length > 0) && ((count + len) >= length))
            actual = length - count;
        ((ResponseBase) response).write(b, off, actual);
        count += actual;
        if (actual < len)
            throw new IOException(sm.getString("responseStream.write.count"));

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Has this response stream been closed?
     */
    public boolean closed() {

        return (this.closed);

    }


    /**
     * Reset the count of bytes written to this stream to zero.
     */
    public void reset() {

        count = 0;

    }


}
