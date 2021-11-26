package io.github.warhead501.omniscience.api.flag;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.query.Query;
import io.github.warhead501.omniscience.api.query.QuerySession;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FlagNoChat extends BaseFlagHandler {

    public FlagNoChat() {
        super(ImmutableList.of("nc"));
    }

    @Override
    public boolean acceptsSource(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return true;
    }

    @Override
    public Optional<CompletableFuture<?>> process(QuerySession session, String flag, String value, Query query) {
        session.addFlag(Flag.NO_CHAT);
        return Optional.empty();
    }
}
