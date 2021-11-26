package io.github.warhead501.omniscience.api.query;

import com.google.common.collect.Range;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

import static io.github.warhead501.omniscience.api.data.DataKeys.*;

public final class SearchConditionGroup implements SearchCondition {
    private final List<SearchCondition> conditions = new ArrayList<>();
    private final Operator operator;

    public SearchConditionGroup(Operator operator) {
        this.operator = operator;
    }

    public static SearchConditionGroup from(Location location) {
        SearchConditionGroup group = new SearchConditionGroup(Operator.AND);

        group.add(FieldCondition.of(LOCATION.then(X), MatchRule.EQUALS, location.getBlockX()));
        group.add(FieldCondition.of(LOCATION.then(Y), MatchRule.EQUALS, location.getBlockY()));
        group.add(FieldCondition.of(LOCATION.then(Z), MatchRule.EQUALS, location.getBlockZ()));
        group.add(FieldCondition.of(LOCATION.then(WORLD), MatchRule.EQUALS, location.getWorld().getUID().toString()));

        return group;
    }
    
    public static SearchConditionGroup from(World world) {
        SearchConditionGroup group = new SearchConditionGroup(Operator.AND);

        group.add(FieldCondition.of(LOCATION.then(WORLD), MatchRule.EQUALS, world.getUID().toString()));

        return group;
    }

    public static SearchConditionGroup from(Location location, int radius) {
        SearchConditionGroup group = new SearchConditionGroup(Operator.AND);

        group.add(FieldCondition.of(LOCATION.then(WORLD), MatchRule.EQUALS, location.getWorld().getUID().toString()));

        Range<Integer> xRange = Range.open(location.getBlockX() - radius, location.getBlockX() + radius);
        group.add(FieldCondition.of(LOCATION.then(X), xRange));

        Range<Integer> yRange = Range.open(location.getBlockY() - radius, location.getBlockY() + radius);
        group.add(FieldCondition.of(LOCATION.then(Y), yRange));

        Range<Integer> zRange = Range.open(location.getBlockZ() - radius, location.getBlockZ() + radius);
        group.add(FieldCondition.of(LOCATION.then(Z), zRange));

        return group;
    }

    public void add(SearchCondition condition) {
        conditions.add(condition);
    }

    public void add(List<SearchCondition> conditions) {
        this.conditions.addAll(conditions);
    }

    public List<SearchCondition> getConditions() {
        return conditions;
    }

    public Operator getOperator() {
        return operator;
    }

    public enum Operator {
        AND,
        OR
    }
}
