package io.github.warhead501.omniscience.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.warhead501.omniscience.api.interfaces.IOmniscience;
import io.github.warhead501.omniscience.command.result.CommandResult;
import io.github.warhead501.omniscience.command.result.UseResult;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface OmniSubCommand {

    String RED = ChatColor.RED.toString();
    String DARK_RED = ChatColor.DARK_RED.toString();
    String BLUE = ChatColor.BLUE.toString();
    String DARK_BLUE = ChatColor.DARK_BLUE.toString();
    String LIGHT_PURPLE = ChatColor.LIGHT_PURPLE.toString();
    String DARK_PURPLE = ChatColor.DARK_PURPLE.toString();
    String AQUA = ChatColor.AQUA.toString();
    String DARK_AQUA = ChatColor.DARK_AQUA.toString();
    String GREEN = ChatColor.GREEN.toString();
    String DARK_GREEN = ChatColor.DARK_GREEN.toString();
    String YELLOW = ChatColor.YELLOW.toString();
    String GOLD = ChatColor.GOLD.toString();
    String GRAY = ChatColor.GRAY.toString();
    String DARK_GRAY = ChatColor.DARK_GRAY.toString();
    String BLACK = ChatColor.BLACK.toString();
    String MAGIC = ChatColor.MAGIC.toString();
    String STRIKETHROUGH = ChatColor.STRIKETHROUGH.toString();
    String ITALIC = ChatColor.ITALIC.toString();
    String BOLD = ChatColor.BOLD.toString();
    String RESET = ChatColor.RESET.toString();

    UseResult canRun(CommandSender sender);

    String getCommand();

    String getUsage();

    String getDescription();

    boolean isCommand(String command);

    CommandResult run(CommandSender sender, IOmniscience core, String[] args);

    void buildLiteralArgumentBuilder(LiteralArgumentBuilder<Object> builder);

    List<String> getCommandSuggestions(String partial);

    default UseResult hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return UseResult.SUCCESS;
        } else {
            return UseResult.NO_PERMISSION;
        }
    }
}
