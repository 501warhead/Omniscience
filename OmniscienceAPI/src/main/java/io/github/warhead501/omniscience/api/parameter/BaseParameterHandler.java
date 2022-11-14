package io.github.warhead501.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.warhead501.omniscience.api.query.MatchRule;
import io.github.warhead501.omniscience.api.data.DataKey;
import io.github.warhead501.omniscience.api.query.FieldCondition;
import io.github.warhead501.omniscience.api.query.Query;
import io.github.warhead501.omniscience.api.util.DataHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class BaseParameterHandler implements ParameterHandler {

    protected final ImmutableList<String> aliases;

    public BaseParameterHandler(ImmutableList<String> aliases) {
        this.aliases = aliases;
    }

    @Override
    public boolean canHandle(String cmd) {
        return aliases.contains(cmd);
    }

    @Override
    public ImmutableList<String> getAliases() {
        return aliases;
    }

    protected List<String> generateDefaultsBasedOnPartial(List<String> strings, String partial) {
        if (partial == null || partial.isEmpty()) {
            return strings;
        }
        String[] values = partial.split(",");
        final String target;
        if (values.length < 1) {
            target = partial;
        } else {
            target = values[values.length - 1];
        }
        if (strings.contains(target)) {
            if (values.length > 1) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < values.length - 1; i++) {
                    builder.append(values[i]).append(",");
                }
                return Collections.singletonList(builder.append(target).toString());
            } else {
                return Collections.singletonList(target);
            }
        }
        return strings.stream()
                .filter(string -> target.startsWith("!") ? string.startsWith(target.substring(1)) : string.startsWith(target))
                .map(val -> {
                    StringBuilder builder = new StringBuilder();
                    if (values.length > 1) {
                        for (int i = 0; i < values.length - 1; i++) {
                            builder.append(values[i]).append(",");
                        }
                    }
                    builder.append(val.toLowerCase());
                    return builder.toString();
                }).collect(Collectors.toList());
    }

    protected void convertStringToIncludes(DataKey key, String value, Query query) {
        List<Pattern> in = Lists.newArrayList();
        List<Pattern> nin = Lists.newArrayList();
        for (String string : value.split(",")) {
            if (string.startsWith("!")) {
                nin.add(DataHelper.compileUserInput(string.substring(1)));
            } else {
                in.add(DataHelper.compileUserInput(string));
            }
        }
        if (!in.isEmpty()) {
            query.addCondition(FieldCondition.of(key, MatchRule.INCLUDES, in));
        }
        if (!nin.isEmpty()) {
            query.addCondition(FieldCondition.of(key, MatchRule.EXCLUDES, nin));
        }
    }

    protected Pattern compileMessageSearch(String[] messages) {
        StringBuilder exclusionBuilder = new StringBuilder();
        StringBuilder searchBuilder = new StringBuilder();
        searchBuilder.append("^.*(");
        exclusionBuilder.append("(?!.*?(");
        boolean excluded = false;
        boolean firstSearch = false;
        for (String string : messages) {
            //Prevent regex from being fucked by user input.
            String lString = string.replaceAll("[-.\\+*?\\[^\\]$(){}=!<>|:\\\\]", "\\\\$0");
            if (string.startsWith("!")) {
                if (!excluded) {
                    exclusionBuilder.append(lString.substring(2).replaceAll("\\*", ".*"));
                    excluded = true;
                } else {
                    exclusionBuilder.append("|").append(lString.substring(2).replaceAll("\\*", ".*"));
                }
            } else {
                if (!firstSearch) {
                    searchBuilder.append(lString);
                    firstSearch = true;
                } else {
                    searchBuilder.append("|").append(lString);
                }
            }
        }
        exclusionBuilder.append("))");
        searchBuilder.append(")+.*$");
        if (excluded) {
            return Pattern.compile("/" + exclusionBuilder.toString() + searchBuilder.toString() + "/i");
        } else {
            return Pattern.compile(searchBuilder.toString());
        }
    }

    protected List<String> getInputAsList(String value) {
        List<String> list = Lists.newArrayList();
        value = value.replace("!", "");
        if (value.contains(",")) {
            String[] split = value.split(",");
            list.addAll(Arrays.asList(split));
        } else {
            list.add(value);
        }
        return list;
    }
}
