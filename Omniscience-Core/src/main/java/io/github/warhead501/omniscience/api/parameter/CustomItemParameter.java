package io.github.warhead501.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.warhead501.omniscience.api.data.DataKey;
import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.query.FieldCondition;
import io.github.warhead501.omniscience.api.query.MatchRule;
import io.github.warhead501.omniscience.api.query.Query;
import io.github.warhead501.omniscience.api.query.QuerySession;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CustomItemParameter extends BaseParameterHandler {

    public CustomItemParameter() {
        super(ImmutableList.of("cu"));
    }

    @Override
    public boolean canRun(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return value.equalsIgnoreCase("y") || value.equalsIgnoreCase("n") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("yes");
    }

    @Override
    public Optional<CompletableFuture<?>> buildForQuery(QuerySession session, String parameter, String value, Query query) {
        if (value.toLowerCase().startsWith("y")) {
            query.addCondition(FieldCondition.of(DataKeys.ITEMSTACK.then(DataKey.of("meta")), MatchRule.EXISTS, true));
        } else if (value.toLowerCase().startsWith("n")) {
            query.addCondition(FieldCondition.of(DataKeys.ITEMSTACK.then(DataKey.of("meta")), MatchRule.EXISTS, false));
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        List<String> options = Lists.newArrayList("no", "yes");
        if (partial == null || partial.isEmpty()) {
            return Optional.of(options);
        }
        return Optional.of(options.stream().filter(opt -> opt.startsWith(partial.toLowerCase())).collect(Collectors.toList()));
    }
}
