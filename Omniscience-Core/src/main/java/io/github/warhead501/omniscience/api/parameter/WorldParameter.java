package io.github.warhead501.omniscience.api.parameter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.warhead501.omniscience.api.query.Query;
import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.api.query.SearchConditionGroup;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import io.github.warhead501.omniscience.Omniscience;

public class WorldParameter  extends BaseParameterHandler {

    public WorldParameter() {
        super(ImmutableList.of("w"));
    }

    @Override
    public boolean canRun(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return Bukkit.getWorld(value) != null;
    }

    @Override
    public Optional<CompletableFuture<?>> buildForQuery(QuerySession session, String parameter, String value, Query query) {
        if (session.getSender() instanceof Player) {
            World world = Bukkit.getWorld(value);

            Omniscience.getParameterHandler("r").ifPresent(session::addIgnoredDefault);

            query.addCondition(SearchConditionGroup.from(world));
        }

        return Optional.empty();
    }
    
    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        return Optional.of(generateDefaultsBasedOnPartial(Bukkit.getWorlds().stream()
                .map(world -> world.getName().toLowerCase()).collect(Collectors.toList()), partial != null ? partial.toLowerCase() : partial));
    }
}