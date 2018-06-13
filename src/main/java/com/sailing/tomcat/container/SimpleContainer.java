package com.sailing.tomcat.container;

import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;
import com.sailing.tomcat.util.Constants;

import javax.naming.directory.DirContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

public class SimpleContainer implements Container {

  public SimpleContainer() {
  }

  public String getInfo() {
    return null;
  }

  public String getName() {
    return null;
  }

  public void setName(String name) {
  }

  public Container getParent() {
    return null;
  }

  public void setParent(Container container) {
  }

  public ClassLoader getParentClassLoader() {
    return null;
  }

  public void setParentClassLoader(ClassLoader parent) {
  }


  public DirContext getResources() {
    return null;
  }

  public void setResources(DirContext resources) {
  }

  public void addChild(Container child) {
  }


  public void addPropertyChangeListener(PropertyChangeListener listener) {
  }

  public Container findChild(String name) {
    return null;
  }

  public Container[] findChildren() {
    return null;
  }

  public void invoke(Request request, Response response)
    throws IOException, ServletException {

    String servletName = ((HttpServletRequest) request).getRequestURI();
    servletName = servletName.substring(servletName.lastIndexOf("/") + 1);
    URLClassLoader loader = null;
    try {
      URL[] urls = new URL[1];
      URLStreamHandler streamHandler = null;
      File classPath = new File(Constants.WEB_ROOT);
      String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
      urls[0] = new URL(null, repository, streamHandler);
      loader = new URLClassLoader(urls);
    } catch (IOException e) {
      System.out.println(e.toString());
    }
    Class myClass = null;
    try {
      servletName =  "com.sailing.tomcat.servlet." + servletName;
      myClass = loader.loadClass(servletName);
    } catch (ClassNotFoundException e) {
      System.out.println(e.toString());
    }

    Servlet servlet = null;

    try {
      servlet = (Servlet) myClass.newInstance();
      servlet.service((HttpServletRequest) request, (HttpServletResponse) response);
    } catch (Exception e) {
      System.out.println(e.toString());
    } catch (Throwable e) {
      System.out.println(e.toString());
    }
  }

  @Override
  public Container map(Request request, boolean update) {
    return null;
  }


  public void removeChild(Container child) {
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
  }

}