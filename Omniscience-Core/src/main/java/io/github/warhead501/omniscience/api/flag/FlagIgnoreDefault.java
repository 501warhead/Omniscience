package io.github.warhead501.omniscience.api.flag;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.warhead501.omniscience.api.query.Query;
import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.Omniscience;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FlagIgnoreDefault extends BaseFlagHandler {

    public FlagIgnoreDefault() {
        super(ImmutableList.of("nod"));
    }

    @Override
    public boolean acceptsSource(CommandSender sender) {
        return true;
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        String[] split = value.contains(",") ? value.split(",") : new String[]{value};
        for (String param : split) {
            if (!Omniscience.getParameterHandler(param).isPresent()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<List<String>> suggestCompletionOptions(String partial) {
        if (partial == null || partial.isEmpty()) {
            return Optional.of(Omniscience.getParameters().stream().flatMap(pm -> pm.getAliases().stream()).collect(Collectors.toList()));
        }
        String[] split = partial.split(",");
        String target = split[split.length - 1];
        List<String> suggestions = Lists.newArrayList();
        Omniscience.getParameters().stream()
                .flatMap(pm -> pm.getAliases().stream())
                .filter(alias -> alias.toLowerCase().startsWith(target.toLowerCase()))
                .forEach(alias -> {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < split.length - 1; i++) {
                        builder.append(split[i]).append(",");
                    }
                    suggestions.add(builder.append(alias).toString());
                });
        return Optional.of(suggestions);
    }

    @Override
    public Optional<CompletableFuture<?>> process(QuerySession session, String flag, String value, Query query) {
        String[] split = value.contains(",") ? value.split(",") : new String[]{value};
        for (String param : split) {
            Omniscience.getParameterHandler(param)
                    .ifPresent(session::addIgnoredDefault);
        }
        return Optional.empty();
    }
}
