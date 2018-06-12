package com.sailing.tomcat;

import com.sailing.tomcat.connector.HttpConnector;
import com.sailing.tomcat.container.SimpleContainer;

public final class Bootstrap {
    public static void main(String[] args) {

        HttpConnector connector = new HttpConnector();
        SimpleContainer container = new SimpleContainer();
        connector.setContainer(container);
        try {
            connector.initialize();
            connector.start();

            // make the application wait until we press any key.
            System.in.read();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
