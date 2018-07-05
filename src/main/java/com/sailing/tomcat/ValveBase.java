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


package com.sailing.tomcat;


import com.sailing.tomcat.container.Contained;
import com.sailing.tomcat.container.Container;
import com.sailing.tomcat.container.Valve;
import com.sailing.tomcat.container.ValveContext;
import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;
import com.sailing.tomcat.util.Constants;
import com.sailing.tomcat.util.StringManager;

import javax.servlet.ServletException;
import java.io.IOException;


/**
 * Convenience base class for implementations of the <b>Valve</b> interface.
 * A subclass <strong>MUST</strong> implement an <code>invoke()</code>
 * method to provide the required functionality, and <strong>MAY</strong>
 * implement the <code>Lifecycle</code> interface to provide configuration
 * management and lifecycle support.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2004/08/26 21:43:51 $
 */

public abstract class ValveBase
    implements Contained, Valve {


    //------------------------------------------------------ Instance Variables


    /**
     * The Container whose pipeline this Valve is a component of.
     */
    protected Container container = null;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * Descriptive information about this Valve implementation.  This value
     * should be overridden by subclasses.
     */
    protected static String info =
        "org.apache.catalina.core.ValveBase/1.0";


    /**
     * The string manager for this package.
     */
    protected final static StringManager sm =
        StringManager.getManager(Constants.Package);


    //-------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Valve is associated, if any.
     */
    public Container getContainer() {

        return (container);

    }


    /**
     * Set the Container with which this Valve is associated, if any.
     *
     * @param container The new associated container
     */
    public void setContainer(Container container) {

        this.container = container;

    }


   /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

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
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    //---------------------------------------------------------- Public Methods


    /**
     * The implementation-specific logic represented by this Valve.  See the
     * Valve description for the normal design patterns for this method.
     * <p>
     * This method <strong>MUST</strong> be provided by a subclass.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     * @param context The valve context used to invoke the next valve
     *  in the current processing pipeline
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public abstract void invoke(Request request, Response response,
                                ValveContext context)
        throws IOException, ServletException;


}
