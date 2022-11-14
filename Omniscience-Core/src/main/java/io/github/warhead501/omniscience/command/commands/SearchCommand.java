package io.github.warhead501.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.warhead501.omniscience.api.interfaces.IOmniscience;
import io.github.warhead501.omniscience.api.parameter.ParameterException;
import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.command.async.SearchCallback;
import io.github.warhead501.omniscience.command.result.CommandResult;
import io.github.warhead501.omniscience.command.result.UseResult;
import io.github.warhead501.omniscience.command.util.Async;
import io.github.warhead501.omniscience.command.util.SearchParameterHelper;
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

        sender.sendMessage(DARK_AQUA + "Querying records...");

        try {
            CompletableFuture<Void> future = session.newQueryFromArguments(args);
            future.thenAccept(ignored -> Async.lookup(session, new SearchCallback(session)));
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

}
