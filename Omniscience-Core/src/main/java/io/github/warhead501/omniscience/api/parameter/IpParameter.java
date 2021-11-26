package io.github.warhead501.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.query.FieldCondition;
import io.github.warhead501.omniscience.api.query.MatchRule;
import io.github.warhead501.omniscience.api.query.Query;
import io.github.warhead501.omniscience.api.query.QuerySession;
import io.github.warhead501.omniscience.api.util.DataHelper;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class IpParameter extends BaseParameterHandler {
    private final Pattern pattern = Pattern.compile("[\\w.:,-\\\\*]+");

    public IpParameter() {
        super(ImmutableList.of("ip"));
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
            convertStringToIncludes(DataKeys.TARGET, value, query);
        } else {
            query.addCondition(FieldCondition.of(DataKeys.TARGET, MatchRule.EQUALS, DataHelper.compileUserInput(value)));
        }


        return Optional.empty();
    }
}