package io.github.warhead501.omniscience.api.util;

public class PastTenseWithEnabled {
    private final String pastTense;
    private boolean enabled;

    public PastTenseWithEnabled(boolean enabled, String pastTense) {
        this.enabled = enabled;
        this.pastTense = pastTense;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPastTense() {
        return pastTense;
    }
}
