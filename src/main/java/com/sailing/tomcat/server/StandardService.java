package com.sailing.tomcat.server;


import com.google.common.collect.Lists;
import com.sailing.tomcat.connector.Connector;
import com.sailing.tomcat.container.Container;
import com.sailing.tomcat.engine.Engine;
import com.sailing.tomcat.life.Lifecycle;
import com.sailing.tomcat.life.LifecycleException;
import com.sailing.tomcat.life.LifecycleListener;
import com.sailing.tomcat.life.LifecycleSupport;
import com.sailing.tomcat.util.Constants;
import com.sailing.tomcat.util.StringManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;


/**
 * Standard implementation of the <code>Service</code> interface.  The
 * associated Container is generally an instance of Engine, but this is
 * not required.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.12 $ $Date: 2004/08/26 21:32:20 $
 */

public final class StandardService
    implements Lifecycle, Service {


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of Connectors associated with this Service.
     */
    private List<Connector> connectors = Lists.newArrayList();


    /**
     * The Container associated with this Service.
     */
    private Container container = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * Descriptive information about this component implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardService/1.0";


    /**
     * Has this component been initialized?
     */
    private boolean initialized = false;


    /**
     * The name of this service.
     */
    private String name = null;


    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);

    /**
     * The <code>Server</code> that owns this Service, if any.
     */
    private Server server = null;

    /**
     * Has this component been started?
     */
    private boolean started = false;


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    // ------------------------------------------------------------- Properties


    /**
     * Return the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     *
     * @param container The new Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        if ((oldContainer != null) && (oldContainer instanceof Engine)) {
            ((Engine) oldContainer).setService(null);
        }

        this.container = container;

        if ((this.container != null) && (this.container instanceof Engine)) {
            ((Engine) this.container).setService(this);
        }

        if (started && (this.container != null) &&
            (this.container instanceof Lifecycle)) {
            try {
                ((Lifecycle) this.container).start();
            } catch (LifecycleException e) {
                ;
            }
        }
        synchronized (connectors) {
            for(Connector connector : connectors) {
                connector.setContainer(this.container);
            }
        }
        if (started && (oldContainer != null) &&
            (oldContainer instanceof Lifecycle)) {
            try {
                ((Lifecycle) oldContainer).stop();
            } catch (LifecycleException e) {
                ;
            }
        }

        // Report this property change to interested listeners
        support.firePropertyChange("container", oldContainer, this.container);

    }


    /**
     * Return the debugging detail level of this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level of this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return descriptive information about this Service implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Return the name of this Service.
     */
    public String getName() {

        return (this.name);

    }


    /**
     * Set the name of this Service.
     *
     * @param name The new service name
     */
    public void setName(String name) {

        this.name = name;

    }


    /**
     * Return the <code>Server</code> with which we are associated (if any).
     */
    public Server getServer() {

        return (this.server);

    }


    /**
     * Set the <code>Server</code> with which we are associated (if any).
     *
     * @param server The server that owns this Service
     */
    public void setServer(Server server) {

        this.server = server;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Connector to the set of defined Connectors, and associate it
     * with this Service's Container.
     *
     * @param connector The Connector to be added
     */
    public void addConnector(Connector connector) {

        synchronized (connectors) {
            connector.setContainer(this.container);
            connector.setService(this);

            connectors.add(connector);

            if (initialized) {
                try {
                    connector.initialize();
                } catch (LifecycleException e) {
                    e.printStackTrace(System.err);
                }
            }

            if (started && (connector instanceof Lifecycle)) {
                try {
                    ((Lifecycle) connector).start();
                } catch (LifecycleException e) {
                    ;
                }
            }

            // Report this property change to interested listeners
            support.firePropertyChange("connector", null, connector);
        }

    }


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        support.addPropertyChangeListener(listener);

    }


    /**
     * Find and return the set of Connectors associated with this Service.
     */
    public Connector[] findConnectors() {

        return (connectors.toArray(new Connector[connectors.size()]));

    }


    /**
     * Remove the specified Connector from the set associated from this
     * Service.  The removed Connector will also be disassociated from our
     * Container.
     *
     * @param connector The Connector to be removed
     */
    public void removeConnector(Connector connector) {

        synchronized (connectors) {
            connectors.remove(connector);

            if (started && (connector instanceof Lifecycle)) {
                try {
                    ((Lifecycle) connectors).stop();
                } catch (LifecycleException e) {
                    ;
                }
            }
            connector.setContainer(null);
            connector.setService(null);

            // Report this property change to interested listeners
            support.firePropertyChange("connector", connector, null);
        }

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        support.removePropertyChangeListener(listener);

    }


    /**
     * Return a String representation of this component.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("StandardService[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a LifecycleEvent listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a LifecycleEvent listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called before any of the public
     * methods of this component are utilized.  It should also send a
     * LifecycleEvent of type START_EVENT to any registered listeners.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        // Validate and update our current component state
        if (started) {
            throw new LifecycleException
                (sm.getString("standardService.start.started"));
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);

        System.out.println
            (sm.getString("standardService.start.name", this.name));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Start our defined Container first
        if (container != null) {
            synchronized (container) {
                if (container instanceof Lifecycle) {
                    ((Lifecycle) container).start();
                }
            }
        }

        // Start our defined Connectors second
        synchronized (connectors) {
            for (Connector connector : connectors) {
                ((Lifecycle) connector).start();
            }
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.  It should also send a LifecycleEvent
     * of type STOP_EVENT to any registered listeners.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started) {
            throw new LifecycleException
                (sm.getString("standardService.stop.notStarted"));
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);

        lifecycle.fireLifecycleEvent(STOP_EVENT, null);

        System.out.println
            (sm.getString("standardService.stop.name", this.name));
        started = false;

        // Stop our defined Connectors first
        synchronized (connectors) {
            for (Connector connector : connectors) {
                if (connector instanceof Lifecycle)
                    ((Lifecycle) connector).stop();
            }
        }

        // Stop our defined Container second
        if (container != null) {
            synchronized (container) {
                if (container instanceof Lifecycle) {
                    ((Lifecycle) container).stop();
                }
            }
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);

    }


    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     */
    public void initialize()
    throws LifecycleException {
        if (initialized)
            throw new LifecycleException (
                sm.getString("standardService.initialize.initialized"));
        initialized = true;

        // Initialize our defined Connectors
        synchronized (connectors) {
            for (Connector connector : connectors) {
                connector.initialize();
            }
        }
    }

}
