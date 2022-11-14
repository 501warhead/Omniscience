package io.github.warhead501.omniscience.api.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.data.DataWrapper;
import io.github.warhead501.omniscience.api.OmniApi;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class DataHelper {

    public static boolean isPrimitiveType(Object object) {
        return (object instanceof Boolean ||
                object instanceof Byte ||
                object instanceof Character ||
                object instanceof Double ||
                object instanceof Float ||
                object instanceof Integer ||
                object instanceof Long ||
                object instanceof Short ||
                object instanceof String);
    }

    public static Optional<BlockData> getBlockDataFromWrapper(DataWrapper wrapper) {
        if (!wrapper.getKeys(false).contains(DataKeys.BLOCK_DATA)) {
            return Optional.empty();
        }
        return wrapper
                .get(DataKeys.BLOCK_DATA)
                .map(o -> Bukkit.getServer()
                        .createBlockData((String) o));
    }

    public static Optional<Location> getLocationFromDataWrapper(DataWrapper wrapper) {
        if (!wrapper.get(DataKeys.LOCATION).isPresent()) {
            return Optional.empty();
        }
        Optional<Integer> oX = wrapper.getInt(DataKeys.LOCATION.then(DataKeys.X));
        Optional<Integer> oY = wrapper.getInt(DataKeys.LOCATION.then(DataKeys.Y));
        Optional<Integer> oZ = wrapper.getInt(DataKeys.LOCATION.then(DataKeys.Z));
        Optional<String> oWorld = wrapper.getString(DataKeys.LOCATION.then(DataKeys.WORLD));
        if (oX.isPresent()
                && oY.isPresent()
                && oZ.isPresent()
                && oWorld.isPresent()) {
            Location location = new Location(Bukkit.getWorld(UUID.fromString(oWorld.get())), oX.get(), oY.get(), oZ.get());
            return Optional.of(location);
        }
        return Optional.empty();
    }

    public static <T extends ConfigurationSerializable> T unwrapConfigSerializable(DataWrapper wrapper) {
        Optional<String> oClassName = wrapper.getString(DataKeys.CONFIG_CLASS);
        if (!oClassName.isPresent()) {
            return null;
        }
        String fullClassName = oClassName.get();
        try {
            Class clazz = ConfigurationSerialization.getClassByAlias(fullClassName);
            DataWrapper localWrapper = wrapper.copy().remove(DataKeys.CONFIG_CLASS);
            Map<String, Object> configMap = Maps.newHashMap();
            localWrapper.getKeys(false)
                    .forEach(key -> localWrapper.get(key)
                            .ifPresent(val -> {
                                if (val instanceof DataWrapper) {
                                    configMap.put(key.toString(), unwrapConfigSerializable((DataWrapper) val));
                                } else if (val instanceof Collection) {
                                    configMap.put(key.toString(), unwrapAsNeeded((Collection) val));
                                } else if (val instanceof Map) {
                                    configMap.put(key.toString(), unwrapAsNeeded((Map<?, ?>) val));
                                } else {
                                    configMap.put(key.toString(), val);
                                }
                            }));
            ConfigurationSerializable config = ConfigurationSerialization.deserializeObject(configMap, clazz);
            return (T) config;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Collection<?> unwrapAsNeeded(Collection<?> collection) {
        ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();

        for (Object value : collection) {
            if (value instanceof DataWrapper) {
                ConfigurationSerializable config = unwrapConfigSerializable((DataWrapper) value);
                if (config != null) {
                    listBuilder.add(config);
                } else {
                    OmniApi.warning("Failed to deserialize the object: " + value);
                }
            } else if (value instanceof Collection) {
                listBuilder.add(unwrapAsNeeded((Collection) value));
            } else if (value instanceof Map) {
                listBuilder.add(unwrapAsNeeded((Map<?, ?>) value));
            } else {
                listBuilder.add(value);
            }
        }

        return listBuilder.build();
    }

    private static Map<?, ?> unwrapAsNeeded(Map<?, ?> map) {
        ImmutableMap.Builder<Object, Object> mapBuilder = ImmutableMap.builder();

        map.forEach((key, value) -> {
            if (value instanceof Map) {
                mapBuilder.put(key, unwrapAsNeeded((Map) value));
            } else if (value instanceof DataWrapper) {
                ConfigurationSerializable config = unwrapConfigSerializable((DataWrapper) value);
                if (config != null) {
                    mapBuilder.put(key, config);
                } else {
                    OmniApi.warning("Failed to deserialize the object: " + value);
                }
            } else if (value instanceof Collection) {
                mapBuilder.put(key, unwrapAsNeeded((Collection) value));
            } else {
                mapBuilder.put(key, value);
            }
        });

        return mapBuilder.build();
    }

    public static Map<Integer, ConfigurationSerializable> convertArrayToMap(ConfigurationSerializable[] configurationSerializables) {
        Map<Integer, ConfigurationSerializable> configurationSerializableMap = Maps.newHashMap();
        for (int i = 0; i < configurationSerializables.length; i++) {
            ConfigurationSerializable item = configurationSerializables[i];
            if (item != null) {
                configurationSerializableMap.put(i, item);
            }
        }
        return configurationSerializableMap;
    }

    public static BaseComponent[] buildLocation(Location location, boolean clickable) {
        ComponentBuilder builder = new ComponentBuilder("(x: " + location.getBlockX() + " y: " + location.getBlockY() + " z: " + location.getBlockZ()).color(ChatColor.GRAY);
        builder.append(" world: " + location.getWorld().getName()).color(ChatColor.GRAY);
        if (clickable) {
            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to teleport").color(ChatColor.GRAY).italic(true).create()));
            builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/omnitele " + location.getWorld().getUID().toString() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ()));
        }
        return builder.append(")").color(ChatColor.GRAY).create();
    }

    public static Pattern compileUserInput(String userInput) {
        String result = ("(" + (userInput.replaceAll("[-.\\+*?\\[^\\]$(){}=!<>|:\\\\]", "\\\\$0")) + ")").replaceAll("\\*", ".*");
        return Pattern.compile(result);
    }

    public static String writeLocationToString(Location location) {
        return "(X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ() + ")";
    }
}
