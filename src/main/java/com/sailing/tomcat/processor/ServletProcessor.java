package com.sailing.tomcat.processor;

import com.sailing.tomcat.util.Constants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

public class ServletProcessor{
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    String uri = request.getRequestURI();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader = null;
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(Constants.WEB_ROOT);
            // the forming of repository is taken from the
            // createClassLoader method in
            // org.apache.catalina.startup.ClassLoaderFactory
            String repository =
                    (new URL("file", null, classPath.getCanonicalPath() +
                            File.separator)).toString();
            // the code for forming the URL is taken from
            // the addRepository method in
            // org.apache.catalina.loader.StandardClassLoader.
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
	    e.printStackTrace();
            System.out.println(e.toString());
        }

        Class myClass = null;
        try {
            servletName = "com.sailing.tomcat.servlet." + servletName;
            myClass = loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
        Servlet servlet = null;
        try {
            servlet = (Servlet) myClass.newInstance();
            servlet.service((ServletRequest) request,
                    (ServletResponse) response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
}
