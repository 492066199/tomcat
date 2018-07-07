package com.sailing.tomcat;

import com.sailing.tomcat.connector.Connector;
import com.sailing.tomcat.connector.HttpConnector;
import com.sailing.tomcat.container.Context;
import com.sailing.tomcat.container.Wrapper;
import com.sailing.tomcat.context.StandardContext;
import com.sailing.tomcat.life.Lifecycle;
import com.sailing.tomcat.life.LifecycleListener;
import com.sailing.tomcat.loader.Loader;
import com.sailing.tomcat.loader.WebappLoader;
import com.sailing.tomcat.security.*;
import com.sailing.tomcat.wrapper.StandardWrapper;

public class Bootstrap1 {
    public static void main(String[] args) {
        System.setProperty("catalina.base",
                System.getProperty("user.dir"));
        Connector connector = new HttpConnector();
        Wrapper wrapper1 = new StandardWrapper();
        wrapper1.setName("Primitive");
        wrapper1.setServletClass("PrimitiveServlet");
        Wrapper wrapper2 = new StandardWrapper();
        wrapper2.setName("Modern");
        wrapper2.setServletClass("ModernServlet");
        Context context = new StandardContext();
        // StandardContext's start method adds a default mapper
        context.setPath("/myApp");
        context.setDocBase("myApp");
        LifecycleListener listener = new SimpleContextConfig();
        ((Lifecycle) context).addLifecycleListener(listener);
        context.addChild(wrapper1);
        context.addChild(wrapper2);
        // for simplicity, we don't add a valve, but you can add
        // valves to context or wrapper just as you did in Chapter 6
        Loader loader = new WebappLoader();
        context.setLoader(loader);
        // context.addServletMapping(pattern, name);
        context.addServletMapping("/Primitive", "Primitive");
        context.addServletMapping("/Modern", "Modern");
        // add ContextConfig. This listener is important because it
        // configures StandardContext (sets configured to true), otherwise
        // StandardContext won't start
        // add constraint
        SecurityCollection securityCollection = new SecurityCollection();
        securityCollection.addPattern("/");
        securityCollection.addMethod("GET");
        SecurityConstraint constraint = new SecurityConstraint();
        constraint.addCollection(securityCollection);
        constraint.addAuthRole("manager");
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setRealmName("Simple Realm");
        // add realm
        Realm realm = new SimpleRealm();
        context.setRealm(realm);
        context.addConstraint(constraint);
        context.setLoginConfig(loginConfig);
        connector.setContainer(context);
        try {
            connector.initialize();
            ((Lifecycle) connector).start();
            ((Lifecycle) context).start();
            // make the application wait until we press a key.
            System.in.read();
            ((Lifecycle) context).stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
