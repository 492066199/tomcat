package com.sailing.tomcat;

import com.sailing.tomcat.connector.HttpConnector;

public final class Bootstrap {
    public static void main(String[] args) {
        HttpConnector connector = new HttpConnector();
        connector.start();
    }
}
