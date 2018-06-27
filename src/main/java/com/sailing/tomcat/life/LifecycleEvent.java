package com.sailing.tomcat.life;

import java.util.EventObject;

public final class LifecycleEvent extends EventObject {
    private Object data = null;
    private Lifecycle lifecycle = null;
    private String type = null;

    public LifecycleEvent(Lifecycle lifecycle, String type) {
        this(lifecycle, type, null);
    }

    public LifecycleEvent(Lifecycle lifecycle, String type, Object data) {
        super(lifecycle);
        this.lifecycle = lifecycle;
        this.type = type;
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    public String getType() {
        return this.type;
    }
}
