package io.github.warhead501.omniscience.api;

import io.github.warhead501.omniscience.api.entry.DataEntry;
import io.github.warhead501.omniscience.api.flag.FlagHandler;
import io.github.warhead501.omniscience.api.interfaces.IOmniscience;
import io.github.warhead501.omniscience.api.interfaces.WorldEditHandler;
import io.github.warhead501.omniscience.api.parameter.ParameterHandler;
import io.github.warhead501.omniscience.api.util.PastTenseWithEnabled;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class OmniApi {

    private static IOmniscience omniscience;

    public static void setCore(IOmniscience omni) throws IllegalAccessException {
        if (omniscience != null) {
            throw new IllegalAccessException("Omniscience's instance cannot be replaced.");
        }
        omniscience = omni;
    }

    public static IOmniscience getOmniscience() {
        return omniscience;
    }

    public static void info(String info) {
        omniscience.info(info);
    }

    public static void warning(String warning) {
        omniscience.warning(warning);
    }

    public static void severe(String error) {
        omniscience.severe(error);
    }

    public static void log(Level level, String msg, Throwable ex) {
        omniscience.log(level, msg, ex);
    }

    public static boolean areDefaultsEnabled() {
        return omniscience.areDefaultsEnabled();
    }

    public static List<ParameterHandler> getParameters() {
        return omniscience.getParameters();
    }

    public static Optional<FlagHandler> getFlagHandler(String flag) {
        return omniscience.getFlagHandler(flag);
    }

    public static Optional<ParameterHandler> getParameterHandler(String parameter) {
        return omniscience.getParameterHandler(parameter);
    }

    public static String getDefaultTime() {
        return omniscience.getDefaultTime();
    }

    public static int getDefaultRadius() {
        return omniscience.getDefaultRadius();
    }

    public static int getRadiusLimit() {
        return omniscience.getMaxRadius();
    }

    public static Map<String, PastTenseWithEnabled> getEvents() {
        return omniscience.getEvents();
    }

    public static List<String> getEnabledEvents() {
        return getEvents().entrySet().stream().filter((ent) -> ent.getValue().isEnabled()).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public static boolean isEventEnabled(String event) {
        return getEvents().containsKey(event) && getEvents().get(event).isEnabled();
    }

    public static boolean isEventRegistered(String event) {
        return getEvents().containsKey(event);
    }

    public static void registerEvent(String event, String pastTense) {
        omniscience.registerEvent(event, pastTense);
    }

    public static void registerParameterHandler(ParameterHandler handler) {
        omniscience.registerParameterHandler(handler);
    }

    public static void registerFlagHandler(FlagHandler handler) {
        omniscience.registerFlagHandler(handler);
    }

    public static String getEventPastTense(String event) {
        if (!getEvents().containsKey(event)) {
            return event;
        }
        return getEvents().get(event).getPastTense();
    }

    public static String getSimpleDateFormat() {
        return omniscience.getSimpleDateFormat();
    }

    public static void registerWorldEditHandler(WorldEditHandler handler) {
        omniscience.registerWorldEditHandler(handler);
    }

    public static Optional<Class<? extends DataEntry>> getEventClass(String event) {
        return omniscience.getEventClass(event);
    }

    public static String getDateFormat() {
        return omniscience.getDateFormat();
    }
}
