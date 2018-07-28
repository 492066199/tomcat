package com.sailing.tomcat;

import com.sailing.tomcat.connector.Connector;
import com.sailing.tomcat.connector.HttpConnector;
import com.sailing.tomcat.container.Container;
import com.sailing.tomcat.container.Context;
import com.sailing.tomcat.context.ContextConfig;
import com.sailing.tomcat.context.StandardContext;
import com.sailing.tomcat.host.Host;
import com.sailing.tomcat.host.StandardHost;
import com.sailing.tomcat.life.Lifecycle;
import com.sailing.tomcat.life.LifecycleListener;
import com.sailing.tomcat.loader.Loader;
import com.sailing.tomcat.loader.WebappLoader;

public class Bootstrap16 {
    // invoke: http://localhost:8080/app1/Modern or
    // http://localhost:8080/app2/Primitive
    // note that we don't instantiate a Wrapper here,
    // ContextConfig reads the WEB-INF/classes dir and loads all
    // servlets.
    public static void main(String[] args) {
        System.setProperty("catalina.base", System.getProperty("user.dir"));
        Connector connector = new HttpConnector();
        Context context = new StandardContext();

        // StandardContext's start method adds a default mapper
        context.setPath("/app1");
        context.setDocBase("app1");

        LifecycleListener listener = new ContextConfig();
        ((Lifecycle) context).addLifecycleListener(listener);


        Loader loader = new WebappLoader();
        context.setLoader(loader);

        Host host = new StandardHost();
        host.addChild(context);
        host.setName("localhost");
        host.setAppBase("webapps");

        connector.setContainer(host);
        try {
            connector.initialize();
            ((Lifecycle) connector).start();
            ((Lifecycle) host).start();
            Container[] c = context.findChildren();
            int length = c.length;
            for (int i=0; i<length; i++) {
                Container child = c[i];
                System.out.println(child.getName());
            }
            // make the application wait until we press a key.
            System.in.read();
            ((Lifecycle) host) .stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
