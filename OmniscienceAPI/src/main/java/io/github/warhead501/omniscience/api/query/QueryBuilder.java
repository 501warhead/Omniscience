package io.github.warhead501.omniscience.api.query;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.warhead501.omniscience.api.OmniApi;
import io.github.warhead501.omniscience.api.flag.FlagHandler;
import io.github.warhead501.omniscience.api.parameter.ParameterException;
import io.github.warhead501.omniscience.api.parameter.ParameterHandler;
import io.github.warhead501.omniscience.api.util.Formatter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class QueryBuilder {
    private static final Pattern flagPattern = Pattern.compile("(-)([^\\s]+)?");

    private QueryBuilder() {

    }

    public static Query empty() {
        return new Query();
    }

    public static CompletableFuture<Query> fromArguments(QuerySession session, String arguments) throws ParameterException {
        return fromArguments(session, (arguments != null ? arguments.split(" ") : new String[]{}));
    }

    public static CompletableFuture<Query> fromArguments(QuerySession session, String[] arguments) throws ParameterException {
        checkNotNull(session);

        Query query = new Query();
        CompletableFuture<Query> future = new CompletableFuture<>();

        Map<String, String> definedParameters = Maps.newHashMap();

        if (arguments != null && arguments.length > 0) {
            List<CompletableFuture<?>> futures = Lists.newArrayList();
            for (String arg : arguments) {
                Optional<CompletableFuture<?>> listenable;

                if (flagPattern.matcher(arg).matches()) {
                    listenable = parseFlagFromArgument(session, query, arg);
                } else {
                    Pair<String, String> pair = getParameterKeyValue(arg);

                    listenable = parseParameterFromArgument(session, query, pair, ImmutableMap.copyOf(definedParameters));

                    definedParameters.put(pair.getKey(), pair.getValue());
                }

                listenable.ifPresent(futures::add);
            }

            if (!futures.isEmpty()) {
                CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
                combinedFuture.thenAccept(q -> future.complete(query));
            } else {
                future.complete(query);
            }
        } else {
            future.complete(query);
        }

        if (OmniApi.areDefaultsEnabled()) {
            StringBuilder usedDefaults = new StringBuilder();
            for (ParameterHandler handler : OmniApi.getParameters()) {
                if (session.isIgnoredDefault(handler)) { //Skip over any parameter that has been explicitly ignored.
                    continue;
                }
                boolean aliasFound = false;

                for (String alias : handler.getAliases()) {
                    if (definedParameters.containsKey(alias)) {
                        aliasFound = true;
                        break;
                    }
                }

                if (!aliasFound) {
                    handler.processDefault(session, query)
                            .ifPresent(
                                    stringStringPair -> usedDefaults.append(stringStringPair.getKey()).append(":").append(stringStringPair.getValue()).append(" ")
                            );
                }
            }

            if (usedDefaults.length() > 0) {
                session.getSender().sendMessage(Formatter.subHeader(String.format("Defaults used: %s", usedDefaults.toString())));
            }
        }

        return future;
    }

    private static Optional<CompletableFuture<?>> parseFlagFromArgument(QuerySession session, Query query, String flag) throws ParameterException {
        flag = flag.substring(1);

        String value = null;
        if (flag.contains("=")) {
            String[] split = flag.split("=");
            flag = split[0];
            if (split.length == 2 && !split[1].trim().isEmpty()) {
                value = split[1];
            }
        }
        final String fflag = flag;

        FlagHandler flagHandler = OmniApi.getFlagHandler(flag)
                .orElseThrow(() -> new ParameterException(String.format("'%s' is not a valid flag. No handler was found.", fflag)));

        if (!flagHandler.acceptsSource(session.getSender())) {
            throw new ParameterException(String.format("'%s' cannot be used by this command source.", flag));
        }

        if (value != null && !flagHandler.acceptsValue(value)) {
            throw new ParameterException(String.format("Invalid value '%s' for parameter '%s'.", value, flag));
        }

        return flagHandler.process(session, flag, value, query);
    }

    private static Pair<String, String> getParameterKeyValue(String parameter) {
        String alias;
        String value;
        if (parameter.contains(":")) {
            String[] split = parameter.split(":", 2);
            alias = split[0];
            value = split[1];
        } else {
            alias = "p";
            value = parameter;
        }

        return Pair.of(alias, value);
    }

    private static Optional<CompletableFuture<?>> parseParameterFromArgument(QuerySession session,
                                                                             Query query,
                                                                             Pair<String, String> parameter,
                                                                             Map<String, String> definedParameters) throws ParameterException {
        if (parameter.getKey().length() <= 0 || parameter.getValue().length() <= 0) {
            throw new ParameterException(String.format("Invalid empty value for parameter '%s'", parameter.getKey()));
        }

        ParameterHandler handler = OmniApi.getParameterHandler(parameter.getKey())
                .orElseThrow(() -> new ParameterException(String.format("'%s' is not a valid parameter. No handler was found.", parameter.getKey())));

        if (!handler.canRun(session.getSender())) {
            throw new ParameterException(String.format("'%s' cannot be run as the current command source", parameter.getKey()));
        }

        if (!handler.acceptsValue(parameter.getValue())) {
            throw new ParameterException(String.format("Invalid value '%s' for parameter '%s'", parameter.getValue(), parameter.getKey()));
        }

        Optional<ParameterException> oThrow = definedParameters.entrySet().stream()
                .map(ent -> Pair.of(ent.getKey(), ent.getValue()))
                .filter(pair -> handler.doesConflict(parameter, pair))
                .map(pair -> new ParameterException(String.format("Parameter '%s:%s' conflicts with other parameter: '%s:%s'", parameter.getKey(), parameter.getValue(), pair.getKey(), pair.getValue())))
                .findFirst();

        if (oThrow.isPresent()) {
            throw oThrow.get();
        }

        return handler.buildForQuery(session, parameter.getKey(), parameter.getValue(), query);
    }
}
