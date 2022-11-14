package io.github.warhead501.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.command.OmniSubCommand;

public abstract class SimpleCommand implements OmniSubCommand {
    private final ImmutableList<String> commands;

    public SimpleCommand(ImmutableList<String> commands) {
        this.commands = commands;
    }

    @Override
    public boolean isCommand(String command) {
        return getCommand().equalsIgnoreCase(command) || commands.contains(command);
    }
}
