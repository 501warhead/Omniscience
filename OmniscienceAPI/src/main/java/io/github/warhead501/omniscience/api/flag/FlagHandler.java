package io.github.warhead501.omniscience.api.flag;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.query.Query;
import io.github.warhead501.omniscience.api.query.QuerySession;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface FlagHandler {

    boolean acceptsSource(CommandSender sender);

    boolean acceptsValue(String value);

    boolean handles(String flag);

    ImmutableList<String> getAliases();

    default boolean requiresArguments() {
        return false;
    }

    default Optional<List<String>> suggestCompletionOptions(String partial) {
        return Optional.empty();
    }

    Optional<CompletableFuture<?>> process(QuerySession session, String flag, String value, Query query);
}
