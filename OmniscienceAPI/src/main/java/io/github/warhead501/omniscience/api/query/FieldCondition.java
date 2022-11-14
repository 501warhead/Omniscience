package io.github.warhead501.omniscience.api.query;

import com.google.common.collect.Range;
import io.github.warhead501.omniscience.api.data.DataKey;

import static com.google.common.base.Preconditions.checkNotNull;

public class FieldCondition implements SearchCondition {
    private final DataKey field;
    private final MatchRule rule;
    private final Object value;

    public FieldCondition(DataKey field, MatchRule rule, Object value) {
        checkNotNull(field);
        checkNotNull(rule);
        checkNotNull(value);
        this.field = field;
        this.rule = rule;
        this.value = value;
    }

    public static FieldCondition of(DataKey field, MatchRule rule, Object value) {
        return new FieldCondition(field, rule, value);
    }

    public static FieldCondition of(DataKey field, Range<?> value) {
        return new FieldCondition(field, MatchRule.BETWEEN, value);
    }

    public DataKey getField() {
        return field;
    }

    public MatchRule getRule() {
        return rule;
    }

    public Object getValue() {
        return value;
    }
}
