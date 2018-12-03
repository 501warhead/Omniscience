package net.lordofthecraft.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.lordofthecraft.omniscience.OmniConfig;
import net.lordofthecraft.omniscience.Omniscience;
import net.lordofthecraft.omniscience.api.entry.DataEntry;
import net.lordofthecraft.omniscience.api.parameter.ParameterException;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import net.lordofthecraft.omniscience.command.async.AsyncCallback;
import net.lordofthecraft.omniscience.command.async.SearchCallback;
import net.lordofthecraft.omniscience.command.result.CommandResult;
import net.lordofthecraft.omniscience.command.result.UseResult;
import net.lordofthecraft.omniscience.command.util.SearchParameterHelper;
import net.lordofthecraft.omniscience.interfaces.IOmniscience;
import net.lordofthecraft.omniscience.mongo.MongoConnectionHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchCommand extends SimpleCommand {

    public SearchCommand() {
        super(ImmutableList.of("s", "sc", "lookup", "l"));
    }

    @Override
    public UseResult canRun(CommandSender sender) {
        return hasPermission(sender, "omniscience.commands.search");
    }

    @Override
    public String getCommand() {
        return "search";
    }

    @Override
    public String getUsage() {
        return GREEN + "<Lookup Params>";
    }

    @Override
    public String getDescription() {
        return "Search Data Records based on the parameters provided.";
    }

    @Override
    public CommandResult run(CommandSender sender, IOmniscience core, String[] args) {
        final QuerySession session = new QuerySession(sender);

        sender.sendMessage(DARK_AQUA + "Querying records... (This can take a bit, please be patient)");

        try {
            CompletableFuture<Void> future = session.newQueryFromArguments(args);
            future.thenAccept(ignored -> lookup(session, new SearchCallback(session)));
        } catch (ParameterException e) {
            return CommandResult.failure(e.getMessage());
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "An unknown error occurred while running this command. Please check console." : ex.getMessage();
            ex.printStackTrace();
            return CommandResult.failure(message);
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

    private void lookup(final QuerySession session, AsyncCallback callback) {
        session.getQuery().setSearchLimit(OmniConfig.INSTANCE.getLookupSizeLimit());
        Bukkit.getScheduler().runTaskAsynchronously(Omniscience.getProvidingPlugin(Omniscience.class), () -> {
            try {
                CompletableFuture<List<DataEntry>> future = MongoConnectionHandler.getInstance().query(session);
                future.thenAccept(results -> {
                    try {
                        if (results.isEmpty()) {
                            callback.empty();
                        } else {
                            callback.success(results);
                        }
                    } catch (Exception e) {
                        session.getSender().sendMessage(ChatColor.RED + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                callback.error(e);
                e.printStackTrace();
            }
        });
    }
}
