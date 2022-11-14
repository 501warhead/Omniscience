package io.github.warhead501.omniscience.command.util;

import io.github.warhead501.omniscience.api.util.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class OmniTeleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Formatter.error("This command can only be run by players."));
            return true;
        }
        // /omnitele world x y z
        if (args.length < 4) {
            sender.sendMessage(Formatter.error("Something went wrong trying to teleport you!"));
            return true;
        }
        UUID worldId = UUID.fromString(args[0]);
        World world = Bukkit.getWorld(worldId);
        if (world == null) {
            sender.sendMessage(Formatter.error("World not found"));
            return true;
        }
        try {
            Location newLocation = new Location(world, Double.valueOf(args[1]) + 0.5D, Double.valueOf(args[2]) + 0.5D, Double.valueOf(args[3]) + 0.5D);
            ((Player) sender).teleport(newLocation);
        } catch (NumberFormatException ex) {
            sender.sendMessage(Formatter.error("An unexpected error occurred while running this command. Are the coordinates right?"));
        }
        return true;
    }
}
