package io.github.warhead501.omniscience.command;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.warhead501.omniscience.api.interfaces.IOmniscience;
import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.api.util.Formatter;
import io.github.warhead501.omniscience.command.commands.*;
import me.lucko.commodore.Commodore;
import io.github.warhead501.omniscience.command.result.CommandResult;
import io.github.warhead501.omniscience.command.result.UseResult;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OmniscienceCommand implements CommandExecutor {

    final static ImmutableSet<OmniSubCommand> subCommandSet;

    static {
        subCommandSet = ImmutableSet.of(
                new PageCommand(),
                new SearchCommand(),
                new ApplierCommand(QuerySession.Sort.NEWEST_FIRST), //Rollback
                new ApplierCommand(QuerySession.Sort.OLDEST_FIRST), //Restore
                new UndoCommand(),
                new ToolCommand(),
                new EventsCommand()
        );
    }

    private final IOmniscience omniscience;

    public OmniscienceCommand(IOmniscience omniscience) {
        this.omniscience = omniscience;
    }

    public static void registerCompletions(Commodore commodore, PluginCommand command) {
        LiteralArgumentBuilder<Object> builder = LiteralArgumentBuilder.literal("omniscience");
        subCommandSet.forEach(cmd -> {
            LiteralArgumentBuilder<Object> subBuilder = LiteralArgumentBuilder.literal(cmd.getCommand());
            cmd.buildLiteralArgumentBuilder(subBuilder);
            builder.then(subBuilder);
        });
        commodore.register(command, builder);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length < 1 || isHelpArg(args[0])) {
            return sendHelp(commandSender, label);
        }
        Optional<OmniSubCommand> cOptional = subCommandSet.stream()
                .filter(cmd -> cmd.isCommand(args[0].toLowerCase()))
                .findFirst();
        if (cOptional.isPresent()) {
            OmniSubCommand subCommand = cOptional.get();
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
            UseResult result = subCommand.canRun(commandSender);
            if (result == UseResult.SUCCESS) {
                CommandResult cmdResult = subCommand.run(commandSender, omniscience, subArgs);
                if (!cmdResult.wasSuccessful()) {
                    commandSender.sendMessage(Formatter.error(cmdResult.getReason()));
                }
                return true;
            } else {
                return sendError(commandSender, result);
            }
        } else {
            commandSender.sendMessage(Formatter.error("The command " + args[0] + " was not found."));
            return sendHelp(commandSender, label);
        }
    }

    private boolean isHelpArg(String arg) {
        return arg.equalsIgnoreCase("help") || arg.equalsIgnoreCase("h") || arg.equalsIgnoreCase("?");
    }

    private boolean sendHelp(CommandSender sender, String label) {
        List<OmniSubCommand> runnableSubCommands = subCommandSet.stream()
                .filter(cmd -> cmd.canRun(sender) == UseResult.SUCCESS)
                .collect(Collectors.toList());
        sender.sendMessage(Formatter.formatPrimaryMessage(" -======= Omniscience =======-"));
        sender.sendMessage(Formatter.subHeader("For Powerful Searching"));
        runnableSubCommands.forEach(cmd ->
                sender.sendMessage(Formatter.formatPrimaryMessage("/" + label)
                        + " " + Formatter.formatSecondaryMessage(cmd.getCommand())
                        + " " + Formatter.formatSecondaryMessage(cmd.getUsage())
                        + Formatter.formatPrimaryMessage(": ") + ChatColor.GRAY + cmd.getDescription()));
        return true;
    }

    private boolean sendError(CommandSender sender, UseResult result) {
        switch (result) {
            case NO_COMMAND_SENDER:
                sender.sendMessage(Formatter.error("This command cannot be run by non-players"));
                break;
            case NO_PLAYER_SENDER:
                sender.sendMessage(Formatter.error("This command cannot be run by players"));
                break;
            case NO_PERMISSION:
                sender.sendMessage(Formatter.error("You do not have permission to run this command"));
                break;
            case OTHER_ERROR:
                sender.sendMessage(Formatter.error("Something went wrong during command execution."));
                break;
        }
        return true;
    }

    private String colorAndReset(ChatColor color, String string) {
        return color + string + ChatColor.RESET;
    }
}
