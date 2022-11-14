package io.github.warhead501.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.warhead501.omniscience.api.OmniApi;
import io.github.warhead501.omniscience.api.interfaces.IOmniscience;
import io.github.warhead501.omniscience.api.util.Formatter;
import io.github.warhead501.omniscience.command.result.CommandResult;
import io.github.warhead501.omniscience.command.result.UseResult;
import org.bukkit.command.CommandSender;

import java.util.List;

public class EventsCommand extends SimpleCommand {

    public EventsCommand() {
        super(ImmutableList.of("e"));
    }

    @Override
    public UseResult canRun(CommandSender sender) {
        return hasPermission(sender, "omniscience.commands.events");
    }

    @Override
    public String getCommand() {
        return "events";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Get a list of enabled, searchable events";
    }

    @Override
    public CommandResult run(CommandSender sender, IOmniscience core, String[] args) {
        sender.sendMessage(Formatter.success("Enabled Events: "));
        List<String> enabledEvents = OmniApi.getEnabledEvents();
        enabledEvents.sort(String::compareToIgnoreCase);
        sender.sendMessage(Formatter.bonus(String.join(", ", enabledEvents)));
        return CommandResult.success();
    }

    @Override
    public void buildLiteralArgumentBuilder(LiteralArgumentBuilder<Object> builder) {

    }

    @Override
    public List<String> getCommandSuggestions(String partial) {
        return null;
    }
}
