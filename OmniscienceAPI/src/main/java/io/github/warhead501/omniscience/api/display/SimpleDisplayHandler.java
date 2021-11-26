package io.github.warhead501.omniscience.api.display;

public abstract class SimpleDisplayHandler implements DisplayHandler {

    private final String displayTag;

    public SimpleDisplayHandler(String displayTag) {
        this.displayTag = displayTag;
    }

    @Override
    public boolean handles(String displayTag) {
        return this.displayTag.equalsIgnoreCase(displayTag);
    }
}
