package net.lordofthecraft.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.query.FieldCondition;
import net.lordofthecraft.omniscience.api.query.MatchRule;
import net.lordofthecraft.omniscience.api.query.Query;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlayerParameter extends BaseParameterHandler {
    private final Pattern pattern = Pattern.compile("[\\w,:-]+");

    public PlayerParameter() {
        super(ImmutableList.of("p"));
    }

    @Override
    public boolean canRun(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return pattern.matcher(value).matches();
    }

    @Override
    public Optional<CompletableFuture<?>> buildForQuery(QuerySession session, String parameter, String value, Query query) {
        if (value.contains(",")) {
            String[] split = value.split(",");
            List<String> in = Lists.newArrayList();
            List<String> nin = Lists.newArrayList();
            for (String string : split) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(string.startsWith("!") ? string.substring(1) : string);
                if (player != null) {
                    if (string.startsWith("!")) {
                        nin.add(player.getUniqueId().toString());
                    } else {
                        in.add(player.getUniqueId().toString());
                    }
                }
            }
            if (!in.isEmpty()) {
                query.addCondition(FieldCondition.of(DataKeys.PLAYER_ID, MatchRule.INCLUDES, in));
            }
            if (!nin.isEmpty()) {
                query.addCondition(FieldCondition.of(DataKeys.PLAYER_ID, MatchRule.EXCLUDES, nin));

            }
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(value);

            if (player != null) {
                query.addCondition(FieldCondition.of(DataKeys.PLAYER_ID, MatchRule.EQUALS, player.getUniqueId().toString()));
            }
        }


        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        return Optional.of(generateDefaultsBasedOnPartial(Bukkit.getOnlinePlayers().stream()
                .map(pl -> pl.getName().toLowerCase()).collect(Collectors.toList()), partial != null ? partial.toLowerCase() : partial));
    }
}
