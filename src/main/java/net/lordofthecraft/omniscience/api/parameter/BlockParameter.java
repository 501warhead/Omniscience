package net.lordofthecraft.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.query.FieldCondition;
import net.lordofthecraft.omniscience.api.query.MatchRule;
import net.lordofthecraft.omniscience.api.query.Query;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockParameter extends BaseParameterHandler {
    private final Pattern pattern = Pattern.compile("[\\w,:-]+");

    public BlockParameter() {
        super(ImmutableList.of("b", "block"));
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
        query.addCondition(FieldCondition.of(DataKeys.TARGET, MatchRule.EQUALS, Pattern.compile(value.toUpperCase())));

        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        Stream<Material> materialList = Lists.newArrayList(Material.values())
                .stream().filter(Material::isBlock);
        if (partial != null && !partial.isEmpty()) {
            materialList = materialList.filter(material -> material.name().toLowerCase().contains(partial.toLowerCase()));
        }
        return Optional.of(materialList.map(mat -> mat.name().toLowerCase()).collect(Collectors.toList()));
    }
}