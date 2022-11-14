package io.github.warhead501.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.warhead501.omniscience.api.interfaces.IOmniscience;
import io.github.warhead501.omniscience.OmniConfig;
import io.github.warhead501.omniscience.Omniscience;
import io.github.warhead501.omniscience.api.util.OmniUtils;
import io.github.warhead501.omniscience.command.result.CommandResult;
import io.github.warhead501.omniscience.command.result.UseResult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ToolCommand extends SimpleCommand {

	public ToolCommand() {
		super(ImmutableList.of("t", "inspect"));
	}

	@Override
	public UseResult canRun(CommandSender sender) {
		return sender instanceof Player ? hasPermission(sender, "omniscience.commands.tool") : UseResult.NO_COMMAND_SENDER;
	}

	@Override
	public String getCommand() {
		return "tool";
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Turn on or off the Omniscience search tool";
	}

	@Override
	public CommandResult run(CommandSender sender, IOmniscience core, String[] args) {
		if (sender instanceof Player) {
			Player pl = (Player) sender;
			if (Omniscience.hasActiveWand(pl)) {
				int first = pl.getInventory().first(OmniConfig.INSTANCE.getWandItem());
				if (first == -1) {
					pl.getInventory().addItem(OmniConfig.INSTANCE.getWandItem());
					pl.sendMessage(GREEN + "Added the Omniscience data tool to your inventory. Happy Searching!");
				} else if (!OmniUtils.isOmniTool(pl.getInventory().getItemInMainHand())) {
					ItemStack tool = pl.getInventory().getItem(first).clone();
					pl.getInventory().remove(pl.getInventory().getItem(first));
					ItemStack main = pl.getInventory().getItemInMainHand().clone();
					pl.getInventory().setItemInMainHand(tool);
					pl.getInventory().setItem(first, main);
				} else {
					Omniscience.wandDeactivateFor(pl);
					pl.sendMessage(GREEN + "Successfully deactivated the Omniscience Data Tool");
				}
			} else {
				Omniscience.wandActivateFor(pl);
				if (!pl.getInventory().contains(OmniConfig.INSTANCE.getWandItem())) {
					pl.getInventory().addItem(OmniConfig.INSTANCE.getWandItem());
					pl.sendMessage(GREEN + "Added the Omniscience data tool to your inventory. Happy Searching.");
				} else {
					pl.sendMessage(GREEN + "Activated the Omniscience Data Tool " + GRAY + "(" + OmniConfig.INSTANCE.getWandItem().getType().name() + ")");
				}
			}
		}
		return CommandResult.success();
	}

	@Override
	public void buildLiteralArgumentBuilder(LiteralArgumentBuilder<Object> builder) {
		// NO:OP
	}

	@Override
	public List<String> getCommandSuggestions(String partial) {
		return null;
	}
}
