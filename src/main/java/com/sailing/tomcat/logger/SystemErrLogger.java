package com.sailing.tomcat.logger;

public class SystemErrLogger extends LoggerBase {
    protected static final String info = "org.apache.catalina.logger.SystemErrLogger/1.0";

    public void log(String msg) {
        System.err.println(msg);
    }
}
