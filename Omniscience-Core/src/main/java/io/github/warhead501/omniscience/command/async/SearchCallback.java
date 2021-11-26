package io.github.warhead501.omniscience.command.async;

import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.display.DisplayHandler;
import io.github.warhead501.omniscience.api.entry.DataAggregateEntry;
import io.github.warhead501.omniscience.api.entry.DataEntry;
import io.github.warhead501.omniscience.api.entry.DataEntryComplete;
import io.github.warhead501.omniscience.api.flag.Flag;
import io.github.warhead501.omniscience.api.parameter.ParameterHandler;
import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.api.util.DataHelper;
import io.github.warhead501.omniscience.api.util.Formatter;
import io.github.warhead501.omniscience.command.commands.PageCommand;
import io.github.warhead501.omniscience.Omniscience;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SearchCallback implements AsyncCallback {

    private final QuerySession session;

    public SearchCallback(QuerySession session) {
        this.session = session;
    }

    @Override
    public void success(List<DataEntry> results) {
        List<BaseComponent[]> components = results.stream().map(this::buildComponent).collect(Collectors.toList());
        PageCommand.setSearchResults(session.getSender(), components);
    }

    @Override
    public void empty() {
        PageCommand.removeSearchResults(session.getSender());
        session.getSender().sendMessage(Formatter.error("Nothing was found. (/omni help)"));
    }

    @Override
    public void error(Exception e) {
        PageCommand.removeSearchResults(session.getSender());
        session.getSender().sendMessage(Formatter.error("An error occurred. Please see console."));
        Omniscience.getPlugin(Omniscience.class).getLogger().log(Level.SEVERE, "An error occurred while parsing!", e);
    }

    private BaseComponent[] buildComponent(DataEntry entry) {
        Optional<DisplayHandler> displayHandler = Optional.empty();
        Optional<String> oDHandler = entry.data.getString(DataKeys.DISPLAY_METHOD);
        if (oDHandler.isPresent()) {
            displayHandler = Omniscience.getDisplayHandler(oDHandler.get());
            if (!displayHandler.isPresent()) {
                Omniscience.getPluginInstance().getLogger().warning("The display handler for the record " + entry.data + " is set to " + oDHandler.get() + ", but no handler was found. Is this an error?");
            }
        }

        StringBuilder startOfMessage = new StringBuilder().append(ChatColor.GRAY);
        StringBuilder endOfMessage = new StringBuilder();
        StringBuilder hoverMessage = new StringBuilder();
        startOfMessage.append(Formatter.formatSecondaryMessage(entry.getSourceName())).append(" ");
        startOfMessage.append(ChatColor.WHITE).append(entry.getVerbPastTense()).append(" ");

        hoverMessage.append(ChatColor.DARK_GRAY).append("Source: ").append(ChatColor.WHITE).append(entry.getSourceName());
        hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Event: ").append(ChatColor.WHITE).append(entry.getEventName());

        entry.data.getInt(DataKeys.QUANTITY)
                .ifPresent(quantity -> {
                    startOfMessage.append(Formatter.formatSecondaryMessage(String.valueOf(quantity))).append(" ");
                    hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Quantity: ").append(ChatColor.WHITE).append(quantity);
                });

        String target = entry.data.getString(DataKeys.TARGET).orElse("Unknown");
        if (displayHandler.isPresent()) {
            target = displayHandler.get().buildTargetMessage(entry, target, this.session).orElse(target);
        }
        if (!target.isEmpty()) {
            target = Formatter.formatPrimaryMessage(target);
            hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Target: ").append(ChatColor.WHITE).append(ChatColor.stripColor(target));
        }
        Optional<TextComponent> targetHover = Optional.empty();
        if (displayHandler.isPresent()) {
            targetHover = displayHandler.get().buildTargetSpecificHoverData(entry, target, this.session);
        }

        if (entry instanceof DataAggregateEntry) {
            entry.data.getInt(DataKeys.COUNT).ifPresent(count -> {
                endOfMessage.append(ChatColor.GREEN).append("x").append(count).append(" ");
                hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Count: ").append(ChatColor.WHITE).append(count);
            });
        }

        displayHandler.ifPresent(
                handler -> handler
                        .buildAdditionalHoverData(entry, this.session)
                        .ifPresent(
                                hoverMessages -> hoverMessages.forEach(hm -> hoverMessage.append("\n").append(hm)
                                )
                        ));

        ComponentBuilder resultBuilder = new ComponentBuilder("");

        if (entry instanceof DataEntryComplete) {
            DataEntryComplete complete = (DataEntryComplete) entry;

            endOfMessage.append(ChatColor.WHITE).append(complete.getRelativeTime());
            hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Time: ").append(ChatColor.WHITE).append(complete.getTime());

            TextComponent start = new TextComponent(startOfMessage.insert(0, ChatColor.GRAY + "= ").toString());
            TextComponent targetComponent = targetHover.orElse(new TextComponent(target));
            targetComponent.setColor(ChatColor.AQUA);
            TextComponent end = new TextComponent(endOfMessage.toString());

            ComponentBuilder holdingBuilder = new ComponentBuilder("");
            ComponentBuilder hoverMessageBuilder = new ComponentBuilder(hoverMessage.toString());

            DataHelper.getLocationFromDataWrapper(complete.data).ifPresent(location -> {
                if (this.session.hasFlag(Flag.EXTENDED)) {
                    holdingBuilder.append("\n").append(" - ").color(ChatColor.GRAY).append(DataHelper.buildLocation(location, true)).color(ChatColor.GRAY);
                }

                hoverMessageBuilder.append("\n").append("Location: ").color(ChatColor.DARK_GRAY).append(DataHelper.buildLocation(location, false)).color(ChatColor.GRAY);
            });

            HoverEvent infoHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessageBuilder.create());
            start.setHoverEvent(infoHover);
            if (!targetHover.isPresent()) {
                targetComponent.setHoverEvent(infoHover);
            }
            end.setHoverEvent(infoHover);
            resultBuilder.append(start).append(targetComponent).append(" ").append(end).append(holdingBuilder.create());
        } else {
            TextComponent main = new TextComponent();
            HoverEvent infoHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage.toString()).create());
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, buildDetailCommand(entry));
            if (entry instanceof DataAggregateEntry) {
                main.addExtra(startOfMessage.insert(0, ChatColor.GRAY + "= (" + ((DataAggregateEntry) entry).getDate() + ") ").toString());
            } else {
                main.addExtra(startOfMessage.insert(0, ChatColor.GRAY + "= ").toString());
            }
            main.setHoverEvent(infoHover);
            main.setClickEvent(clickEvent);
            if (targetHover.isPresent()) {
                main.addExtra(targetHover.get());
                main.addExtra(" ");
            } else {
                main.addExtra(Formatter.formatPrimaryMessage(target + " "));
                main.setHoverEvent(infoHover);
                main.setClickEvent(clickEvent);
            }
            main.addExtra(endOfMessage.toString());
            main.setHoverEvent(infoHover);
            main.setClickEvent(clickEvent);
            resultBuilder.append(main);
        }

        return resultBuilder.create();
    }

    private String buildDetailCommand(DataEntry entry) {
        String action = "a:" + entry.getEventName();
        final String source;
        if (entry.data.get(DataKeys.PLAYER_ID).isPresent()) {
            source = "p:" + entry.getSourceName();
        } else {
            source = "c:" + entry.getSourceName();
        }
        String target = "trg:" + entry.getTargetName().replaceAll(" ", ",");
        StringBuilder command = new StringBuilder("/omniscience search ");
        command
                .append(action)
                .append(" ")
                .append(source)
                .append(" ")
                .append(target)
                .append(" ");
        Optional<ParameterHandler> radius = Omniscience.getParameterHandler("r");
        if (radius.isPresent() && session.isIgnoredDefault(radius.get())) {
            command.append("-g").append(" ");
        } else {
            command.append("r:").append(session.getRadius()).append(" ");
        }
        command.append("-ng");
        Omniscience.logDebug("Click Command: " + command.toString());
        return command.toString();
    }
}
