package com.sailing.tomcat.container.impl;

import com.sailing.tomcat.container.*;
import com.sailing.tomcat.request.Request;
import com.sailing.tomcat.response.Response;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimplePipeline implements Pipeline {

    protected Valve basic = null;
    protected Container container = null;
    protected List<Valve> valves = new ArrayList<>();

    public SimplePipeline(Container container) {
        this.container = container;
    }

    public Valve getBasic() {
        return basic;
    }

    public void setBasic(Valve valve) {
        this.basic = valve;
        ((Contained) valve).setContainer(container);
    }

    public void addValve(Valve valve) {
        if (valve instanceof Contained)
            ((Contained) valve).setContainer(this.container);

        synchronized (valves) {
            valves.add(valve);
        }
    }

    public Valve[] getValves() {
        synchronized (valves) {
            return valves.toArray(new Valve[valves.size()]);
        }
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        (new SimplePipelineValveContext()).invokeNext(request, response);
    }

    public void removeValve(Valve valve) {

    }

    protected class SimplePipelineValveContext implements ValveContext {

        protected int stage = 0;

        public String getInfo() {
            return null;
        }

        public void invokeNext(Request request, Response response)
                throws IOException, ServletException {
            int subscript = stage;
            stage = stage + 1;
            // Invoke the requested Valve for the current request thread
            if (subscript < valves.size()) {
                valves.get(subscript).invoke(request, response, this);
            } else if ((subscript == valves.size()) && (basic != null)) {
                basic.invoke(request, response, this);
            } else {
                throw new ServletException("No valve");
            }
        }
    }
}
