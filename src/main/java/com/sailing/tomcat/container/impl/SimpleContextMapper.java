package com.sailing.tomcat.container.impl;

import com.sailing.tomcat.container.Container;
import com.sailing.tomcat.container.Wrapper;
import com.sailing.tomcat.container.Mapper;
import com.sailing.tomcat.request.HttpRequest;
import com.sailing.tomcat.request.Request;

import javax.servlet.http.HttpServletRequest;


public class SimpleContextMapper implements Mapper {

    private SimpleContext context = null;
    private String protocol;

    public Container getContainer() {
        return (context);
    }

    public void setContainer(Container container) {
        if (!(container instanceof SimpleContext)) {
            throw new IllegalArgumentException("Illegal type of container");
        }
        context = (SimpleContext) container;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Container map(Request request, boolean update) {
        // Identify the context-relative URI to be mapped
        String contextPath = ((HttpServletRequest) request.getRequest()).getContextPath();
        String requestURI = ((HttpRequest) request).getDecodedRequestURI();
        String relativeURI = requestURI.substring(contextPath.length());

        String servletPath = relativeURI;
        String name = context.findServletMapping(relativeURI);
        return  (Wrapper) context.findChild(name);
    }
}