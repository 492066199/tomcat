package com.sailing.tomcat.container;

import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;

import javax.naming.directory.DirContext;
import javax.servlet.ServletException;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public interface Container {
    // ----------------------------------------------------- Manifest Constants
    /**
     * The ContainerEvent event type sent when a child container is added
     * by <code>addChild()</code>.
     */
    public static final String ADD_CHILD_EVENT = "addChild";


    /**
     * The ContainerEvent event type sent when a Mapper is added
     * by <code>addMapper()</code>.
     */
    public static final String ADD_MAPPER_EVENT = "addMapper";


    /**
     * The ContainerEvent event type sent when a valve is added
     * by <code>addValve()</code>, if this Container supports pipelines.
     */
    public static final String ADD_VALVE_EVENT = "addValve";


    /**
     * The ContainerEvent event type sent when a child container is removed
     * by <code>removeChild()</code>.
     */
    public static final String REMOVE_CHILD_EVENT = "removeChild";


    /**
     * The ContainerEvent event type sent when a Mapper is removed
     * by <code>removeMapper()</code>.
     */
    public static final String REMOVE_MAPPER_EVENT = "removeMapper";


    /**
     * The ContainerEvent event type sent when a valve is removed
     * by <code>removeValve()</code>, if this Container supports pipelines.
     */
    public static final String REMOVE_VALVE_EVENT = "removeValve";

    void addMapper(Mapper mapper);

    String getInfo();

    Loader getLoader();

    void setLoader(Loader loader);

    void addChild(Container child);

    Container findChild(String name);

    Container[] findChildren();

    void removeChild(Container child);

//
//    /**
//     * Return the Logger with which this Container is associated.  If there is
//     * no associated Logger, return the Logger associated with our parent
//     * Container (if any); otherwise return <code>null</code>.
//     */
//    public Logger getLogger();
//
//
//    /**
//     * Set the Logger with which this Container is associated.
//     *
//     * @param logger The newly associated Logger
//     */
//    public void setLogger(Logger logger);
//
//
//    /**
//     * Return the Manager with which this Container is associated.  If there is
//     * no associated Manager, return the Manager associated with our parent
//     * Container (if any); otherwise return <code>null</code>.
//     */
//    public Manager getManager();
//
//
//    /**
//     * Set the Manager with which this Container is associated.
//     *
//     * @param manager The newly associated Manager
//     */
//    public void setManager(Manager manager);
//
//
//    /**
//     * Return the Cluster with which this Container is associated.  If there is
//     * no associated Cluster, return the Cluster associated with our parent
//     * Container (if any); otherwise return <code>null</code>.
//     */
//    public Cluster getCluster();
//
//
//    /**
//     * Set the Cluster with which this Container is associated.
//     *
//     * @param connector The Connector to be added
//     */
//    public void setCluster(Cluster cluster);


    /**
     * Return a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     */
    public String getName();


    /**
     * Set a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     *
     * @param name New name of this container
     *
     * @exception IllegalStateException if this Container has already been
     *  added to the children of a parent Container (after which the name
     *  may not be changed)
     */
    public void setName(String name);


    /**
     * Return the Container for which this Container is a child, if there is
     * one.  If there is no defined parent, return <code>null</code>.
     */
    public Container getParent();


    /**
     * Set the parent Container to which this Container is being added as a
     * child.  This Container may refuse to become attached to the specified
     * Container by throwing an exception.
     *
     * @param container Container to which this Container is being added
     *  as a child
     *
     * @exception IllegalArgumentException if this Container refuses to become
     *  attached to the specified Container
     */
    public void setParent(Container container);


    /**
     * Return the parent class loader (if any) for web applications.
     */
    public ClassLoader getParentClassLoader();


    /**
     * Set the parent class loader (if any) for web applications.
     * This call is meaningful only <strong>before</strong> a Loader has
     * been configured, and the specified value (if non-null) should be
     * passed as an argument to the class loader constructor.
     *
     * @param parent The new parent class loader
     */
    public void setParentClassLoader(ClassLoader parent);


//    /**
//     * Return the Realm with which this Container is associated.  If there is
//     * no associated Realm, return the Realm associated with our parent
//     * Container (if any); otherwise return <code>null</code>.
//     */
//    public Realm getRealm();
//
//
//    /**
//     * Set the Realm with which this Container is associated.
//     *
//     * @param realm The newly associated Realm
//     */
//    public void setRealm(Realm realm);


    /**
     * Return the Resources with which this Container is associated.  If there
     * is no associated Resources object, return the Resources associated with
     * our parent Container (if any); otherwise return <code>null</code>.
     */
    public DirContext getResources();


    /**
     * Set the Resources object with which this Container is associated.
     *
     * @param resources The newly associated Resources
     */
    public void setResources(DirContext resources);

//    /**
//     * Add a container event listener to this component.
//     *
//     * @param listener The listener to add
//     */
//    public void addContainerListener(ContainerListener listener);
//

    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

//    /**
//     * Return the set of container listeners associated with this Container.
//     * If this Container has no registered container listeners, a zero-length
//     * array is returned.
//     */
//    public ContainerListener[] findContainerListeners();
//
//
//    /**
//     * Return the Mapper associated with the specified protocol, if there
//     * is one.  If there is only one defined Mapper, use it for all protocols.
//     * If there is no matching Mapper, return <code>null</code>.
//     *
//     * @param protocol Protocol for which to find a Mapper
//     */
//    public Mapper findMapper(String protocol);
//
//
//    /**
//     * Return the set of Mappers associated with this Container.  If this
//     * Container has no Mappers, a zero-length array is returned.
//     */
//    public Mapper[] findMappers();
//
//

    /**
     * Process the specified Request, and generate the corresponding Response,
     * according to the design of this particular Container.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     *
     * @exception IOException if an input/output error occurred while
     *  processing
     * @exception ServletException if a ServletException was thrown
     *  while processing this request
     */
    public void invoke(Request request, Response response)
            throws IOException, ServletException;


    /**
     * Return the child Container that should be used to process this Request,
     * based upon its characteristics.  If no such child Container can be
     * identified, return <code>null</code> instead.
     *
     * @param request Request being processed
     * @param update Update the Request to reflect the mapping selection?
     */
    public Container map(Request request, boolean update);



//    /**
//     * Remove a container event listener from this component.
//     *
//     * @param listener The listener to remove
//     */
//    public void removeContainerListener(ContainerListener listener);
//
//
//    /**
//     * Remove a Mapper associated with this Container, if any.
//     *
//     * @param mapper The Mapper to be removed
//     */
//    public void removeMapper(Mapper mapper);


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

}
