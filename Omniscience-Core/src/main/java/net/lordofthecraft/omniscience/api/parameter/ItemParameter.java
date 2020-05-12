package net.lordofthecraft.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.query.FieldCondition;
import net.lordofthecraft.omniscience.api.query.MatchRule;
import net.lordofthecraft.omniscience.api.query.Query;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import net.lordofthecraft.omniscience.api.util.DataHelper;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ItemParameter extends BaseParameterHandler {
    private final Pattern pattern = Pattern.compile("[\\w,:-\\\\*]+");

    public ItemParameter() {
        super(ImmutableList.of("i"));
    }

    @Override
    public boolean canRun(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return pattern.matcher(value).matches() && isItemValue(value);
    }

    @Override
    public Optional<CompletableFuture<?>> buildForQuery(QuerySession session, String parameter, String value, Query query) {
        if (value.contains(",")) {
            convertStringToIncludes(DataKeys.TARGET, value.toUpperCase(), query);
        } else {
            query.addCondition(FieldCondition.of(DataKeys.TARGET, MatchRule.EQUALS, DataHelper.compileUserInput(value.toUpperCase())));
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        return Optional.of(generateDefaultsBasedOnPartial(Lists.newArrayList(Material.values()).stream().filter(Material::isItem)
                .map(mat -> mat.name().toLowerCase())
                .collect(Collectors.toList()), partial != null ? partial.toLowerCase() : partial));
    }

    private boolean isItemValue(String value) {
        for (String s : getInputAsList(value.toUpperCase())) {
            if (Material.getMaterial(s) == null || !Material.getMaterial(s).isItem()) {
                return false;
            }
        }
        return true;
    }
}
