package io.github.warhead501.omniscience.api.flag;

import com.google.common.collect.ImmutableList;

public abstract class BaseFlagHandler implements FlagHandler {
    private final ImmutableList<String> aliases;

    public BaseFlagHandler(ImmutableList<String> aliases) {
        this.aliases = aliases;
    }

    @Override
    public boolean handles(String alias) {
        return aliases.contains(alias);
    }

    @Override
    public ImmutableList<String> getAliases() {
        return aliases;
    }
}
