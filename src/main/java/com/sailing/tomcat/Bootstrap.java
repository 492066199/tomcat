package com.sailing.tomcat;

import com.sailing.tomcat.connector.HttpConnector;
import com.sailing.tomcat.container.*;
import com.sailing.tomcat.container.impl.ClientIPLoggerValve;
import com.sailing.tomcat.container.impl.HeaderLoggerValve;
import com.sailing.tomcat.container.impl.SimpleLoader;
import com.sailing.tomcat.container.impl.SimpleWrapper;

public final class Bootstrap {
    public static void main(String[] args) {

        HttpConnector connector = new HttpConnector();
        Wrapper wrapper = new SimpleWrapper();
        wrapper.setServletClass("com.sailing.tomcat.servlet.ModernServlet");
        Loader loader = new SimpleLoader();
        Valve valve1 = new HeaderLoggerValve();
        Valve valve2 = new ClientIPLoggerValve();

        wrapper.setLoader(loader);
        ((Pipeline) wrapper).addValve(valve1);
        ((Pipeline) wrapper).addValve(valve2);

        connector.setContainer(wrapper);

        try {
            connector.initialize();
            connector.start();

            // make the application wait until we press a key.
            System.in.read();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
