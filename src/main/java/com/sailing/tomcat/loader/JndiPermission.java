package com.sailing.tomcat.loader;

import java.security.BasicPermission;

public final class JndiPermission extends BasicPermission {

    public JndiPermission(String name) {
        super(name);
    }

    public JndiPermission(String name, String actions) {
        super(name,actions);
    }

}
