package net.lordofthecraft.omniscience.api.parameter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import net.lordofthecraft.omniscience.Omniscience;
import net.lordofthecraft.omniscience.api.query.Query;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import net.lordofthecraft.omniscience.api.query.SearchConditionGroup;

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