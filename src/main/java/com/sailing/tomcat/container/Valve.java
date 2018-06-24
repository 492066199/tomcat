package com.sailing.tomcat.container;

import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;

import javax.servlet.ServletException;
import java.io.IOException;


public interface Valve {

    String getInfo();

    void invoke(Request request, Response response, ValveContext context) throws IOException, ServletException;
}
