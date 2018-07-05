/*
 * Copyright 1999-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.sailing.tomcat.host;


import com.sailing.tomcat.ValveBase;
import com.sailing.tomcat.container.Context;
import com.sailing.tomcat.container.ValveContext;
import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;
import com.sailing.tomcat.session.Manager;
import com.sailing.tomcat.session.Session;
import com.sailing.tomcat.util.Constants;
import com.sailing.tomcat.util.StringManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Valve that implements the default basic behavior for the
 * <code>StandardHost</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>:  This implementation is likely to be useful only
 * when processing HTTP requests.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.8 $ $Date: 2004/08/26 21:32:20 $
 */

final class StandardHostValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardHostValve/1.0";


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Select the appropriate child Context to process this request,
     * based on the specified request URI.  If no matching Context can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public void invoke(Request request, Response response,
                       ValveContext valveContext)
        throws IOException, ServletException {

        // Validate the request and response object types
        if (!(request.getRequest() instanceof HttpServletRequest) ||
            !(response.getResponse() instanceof HttpServletResponse)) {
            return;     // NOTE - Not much else we can do generically
        }

        // Select the Context to be used for this Request
        StandardHost host = (StandardHost) getContainer();
        Context context = (Context) host.map(request, true);
        if (context == null) {
            ((HttpServletResponse) response.getResponse()).sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 sm.getString("standardHost.noContext"));
            return;
        }

        // Bind the context CL to the current thread
        Thread.currentThread().setContextClassLoader
            (context.getLoader().getClassLoader());

        // Update the session last access time for our session (if any)
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        String sessionId = hreq.getRequestedSessionId();
        if (sessionId != null) {
            Manager manager = context.getManager();
            if (manager != null) {
                Session session = manager.findSession(sessionId);
                if ((session != null) && session.isValid())
                    session.access();
            }
        }

        // Ask this Context to process this request
        context.invoke(request, response);

        Thread.currentThread().setContextClassLoader
            (StandardHostValve.class.getClassLoader());

    }

}
