package com.sailing.tomcat.container.impl;


import com.google.common.collect.Maps;
import com.sailing.tomcat.container.*;
import com.sailing.tomcat.container.Mapper;
import com.sailing.tomcat.life.Lifecycle;
import com.sailing.tomcat.life.LifecycleException;
import com.sailing.tomcat.life.LifecycleListener;
import com.sailing.tomcat.life.LifecycleSupport;
import com.sailing.tomcat.loader.Loader;
import com.sailing.tomcat.logger.Logger;
import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;
import com.sailing.tomcat.security.LoginConfig;
import com.sailing.tomcat.security.Realm;
import com.sailing.tomcat.security.SecurityConstraint;
import com.sailing.tomcat.session.Manager;
import com.sailing.tomcat.wrapper.FilterDef;
import com.sailing.tomcat.wrapper.FilterMap;

import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;


public class SimpleContext implements Context, Pipeline, Lifecycle {

    public SimpleContext() {
        pipeline.setBasic(new SimpleContextValve());
    }

    protected Loader loader = null;
    protected SimplePipeline pipeline = new SimplePipeline(this);
    protected Map<String, String> servletMappings = Maps.newConcurrentMap();

    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    //default
    protected Mapper mapper = null;
    protected Map<String, Mapper> mappers = Maps.newConcurrentMap();

    protected Map<String, Container> children = Maps.newConcurrentMap();
    private Container parent = null;
    protected boolean started = false;
    private Logger logger;

    public Container getParent() {
        return parent;
    }

    public void setParent(Container container) {
        this.parent = container;
    }

    public void addChild(Container child) {
        child.setParent(this);
        children.put(child.getName(), child);
    }

    // method implementations of Pipeline
    public Valve getBasic() {
        return pipeline.getBasic();
    }

    public void setBasic(Valve valve) {
        pipeline.setBasic(valve);
    }

    public synchronized void addValve(Valve valve) {
        pipeline.addValve(valve);
    }

    public Valve[] getValves() {
        return pipeline.getValves();
    }

    public void removeValve(Valve valve) {
        pipeline.removeValve(valve);
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        pipeline.invoke(request, response);
    }

    public void addMapper(Mapper mapper) {
        // this method is adopted from addMapper in ContainerBase
        // the first mapper added becomes the default mapper
        mapper.setContainer(this);      // May throw IAE
        //set default
        this.mapper = mapper;

        if (mappers.get(mapper.getProtocol()) != null) {
            throw new IllegalArgumentException("addMapper:  Protocol '" + mapper.getProtocol() + "' is not unique");
        }
        mapper.setContainer(this);      // May throw IAE
        mappers.put(mapper.getProtocol(), mapper);
        if (mappers.size() == 1) {
            this.mapper = mapper;
        } else {
            this.mapper = null;
        }
    }

    public Mapper findMapper(String protocol) {
        if (mapper != null) {
            return (mapper);
        }else {
            return mappers.get(protocol);
        }
    }

    public Container map(Request request, boolean update) {
        //this method is taken from the map method in org.apache.cataline.core.ContainerBase
        //the findMapper method always returns the default mapper, if any, regardless the
        //request's protocol
        Mapper mapper = findMapper(request.getRequest().getProtocol());
        if (mapper == null) {
            return null;
        }
        // Use this Mapper to perform this mapping
        return mapper.map(request, update);
    }

    @Override
    public void removeContainerListener(ContainerListener listener) {

    }

    public void addServletMapping(String pattern, String name) {
        servletMappings.put(pattern, name);
    }

    public String findServletMapping(String pattern) {
        return servletMappings.get(pattern);
    }

    public Container findChild(String name) {
        if (name == null)
            return null;
        return children.get(name);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        this.lifecycle.addLifecycleListener(listener);
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        this.lifecycle.removeLifecycleListener(listener);
    }

    @Override
    public void start() throws LifecycleException {
        if (started) {
            throw new LifecycleException("SimpleContext has already started");
        }

        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);
        started = true;
        try {
            //loader
            if ((loader != null) && (loader instanceof Lifecycle)) {
                ((Lifecycle) loader).start();
            }

            //childern
            Container children[] = this.findChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof Lifecycle) {
                    ((Lifecycle) children[i]).start();
                }
            }

            //pipeline
            if (pipeline instanceof Lifecycle) {
                ((Lifecycle) pipeline).start();
            }

            lifecycle.fireLifecycleEvent(START_EVENT, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);
    }

    @Override
    public void stop() throws LifecycleException {
        if (!started) {
            throw new LifecycleException("SimpleContext has not been started");
        }
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        try {
            //pipeline
            if (pipeline instanceof Lifecycle) {
                ((Lifecycle) pipeline).stop();
            }

            //childern
            Container children[] = findChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof Lifecycle)
                    ((Lifecycle) children[i]).stop();
            }

            //loader
            if ((loader != null) && (loader instanceof Lifecycle)) {
                ((Lifecycle) loader).stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);
    }

    public Mapper[] findMappers() {
        return null;
    }

    public void removeMapper(Mapper mapper) {

    }

    public Object[] getApplicationListeners() {
        return null;
    }

    public void setApplicationListeners(Object listeners[]) {
    }

    public boolean getAvailable() {
        return false;
    }

    public void setAvailable(boolean flag) {
    }
    //    public void setCharsetMapper(CharsetMapper mapper) {
    //
    //    }
    //        return null;
//    public CharsetMapper getCharsetMapper() {

//    }

    public boolean getConfigured() {
        return false;
    }

    public void setConfigured(boolean configured) {
    }

    public boolean getCookies() {
        return false;
    }

    public void setCookies(boolean cookies) {
    }

    public boolean getCrossContext() {
        return false;
    }

    public void setCrossContext(boolean crossContext) {
    }

    public String getDisplayName() {
        return null;
    }

    public void setDisplayName(String displayName) {
    }

    public boolean getDistributable() {
        return false;
    }

    public void setDistributable(boolean distributable) {
    }

    public String getDocBase() {
        return null;
    }

    public void setDocBase(String docBase) {
    }

    @Override
    public LoginConfig getLoginConfig() {
        return null;
    }

    @Override
    public void setLoginConfig(LoginConfig config) {

    }
    //    public void setNamingResources(NamingResources namingResources) {
    //
    //    }
    //        return null;
    //    public NamingResources getNamingResources() {
    //
    //    }
    //    public void setLoginConfig(LoginConfig config) {
    //
    //    }
    //        return null;
//    public LoginConfig getLoginConfig() {

//    }

    public String getPath() {
        return null;
    }

    public void setPath(String path) {
    }

    public String getPublicId() {
        return null;
    }

    public void setPublicId(String publicId) {
    }

    public boolean getReloadable() {
        return false;
    }

    public void setReloadable(boolean reloadable) {
    }

    public boolean getOverride() {
        return false;
    }

    public void setOverride(boolean override) {
    }

    public boolean getPrivileged() {
        return false;
    }

    public void setPrivileged(boolean privileged) {
    }

    public ServletContext getServletContext() {
        return null;
    }

    public int getSessionTimeout() {
        return 0;
    }

    public void setSessionTimeout(int timeout) {
    }

    public String getWrapperClass() {
        return null;
    }

    public void setWrapperClass(String wrapperClass) {
    }

    public void addApplicationListener(String listener) {
    }

    @Override
    public void addConstraint(SecurityConstraint constraint) {

    }

    @Override
    public void addFilterDef(FilterDef filterDef) {

    }

    @Override
    public void addFilterMap(FilterMap filterMap) {

    }
    //    public void addFilterMap(FilterMap filterMap) {
    //
    //    }
    //    public void addFilterDef(FilterDef filterDef) {
    //
    //    }
    //    public void addErrorPage(ErrorPage errorPage) {
    //
    //    }
    //    public void addEnvironment(ContextEnvironment environment) {
    //
    //    }
    //    public void addEjb(ContextEjb ejb) {
    //
    //    }
    //    public void addConstraint(SecurityConstraint constraint) {
    //
    //    }
//    public void addApplicationParameter(ApplicationParameter parameter) {

//    }

    public void addInstanceListener(String listener) {
    }
//    public void addLocalEjb(ContextLocalEjb ejb) {

//    }

    public void addMimeMapping(String extension, String mimeType) {
    }

    public void addParameter(String name, String value) {
    }
//    public void addResource(ContextResource resource) {

//    }

    public void addResourceEnvRef(String name, String type) {
    }
//    public void addResourceLink(ContextResourceLink resourceLink) {

//    }

    public void addRoleMapping(String role, String link) {
    }

    public void addSecurityRole(String role) {
    }

    public void addTaglib(String uri, String location) {
    }

    public void addWelcomeFile(String name) {
    }

    public void addWrapperLifecycle(String listener) {
    }

    public void addWrapperListener(String listener) {
    }

    public Wrapper createWrapper() {
        return null;
    }

    public String[] findApplicationListeners() {
        return null;
    }

    @Override
    public SecurityConstraint[] findConstraints() {
        return new SecurityConstraint[0];
    }

    @Override
    public FilterDef findFilterDef(String filterName) {
        return null;
    }

    @Override
    public FilterDef[] findFilterDefs() {
        return new FilterDef[0];
    }

    @Override
    public FilterMap[] findFilterMaps() {
        return new FilterMap[0];
    }
    //        return null;
    //    public FilterMap[] findFilterMaps() {
    //
    //    }
    //        return null;
    //    public FilterDef[] findFilterDefs() {
    //
    //    }
    //        return null;
    //    public FilterDef findFilterDef(String filterName) {
    //
    //    }
    //        return null;
    //    public ErrorPage[] findErrorPages() {
    //
    //    }
    //        return null;
    //    public ErrorPage findErrorPage(String exceptionType) {
    //
    //    }
    //        return null;
    //    public ErrorPage findErrorPage(int errorCode) {
    //
    //    }
    //        return null;
    //    public ContextEnvironment[] findEnvironments() {
    //
    //    }
    //        return null;
    //    public ContextEnvironment findEnvironment(String name) {
    //
    //    }
    //        return null;
    //    public ContextEjb[] findEjbs() {
    //
    //    }
    //        return null;
    //    public ContextEjb findEjb(String name) {
    //
    //    }
    //        return null;
    //    public SecurityConstraint[] findConstraints() {
    //
    //    }
    //        return null;
//    public ApplicationParameter[] findApplicationParameters() {

//    }

    public String[] findInstanceListeners() {
        return null;
    }
    //        return null;
    //    public ContextLocalEjb[] findLocalEjbs() {
    //
    //    }
    //        return null;
//    public ContextLocalEjb findLocalEjb(String name) {

//    }

    public String findMimeMapping(String extension) {
        return null;
    }

    public String[] findMimeMappings() {
        return null;
    }

    public String findParameter(String name) {
        return null;
    }

    public String[] findParameters() {
        return null;
    }
    //        return null;
//    public ContextResource findResource(String name) {

//    }

    public String findResourceEnvRef(String name) {
        return null;
    }

    public String[] findResourceEnvRefs() {
        return null;
    }
    //        return null;
    //    public ContextResource[] findResources() {
    //
    //    }
    //        return null;
    //    public ContextResourceLink[] findResourceLinks() {
    //
    //    }
    //        return null;
//    public ContextResourceLink findResourceLink(String name) {

//    }

    public String findRoleMapping(String role) {
        return null;
    }

    public boolean findSecurityRole(String role) {
        return false;
    }

    public String[] findSecurityRoles() {
        return null;
    }

    public String[] findServletMappings() {
        return null;
    }

    public String findStatusPage(int status) {
        return null;
    }

    public int[] findStatusPages() {
        return null;
    }

    public String findTaglib(String uri) {
        return null;
    }

    public String[] findTaglibs() {
        return null;
    }

    public boolean findWelcomeFile(String name) {
        return false;
    }

    public String[] findWelcomeFiles() {
        return null;
    }

    public String[] findWrapperLifecycles() {
        return null;
    }

    public String[] findWrapperListeners() {
        return null;
    }

    public void reload() {
    }

    public void removeApplicationListener(String listener) {
    }

    public void removeApplicationParameter(String name) {
    }

    @Override
    public void removeConstraint(SecurityConstraint constraint) {

    }
//    public void removeConstraint(SecurityConstraint constraint) {

//    }

    public void removeEjb(String name) {
    }

    public void removeEnvironment(String name) {
    }

    @Override
    public void removeFilterDef(FilterDef filterDef) {

    }

    @Override
    public void removeFilterMap(FilterMap filterMap) {

    }
    //    public void removeFilterMap(FilterMap filterMap) {
    //
    //    }
    //    public void removeFilterDef(FilterDef filterDef) {
    //
    //    }
//    public void removeErrorPage(ErrorPage errorPage) {

//    }

    public void removeInstanceListener(String listener) {
    }

    public void removeLocalEjb(String name) {
    }

    public void removeMimeMapping(String extension) {
    }

    public void removeParameter(String name) {
    }

    public void removeResource(String name) {
    }

    public void removeResourceEnvRef(String name) {
    }

    public void removeResourceLink(String name) {
    }

    public void removeRoleMapping(String role) {
    }

    public void removeSecurityRole(String role) {
    }

    public void removeServletMapping(String pattern) {
    }

    public void removeTaglib(String uri) {
    }

    public void removeWelcomeFile(String name) {
    }

    public void removeWrapperLifecycle(String listener) {
    }

    public void removeWrapperListener(String listener) {
    }

    //methods of the Container interface

    public String getInfo() {
        return null;
    }

    public Loader getLoader() {
        if (loader != null)
            return (loader);
        if (parent != null)
            return (parent.getLoader());
        return (null);
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Manager getManager() {
        return null;
    }

    @Override
    public void setManager(Manager manager) {

    }

    public Logger getLogger() {
        return logger;
    }

    //    public void setCluster(Cluster cluster) {
    //
    //    }
    //        return null;
    //    public Cluster getCluster() {
    //
    //    }
    //    public void setManager(Manager manager) {
    //
    //    }
    //        return null;
    //    public Manager getManager() {
    //
    //    }

    public String getName() {
        return null;
    }

    public void setName(String name) {
    }

    public ClassLoader getParentClassLoader() {
        return null;
    }

    public void setParentClassLoader(ClassLoader parent) {
    }

    @Override
    public Realm getRealm() {
        return null;
    }

    @Override
    public void setRealm(Realm realm) {

    }

//    public Realm getRealm() {
//        return null;
//    }
//
//    public void setRealm(Realm realm) {
//    }

    public DirContext getResources() {
        return null;
    }

    public void setResources(DirContext resources) {
    }

    @Override
    public void addContainerListener(ContainerListener listener) {

    }

//    public void addContainerListener(ContainerListener listener) {
//    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public ContainerListener[] findContainerListeners() {
        return new ContainerListener[0];
    }

    public Container[] findChildren() {
        synchronized (children) {
            Container results[] = new Container[children.size()];
            return ((Container[]) children.values().toArray(results));
        }
    }

//    public ContainerListener[] findContainerListeners() {
//        return null;
//    }

    public void removeChild(Container child) {
    }

//    public void removeContainerListener(ContainerListener listener) {
//    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}