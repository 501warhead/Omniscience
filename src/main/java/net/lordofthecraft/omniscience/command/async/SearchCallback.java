package net.lordofthecraft.omniscience.command.async;

import net.lordofthecraft.omniscience.Omniscience;
import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.data.DataWrapper;
import net.lordofthecraft.omniscience.api.entry.DataAggregateEntry;
import net.lordofthecraft.omniscience.api.entry.DataEntry;
import net.lordofthecraft.omniscience.api.entry.DataEntryComplete;
import net.lordofthecraft.omniscience.api.flag.Flag;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import net.lordofthecraft.omniscience.command.commands.PageCommand;
import net.lordofthecraft.omniscience.util.DataHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;
import java.util.List;
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
        session.getSender().sendMessage(ChatColor.RED + "Nothing was found. Check /omni help for help.");
    }

    @Override
    public void error(Exception e) {
        session.getSender().sendMessage(ChatColor.RED + "An error occurred. Please see console.");
        Omniscience.getPlugin(Omniscience.class).getLogger().log(Level.SEVERE, "An error occurred while parsing!", e);
    }

    private BaseComponent[] buildComponent(DataEntry entry) {
        System.out.println("buildComponent 1 " + entry);

        StringBuilder message = new StringBuilder();
        StringBuilder hoverMessage = new StringBuilder();
        message.append(ChatColor.DARK_AQUA).append(entry.getSourceName()).append(" ");
        message.append(ChatColor.WHITE).append(entry.getVerbPastTense()).append(" ");

        System.out.println("buildComponent 2 " + message + ":" + hoverMessage);

        //this would be FUCKING AWESOME to show the item (if there is one)!
        hoverMessage.append(ChatColor.DARK_GRAY).append("Source: ").append(ChatColor.WHITE).append(entry.getSourceName());
        hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Event: ").append(ChatColor.WHITE).append(entry.getEventName());

        System.out.println("buildComponent 3 " + message + ":" + hoverMessage);

        String quantity = entry.data.getString(DataKeys.QUANTITY).orElse(null);
        if (quantity != null && !quantity.isEmpty()) {
            message.append(ChatColor.DARK_AQUA).append(quantity).append(" ");
            hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Quantity: ").append(ChatColor.WHITE).append(quantity);
        }

        System.out.println("buildComponent 4 " + message + ":" + hoverMessage);

        String target = entry.data.getString(DataKeys.TARGET).orElse("Unknown");
        if (!target.isEmpty()) {
            message.append(ChatColor.DARK_AQUA).append(target).append(" ");
            hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Target: ").append(ChatColor.WHITE).append(target);
        }

        System.out.println("buildComponent 5 " + message + ":" + hoverMessage);

        if (entry instanceof DataAggregateEntry) {
            entry.data.getInt(DataKeys.COUNT).ifPresent(count -> {
                message.append(ChatColor.GREEN).append("x").append(count).append(" ");
                hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Count: ").append(ChatColor.WHITE).append(count);
            });
        }

        System.out.println("buildComponent 6 " + message + ":" + hoverMessage);

        ComponentBuilder resultBuilder = new ComponentBuilder("");

        if (entry instanceof DataEntryComplete) {
            DataEntryComplete complete = (DataEntryComplete) entry;

            message.append(ChatColor.WHITE).append(complete.getRelativeTime());
            hoverMessage.append("\n").append(ChatColor.DARK_GRAY).append("Time: ").append(ChatColor.WHITE).append(complete.getTime());

            TextComponent main = new TextComponent();
            main.addExtra(message.toString());

            ComponentBuilder holdingBuilder = new ComponentBuilder("");
            ComponentBuilder hoverMessageBuilder = new ComponentBuilder(hoverMessage.toString());

            complete.data.get(DataKeys.LOCATION).ifPresent(oLoc -> {
                DataWrapper wrapper = (DataWrapper) oLoc;
                DataHelper.getLocationFromDataWrapper(wrapper).ifPresent(location -> {
                    if (this.session.hasFlag(Flag.EXTENDED)) {
                        holdingBuilder.append("\n").append(" - ").color(ChatColor.GRAY).append(DataHelper.buildLocation(location, true)).color(ChatColor.GRAY);
                    }

                    hoverMessageBuilder.append("\n").append("Location: ").color(ChatColor.DARK_GRAY).append(DataHelper.buildLocation(location, false)).color(ChatColor.GRAY);
                });
            });

            main.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessageBuilder.create()));
            resultBuilder.append(main).append(holdingBuilder.create());
        } else {
            TextComponent main = new TextComponent();
            main.addExtra(message.toString());
            main.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage.toString()).create()));
            resultBuilder.append(main);
        }

        System.out.println("buildComponent 7 " + message + ":" + hoverMessage + " (" + Arrays.toString(resultBuilder.create()) + ")");

        return resultBuilder.create();
    }
}
