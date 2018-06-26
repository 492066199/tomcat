package com.sailing.tomcat.container;

import com.sailing.tomcat.container.Container;
import com.sailing.tomcat.request.Request;

public interface Mapper {
    Container getContainer();

    void setContainer(Container container);

    String getProtocol();

    void setProtocol(String protocol);

    Container map(Request request, boolean update);
}
