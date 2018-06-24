package com.sailing.tomcat.container.impl;

import com.sailing.tomcat.container.Contained;
import com.sailing.tomcat.container.Container;
import com.sailing.tomcat.container.Valve;
import com.sailing.tomcat.container.ValveContext;
import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


//this is a wrapper basic pipeline!
public class SimpleWrapperValve implements Valve, Contained {

    protected Container container;

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void invoke(Request request, Response response, ValveContext valveContext)
            throws IOException, ServletException {

        SimpleWrapper wrapper = (SimpleWrapper) getContainer();
        ServletRequest sreq = request.getRequest();
        ServletResponse sres = response.getResponse();
        Servlet servlet = null;
        HttpServletRequest hreq = null;
        if (sreq instanceof HttpServletRequest)
            hreq = (HttpServletRequest) sreq;
        HttpServletResponse hres = null;
        if (sres instanceof HttpServletResponse)
            hres = (HttpServletResponse) sres;

        // Allocate a servlet instance to process this request
        try {
            servlet = wrapper.allocate();
            if (hres!=null && hreq!=null) {
                servlet.service(hreq, hres);
            }
            else {
                servlet.service(sreq, sres);
            }
        }
        catch (ServletException e) {
            e.printStackTrace();
        }
    }

    public String getInfo() {
        return "this is a wrapper basic pipeline!";
    }
}
