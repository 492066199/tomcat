package com.sailing.tomcat;

import com.sailing.tomcat.connector.Connector;
import com.sailing.tomcat.connector.HttpConnector;
import com.sailing.tomcat.container.Context;
import com.sailing.tomcat.container.Wrapper;
import com.sailing.tomcat.context.StandardContext;
import com.sailing.tomcat.engine.Engine;
import com.sailing.tomcat.engine.StandardEngine;
import com.sailing.tomcat.host.Host;
import com.sailing.tomcat.host.StandardHost;
import com.sailing.tomcat.life.Lifecycle;
import com.sailing.tomcat.life.LifecycleListener;
import com.sailing.tomcat.loader.Loader;
import com.sailing.tomcat.loader.WebappLoader;
import com.sailing.tomcat.security.SimpleContextConfig;
import com.sailing.tomcat.wrapper.StandardWrapper;

public class Bootstartp2 {
    public static void main(String[] args) {
        System.setProperty("catalina.base", System.getProperty("user.dir"));

        Connector connector = new HttpConnector();

        Wrapper wrapper1 = new StandardWrapper();
        wrapper1.setName("Primitive");
        wrapper1.setServletClass("com.sailing.tomcat.servlet.PrimitiveServlet");
        Wrapper wrapper2 = new StandardWrapper();
        wrapper2.setName("Modern");
        wrapper2.setServletClass("com.sailing.tomcat.servlet.ModernServlet");

        Context context = new StandardContext();
        // StandardContext's start method adds a default mapper
        context.setPath("/app1");
        context.setDocBase("app1");
        context.addChild(wrapper1);
        context.addChild(wrapper2);

        LifecycleListener listener = new SimpleContextConfig();
        ((Lifecycle) context).addLifecycleListener(listener);

        Host host = new StandardHost();
        host.addChild(context);
        host.setName("localhost");
        host.setAppBase("webapps");

        Loader loader = new WebappLoader();
        context.setLoader(loader);

        // context.addServletMapping(pattern, name);
        context.addServletMapping("/Primitive", "Primitive");
        context.addServletMapping("/Modern", "Modern");

        Engine engine = new StandardEngine();
        engine.addChild(host);
        engine.setDefaultHost("localhost");

        connector.setContainer(engine);
        try {
            connector.initialize();
            ((Lifecycle) connector).start();
            ((Lifecycle) engine).start();
            // make the application wait until we press a key.
            System.in.read();
            ((Lifecycle) engine).stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
