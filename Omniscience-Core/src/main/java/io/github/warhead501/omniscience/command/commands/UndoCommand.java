package io.github.warhead501.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.warhead501.omniscience.api.data.LocationTransaction;
import io.github.warhead501.omniscience.api.entry.ActionResult;
import io.github.warhead501.omniscience.api.interfaces.IOmniscience;
import io.github.warhead501.omniscience.api.util.Formatter;
import io.github.warhead501.omniscience.Omniscience;
import io.github.warhead501.omniscience.command.result.CommandResult;
import io.github.warhead501.omniscience.command.result.UseResult;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Optional;

public class UndoCommand extends SimpleCommand {

    public UndoCommand() {
        super(ImmutableList.of("u"));
    }

    @Override
    public UseResult canRun(CommandSender sender) {
        return (sender instanceof Player ? hasPermission(sender, "omniscience.commands.undo") : UseResult.NO_COMMAND_SENDER);
    }

    @Override
    public String getCommand() {
        return "undo";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Reverses the last changes you made";
    }

    @Override
    public CommandResult run(CommandSender sender, IOmniscience core, String[] args) {
        if (!(sender instanceof Player)) {
            return CommandResult.failure("You must be a player to use this command");
        }

        Optional<List<ActionResult>> oResults = Omniscience.getLastActionResults(((Player) sender).getUniqueId());
        if (!oResults.isPresent()) {
            return CommandResult.failure("You have no valid actions to undo");
        }

        List<ActionResult> results = Lists.reverse(oResults.get());

        int applied = 0;
        int skipped = 0;

        for (ActionResult result : results) {
            if (result.getTransaction() != null) {
                Object rawOriginal = result.getTransaction().getOriginalState();
                Object rawFinal = result.getTransaction().getFinalState();

                if (result.getTransaction() instanceof LocationTransaction) {
                    Location location = ((LocationTransaction) result.getTransaction()).getLocation();

                    if (rawOriginal instanceof Inventory) {
                        if (location.getBlock().getState() instanceof Container) {
                            ((Container) location.getBlock().getState()).getInventory().setContents(((Inventory) rawOriginal).getContents());
                            applied++;
                        } else {
                            skipped++;
                        }
                    }
                }

                if (rawOriginal instanceof BlockState) {
                    BlockData data = ((BlockState) rawOriginal).getBlockData();
                    ((BlockState) rawOriginal).getBlock().setBlockData(data);
                    if (((BlockState) rawOriginal).getBlockData().equals(data)) {
                        applied++;
                    } else {
                        skipped++;
                    }
                }

                if (rawFinal instanceof Entity) {
                    Entity entity = (Entity) rawFinal;
                    if (!entity.isDead() && entity.isValid()) {
                        entity.remove();
                        applied++;
                    } else {
                        skipped++;
                    }
                }
            } else {
                skipped++;
            }
        }

        final String messageTemplate;
        if (skipped > 0) {
            messageTemplate = String.format("%s reversals(). %s skipped", applied, skipped);
        } else {
            messageTemplate = String.format("%s reversals", applied);
        }

        sender.sendMessage(Formatter.success(messageTemplate));

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
