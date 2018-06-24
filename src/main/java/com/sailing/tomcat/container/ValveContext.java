package com.sailing.tomcat.container;


import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;

import javax.servlet.ServletException;
import java.io.IOException;

public interface ValveContext {
    String getInfo();
    void invokeNext(Request request, Response response) throws IOException, ServletException;
}
