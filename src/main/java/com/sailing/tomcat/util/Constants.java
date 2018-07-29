package com.sailing.tomcat.util;

public class Constants {
    public static final String WEB_ROOT = "/root/tomcat/target";
    public static final String Package = "com.sailing.small.tomcat";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    public static final int PROCESSOR_IDLE = 0;
    public static final int PROCESSOR_ACTIVE = 1;

    public static final String JSP_SERVLET_CLASS =
            "org.apache.jasper.servlet.JspServlet";
    public static final String JSP_SERVLET_NAME = "jsp";

    public static final int MAJOR_VERSION = 2;
    public static final int MINOR_VERSION = 3;


    public static final String ApplicationWebXml = "web.xml";

    public static final String DefaultWebXml = "conf/web.xml";

    public static final String TldDtdPublicId_11 =
            "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN";
    public static final String TldDtdResourcePath_11 =
            //        "conf/tld_11.dtd";
            "/javax/servlet/jsp/resources/web-jsptaglibrary_1_1.dtd";

    public static final String TldDtdPublicId_12 =
            "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
    public static final String TldDtdResourcePath_12 =
            //        "conf/tld_12.dtd";
            "/javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd";

//    public static final String WebDtdPublicId_22 =
//            "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
//    public static final String WebDtdResourcePath_22 =
//            //      "conf/web_22.dtd";
//            "/javax/servlet/resources/web-app_2_2.dtd";

    public static final String WebDtdPublicId_23 =
            "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public static final String WebDtdResourcePath_23 =
                  "/dtd/web-app_2_3.dtd";
//            "/javax/servlet/resources/web-app_2_3.dtd";

    public static final String PROTOCOL_HANDLER_VARIABLE =
            "java.protocol.handler.pkgs";

    // Default namespace name
    public static final String DEFAULT_NAMESPACE = "DAV:";
}