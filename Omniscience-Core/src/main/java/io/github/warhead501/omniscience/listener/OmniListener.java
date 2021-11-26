package io.github.warhead501.omniscience.listener;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.OmniEventRegistrar;
import org.bukkit.event.Listener;

public abstract class OmniListener implements Listener {

    private final ImmutableList<String> events;
    private boolean enabled;

    public OmniListener(ImmutableList<String> events) {
        this.events = events;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean handles(String event) {
        return events.contains(event);
    }

    protected boolean isEnabled(String event) {
        return OmniEventRegistrar.INSTANCE.isEventEnabled(event);
    }
}
