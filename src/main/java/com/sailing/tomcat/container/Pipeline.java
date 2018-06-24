package com.sailing.tomcat.container;

import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;

import javax.servlet.ServletException;
import java.io.IOException;


public interface Pipeline {
    Valve getBasic();
    void setBasic(Valve valve);


    void addValve(Valve valve);
    Valve[] getValves();


    void invoke(Request request, Response response) throws IOException, ServletException;
    void removeValve(Valve valve);
}
