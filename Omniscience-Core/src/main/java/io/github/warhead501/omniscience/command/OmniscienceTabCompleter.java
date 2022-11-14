package io.github.warhead501.omniscience.command;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Optional;

public class OmniscienceTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 1) {
            Optional<OmniSubCommand> oCommand = OmniscienceCommand.subCommandSet.stream()
                    .filter(cmd -> cmd.isCommand(args[0]))
                    .findFirst();
            if (oCommand.isPresent()) {
                return oCommand.get().getCommandSuggestions(args[args.length - 1]);
            }
        }
        return Lists.newArrayList();
    }
}
