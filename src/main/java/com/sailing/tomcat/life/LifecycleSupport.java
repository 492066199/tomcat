package com.sailing.tomcat.life;

import com.google.common.collect.Lists;
import java.util.List;

public final class LifecycleSupport {

    private Lifecycle lifecycle = null;
    private List<LifecycleListener> listeners = Lists.newCopyOnWriteArrayList();

    public LifecycleSupport(Lifecycle lifecycle) {
        super();
        this.lifecycle = lifecycle;
    }

    public void addLifecycleListener(LifecycleListener listener) {
        listeners.add(listener);
    }

    public LifecycleListener[] findLifecycleListeners() {
        return listeners.toArray(new LifecycleListener[0]);
    }

    public void fireLifecycleEvent(String type, Object data) {
        LifecycleEvent event = new LifecycleEvent(lifecycle, type, data);
        for (LifecycleListener listener : listeners) {
            listener.lifecycleEvent(event);
        }
    }

    public void removeLifecycleListener(LifecycleListener listener) {
        listeners.remove(listener);
    }
}
