package com.sailing.tomcat.processor;

import com.sailing.tomcat.servlet.HttpRequest;
import com.sailing.tomcat.servlet.HttpResponse;

import java.io.IOException;

public class StaticResourceProcessor {
    public void process(HttpRequest request, HttpResponse response) {
        try {
            response.sendStaticResource();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
