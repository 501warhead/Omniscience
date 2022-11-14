package io.github.warhead501.omniscience.api.flag;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.query.Query;
import io.github.warhead501.omniscience.api.query.QuerySession;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FlagOrder extends BaseFlagHandler {

    private final ImmutableList<String> argumentOptions;

    public FlagOrder() {
        super(ImmutableList.of("ord"));
        this.argumentOptions = ImmutableList.of("new", "newest", "desc", "old", "oldest", "asc");
    }

    @Override
    public boolean acceptsSource(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return argumentOptions.contains(value);
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public Optional<List<String>> suggestCompletionOptions(String partial) {
        if (partial == null || partial.isEmpty()) {
            return Optional.of(new ArrayList<>(argumentOptions));
        }
        return Optional.of(argumentOptions
                .stream()
                .filter(opt -> opt.toLowerCase().startsWith(partial.toLowerCase()))
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<CompletableFuture<?>> process(QuerySession session, String flag, String value, Query query) {
        if (value != null) {
            switch (value) {
                case "new":
                case "newest":
                case "desc":
                    session.setSortOrder(QuerySession.Sort.NEWEST_FIRST);
                    break;
                case "old":
                case "oldest":
                case "asc":
                default:
                    session.setSortOrder(QuerySession.Sort.OLDEST_FIRST);
            }
        } else {
            session.setSortOrder(QuerySession.Sort.OLDEST_FIRST);
        }
        return Optional.empty();
    }
}
