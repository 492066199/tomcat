package com.sailing.tomcat.loader;

import com.sailing.tomcat.container.Container;

import java.beans.PropertyChangeListener;


public interface Loader {

    ClassLoader getClassLoader();

    Container getContainer();

    String getInfo();

    void setContainer(Container container);


//    /**
//     * Return the DefaultContext with which this Manager is associated.
//     */
//    public DefaultContext getDefaultContext();
//
//
//    /**
//     * Set the DefaultContext with which this Manager is associated.
//     *
//     * @param defaultContext The newly associated DefaultContext
//     */
//    public void setDefaultContext(DefaultContext defaultContext);
    

    /**
     * Return the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     */
    public boolean getDelegate();


    /**
     * Set the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     *
     * @param delegate The new flag
     */
    public void setDelegate(boolean delegate);

    /**
     * Return the reloadable flag for this Loader.
     */
    public boolean getReloadable();


    /**
     * Set the reloadable flag for this Loader.
     *
     * @param reloadable The new reloadable flag
     */
    public void setReloadable(boolean reloadable);


    // --------------------------------------------------------- Public Methods


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);


    /**
     * Add a new repository to the set of repositories for this class loader.
     *
     * @param repository Repository to be added
     */
    public void addRepository(String repository);


    /**
     * Return the set of repositories defined for this class loader.
     * If none are defined, a zero-length array is returned.
     */
    public String[] findRepositories();


    /**
     * Has the internal repository associated with this Loader been modified,
     * such that the loaded classes should be reloaded?
     */
    public boolean modified();


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);


}
