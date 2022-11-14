package io.github.warhead501.omniscience.command.util;

import com.google.common.collect.Lists;
import io.github.warhead501.omniscience.api.parameter.ParameterHandler;
import io.github.warhead501.omniscience.Omniscience;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class SearchParameterHelper {

    public static List<String> suggestParameterCompletion(String partial) {
        List<String> results = Lists.newArrayList();
        if (partial != null && !partial.isEmpty() && (partial.startsWith("-") || partial.contains(":"))) {
            if (partial.startsWith("-")) {
                if (partial.contains("=")) { //They've completed a flag, and are looking for suggestions on how to finish it
                    String[] flagSplit = partial.substring(1).split("=");
                    Omniscience.getFlagHandler(flagSplit[0])
                            .ifPresent(
                                    flagHandler -> flagHandler.suggestCompletionOptions(flagSplit.length > 1 ? flagSplit[1] : null)
                                            .ifPresent(
                                                    completionResults -> completionResults.forEach(res -> results.add("-" + flagSplit[0] + "=" + res))
                                            )
                            );
                } else { //They're typing out a flag and are looking for options that match so far
                    Omniscience.getFlagHandlers()
                            .stream()
                            .flatMap(flagHandler -> {
                                boolean requiresArguments = flagHandler.requiresArguments();
                                return flagHandler.getAliases().stream()
                                        .map(flag -> "-" + flag + (requiresArguments ? "=" : ""));
                            })
                            .filter(flag -> flag.substring(1).startsWith(partial.substring(1).toLowerCase()))
                            .forEach(results::add);
                }

            } else if (partial.contains(":")) { //This means that they are typing a parameter and are ready to recieve some suggestions for how to fill it out
                String[] splitPartial = partial.split(":");
                if (splitPartial.length > 0) {
                    Optional<ParameterHandler> oHandler = Omniscience.getParameterHandler(splitPartial[0]);
                    oHandler.ifPresent(handler -> handler.suggestTabCompletion(splitPartial.length > 1 ? splitPartial[1] : null)
                            .ifPresent(completionResults -> {
                                completionResults.forEach(res -> results.add(splitPartial[0] + ":" + res));
                            }));
                }
            }
        } else {
            Stream<String> flags = Omniscience.getFlagHandlers()
                    .stream()
                    .flatMap(flagHandler -> {
                        boolean requiresArguments = flagHandler.requiresArguments();
                        return flagHandler.getAliases().stream()
                                .map(flag -> "-" + flag + (requiresArguments ? "=" : ""));
                    });
            Stream<String> params = Omniscience.getParameters()
                    .stream()
                    .flatMap(
                            parameterHandler -> parameterHandler.getAliases().stream().map(param -> param + ":")
                    );
            Stream<String> concat = Stream.concat(flags, params);
            if (partial != null && !partial.isEmpty()) {
                concat = concat.filter(arg -> arg.toLowerCase().startsWith(partial.toLowerCase()));
            }
            concat.forEach(results::add);
        }

        return results;
    }
}
