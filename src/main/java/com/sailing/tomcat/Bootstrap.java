package com.sailing.tomcat;

import com.sailing.tomcat.connector.HttpConnector;
import com.sailing.tomcat.container.*;
import com.sailing.tomcat.container.impl.*;
import com.sailing.tomcat.container.Mapper;
import com.sailing.tomcat.life.Lifecycle;
import com.sailing.tomcat.life.LifecycleListener;
import com.sailing.tomcat.life.SimpleContextLifecycleListener;
import com.sailing.tomcat.logger.FileLogger;

public final class Bootstrap {
    public static void main(String[] args) {

        HttpConnector connector = new HttpConnector();

        Wrapper wrapper1 = new SimpleWrapper();
        wrapper1.setName("Primitive");
        wrapper1.setServletClass("com.sailing.tomcat.servlet.PrimitiveServlet");

        Wrapper wrapper2 = new SimpleWrapper();
        wrapper2.setName("Modern");
        wrapper2.setServletClass("com.sailing.tomcat.servlet.ModernServlet");

        Context context = new SimpleContext();

        LifecycleListener listener = new SimpleContextLifecycleListener();
        ((Lifecycle) context).addLifecycleListener(listener);

        context.addChild(wrapper1);
        context.addChild(wrapper2);

        Valve valve1 = new HeaderLoggerValve();
        Valve valve2 = new ClientIPLoggerValve();

        ((Pipeline) context).addValve(valve1);
        ((Pipeline) context).addValve(valve2);

        Mapper mapper = new SimpleContextMapper();
        mapper.setProtocol("http");

        context.addMapper(mapper);

        Loader loader = new SimpleLoader();

        context.setLoader(loader);
        context.addServletMapping("/servlet/PrimitiveServlet", "Primitive");
        context.addServletMapping("/servlet/ModernServlet", "Modern");

        System.setProperty("catalina.base", System.getProperty("user.dir"));
        FileLogger logger = new FileLogger();
        logger.setPrefix("FileLog_");
        logger.setSuffix(".txt");
        logger.setTimestamp(true);
        logger.setVerbosity(0);

        context.setLogger(logger);

        connector.setContainer(context);
        try {
            connector.initialize();
            connector.start();
            ((Lifecycle) context).start();
            // make the application wait until we press a key.
            System.in.read();
            ((Lifecycle) context).stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
