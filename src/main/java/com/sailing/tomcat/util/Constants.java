package com.sailing.tomcat.util;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public class Constants {
    public static final String WEB_ROOT = "/root/tomcat/target";
    public static final String Package = "com.sailing.small.tomcat";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    public static final int PROCESSOR_IDLE = 0;
    public static final int PROCESSOR_ACTIVE = 1;
}