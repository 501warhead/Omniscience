package io.github.warhead501.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.warhead501.omniscience.api.entry.ActionResult;
import io.github.warhead501.omniscience.api.entry.Actionable;
import io.github.warhead501.omniscience.api.entry.ActionableException;
import io.github.warhead501.omniscience.api.entry.DataEntry;
import io.github.warhead501.omniscience.api.flag.Flag;
import io.github.warhead501.omniscience.api.interfaces.IOmniscience;
import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.api.util.Formatter;
import io.github.warhead501.omniscience.OmniConfig;
import io.github.warhead501.omniscience.Omniscience;
import io.github.warhead501.omniscience.command.result.CommandResult;
import io.github.warhead501.omniscience.command.result.UseResult;
import io.github.warhead501.omniscience.command.util.SearchParameterHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ApplierCommand extends SimpleCommand {

    private final QuerySession.Sort sort;

    public ApplierCommand(QuerySession.Sort sortOrder) {
        super(sortOrder == QuerySession.Sort.NEWEST_FIRST ? ImmutableList.of("rb", "roll") : ImmutableList.of("rs", "rst"));
        this.sort = sortOrder;
    }

    @Override
    public UseResult canRun(CommandSender sender) {
        return hasPermission(sender, "omniscience.commands.rollback");
    }

    @Override
    public String getCommand() {
        return sort == QuerySession.Sort.NEWEST_FIRST ? "rollback" : "restore";
    }

    @Override
    public String getUsage() {
        return GREEN + "<Lookup Params>";
    }

    @Override
    public String getDescription() {
        return (sort == QuerySession.Sort.NEWEST_FIRST ? "rollback" : "restore") + " a set of changes based on the Parameters Provided";
    }

    @Override
    public CommandResult run(CommandSender sender, IOmniscience core, String[] args) {
        if (args.length == 0) {
            return CommandResult.failure(RED + "Error: " + GRAY + "Please specify search arguments.");
        }
        final QuerySession session = new QuerySession(sender);
        session.addFlag(Flag.NO_GROUP);
        try {
            sender.sendMessage(ChatColor.DARK_AQUA + "Querying records...");
            CompletableFuture<Void> future = session.newQueryFromArguments(args);
            session.setSortOrder(sort);
            future.thenAccept(ignored -> {
                session.getQuery().setSearchLimit(OmniConfig.INSTANCE.getActionablesLimit());

                try {
                    List<ActionResult> actionResults = Lists.newArrayList();

                    CompletableFuture<List<DataEntry>> futureResults = Omniscience.getStorageHandler().records().query(session);

                    futureResults.thenAccept(results -> {
                        if (results.isEmpty()) {
                            sender.sendMessage(Formatter.error("No results."));
                        } else {
                            try {
                                for (DataEntry entry : results) {
                                    if (entry instanceof Actionable) {
                                        Actionable actionable = (Actionable) entry;
                                        try {
                                            if (sort.equals(QuerySession.Sort.NEWEST_FIRST)) {
                                                actionResults.add(actionable.rollback());
                                            } else {
                                                actionResults.add(actionable.restore());
                                            }
                                        } catch (ActionableException ae) {
                                            actionResults.add(ae.getResult());
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            if (sender instanceof Player) {
                                int changes = 0;

                                if (session.hasFlag(Flag.DRAIN)) {
                                    //TODO drain liquids around person
                                }

                                if (changes > 0) {
                                    sender.sendMessage(Formatter.bonus("Cleaning area..."));
                                }
                            }

                            int appliedCount = 0;
                            int skippedCount = 0;
                            for (ActionResult result : actionResults) {
                                if (result.applied()) {
                                    appliedCount++;
                                } else {
                                    skippedCount++;
                                }
                            }

                            Map<String, String> tokens = Maps.newHashMap();
                            tokens.put("appliedCount", "" + appliedCount);
                            tokens.put("skippedCount", "" + skippedCount);

                            final String messageTemplate;
                            if (skippedCount > 0) {
                                messageTemplate = String.format(" %s reversals. %s skipped", appliedCount, skippedCount);
                                for (ActionResult result : actionResults) {
                                    if (!result.applied()) {
                                        sender.sendMessage(Formatter.bonus("Skip Reason: " + result.getReason()));
                                    }
                                }
                            } else {
                                messageTemplate = String.format(" %s reversals", appliedCount);
                            }

                            sender.sendMessage(Formatter.success(messageTemplate));

                            if (sender instanceof Player) {
                                Omniscience.addLastActionResults(((Player) sender).getUniqueId(), actionResults);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(Formatter.error(e.getMessage()));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return CommandResult.failure(e.getMessage());
        }
        return CommandResult.success();
    }

    @Override
    public void buildLiteralArgumentBuilder(LiteralArgumentBuilder<Object> builder) {
        builder.then(RequiredArgumentBuilder.argument("search-parameters", StringArgumentType.greedyString()));
    }

    @Override
    public List<String> getCommandSuggestions(String partial) {
        return SearchParameterHelper.suggestParameterCompletion(partial);
    }
}
