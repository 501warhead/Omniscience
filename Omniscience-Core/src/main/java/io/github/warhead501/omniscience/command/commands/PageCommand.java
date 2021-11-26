package io.github.warhead501.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.warhead501.omniscience.api.interfaces.IOmniscience;
import io.github.warhead501.omniscience.api.util.Formatter;
import io.github.warhead501.omniscience.command.result.CommandResult;
import io.github.warhead501.omniscience.command.result.UseResult;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class PageCommand extends SimpleCommand {

    private static final Map<CommandSender, List<BaseComponent[]>> searchResults = Maps.newConcurrentMap();

    public PageCommand() {
        super(ImmutableList.of("p", "pg"));
    }

    public static void setSearchResults(CommandSender sender, List<BaseComponent[]> results) {
        searchResults.put(sender, results);
        if (!results.isEmpty()) {
            showPage(sender, 0);
        }
    }

    public static void removeSearchResults(CommandSender sender) {
        searchResults.remove(sender);
    }

    private static CommandResult showPage(CommandSender sender, int pageNum) {
        if (!searchResults.containsKey(sender)) {
            return CommandResult.failure("You do not have any search results. Please run a search with /omni search!");
        }
        List<BaseComponent[]> results = searchResults.get(sender);
        if (results.size() < pageNum * 15) {
            return CommandResult.failure("Error: " + (pageNum + 1) + " is not a valid page.");
        }
        sender.sendMessage(Formatter.getPageHeader((pageNum + 1), (int) Math.round(Math.ceil(results.size() / 15D))));
        for (int i = pageNum * 15; i < (pageNum * 15) + 14; i++) {
            if (i >= results.size()) {
                break;
            }
            BaseComponent[] component = results.get(i);
            if (sender instanceof Player) {
                ((Player) sender).spigot().sendMessage(component);
            } else {
                String message = new TextComponent(component).toPlainText();
                sender.sendMessage(message);
            }
        }
        return CommandResult.success();
    }

    @Override
    public UseResult canRun(CommandSender sender) {
        return hasPermission(sender, "omniscience.commands.page");
    }

    @Override
    public String getCommand() {
        return "page";
    }

    @Override
    public String getUsage() {
        return GREEN + "<Page #>";
    }

    @Override
    public String getDescription() {
        return "Moves you onto the page specified of your results, if available.";
    }

    @Override
    public void buildLiteralArgumentBuilder(LiteralArgumentBuilder<Object> builder) {
        builder.then(RequiredArgumentBuilder.argument("page-number", IntegerArgumentType.integer(1)));
    }

    @Override
    public CommandResult run(CommandSender sender, IOmniscience core, String[] args) {
        if (!NumberUtils.isDigits(args[0])) {
            return CommandResult.failure("Please specify a page number.");
        }
        int pageNum = Integer.valueOf(args[0]) - 1;
        return showPage(sender, pageNum);
    }

    @Override
    public List<String> getCommandSuggestions(String partial) {
        return Lists.newArrayList();
    }
}
