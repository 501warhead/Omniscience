package net.lordofthecraft.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import net.lordofthecraft.omniscience.api.query.Query;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ParameterHandler {

    boolean canRun(CommandSender sender);

    boolean acceptsValue(String value);

    boolean canHandle(String cmd);

    ImmutableList<String> getAliases();

    Optional<CompletableFuture<?>> buildForQuery(QuerySession session, String parameter, String value, Query query);

    default Optional<Pair<String, String>> processDefault(QuerySession session, Query query) {
        return Optional.empty();
    }

    default Optional<List<String>> suggestTabCompletion(String partial) {
        return Optional.empty();
    }
}
