package com.sailing.tomcat.logger;

public class SystemOutLogger extends LoggerBase {

    protected static final String info = "org.apache.catalina.logger.SystemOutLogger/1.0";

    public void log(String msg) {
        System.out.println(msg);
    }
}
