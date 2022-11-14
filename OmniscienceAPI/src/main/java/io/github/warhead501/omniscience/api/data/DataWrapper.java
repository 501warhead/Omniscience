package io.github.warhead501.omniscience.api.data;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.github.warhead501.omniscience.api.util.DataHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.warhead501.omniscience.api.data.DataKey.of;

/**
 * A storage location for data. We do not care about what this data is. This method is oblivious to what it's storing or what it's storing data for. It's simply a holder.
 *
 * @author 501warhead
 */
public final class DataWrapper {

    private static final String[] listPairings = {"([{", ")]}"};
    private static final Pattern listPattern = Pattern.compile("^([\\(\\[\\{]?)(.+?)([\\)\\]\\}]?)$");

    private final Map<String, Object> data = Maps.newLinkedHashMap();
    private final DataKey key;
    private final DataWrapper parent;

    private DataWrapper(DataWrapper parent, DataKey key) {
        this.parent = parent;

        this.key = key;
    }

    private DataWrapper() {
        this.key = of();
        this.parent = this;
    }

    public static DataWrapper createNew() {
        return new DataWrapper();
    }

    public static DataWrapper ofBlock(BlockState block) {
        DataWrapper wrapper = new DataWrapper();
        wrapper.set(DataKeys.MATERIAL_TYPE, block.getType().name());
        //TODO We'll need a way to parse this. Return later when we know wtf this looks like.
        wrapper.set(DataKeys.BLOCK_DATA, block.getBlockData().getAsString());
        return wrapper;
    }

    private static Optional<Byte> asByte(Object obj) {
        if (obj == null) {
            // fail fast
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).byteValue());
        }

        try {
            return Optional.ofNullable(Byte.parseByte(sanitiseNumber(obj)));
        } catch (NumberFormatException | NullPointerException e) {
            // do nothing
        }
        return Optional.empty();
    }

    private static Optional<String> asString(Object obj) {
        if (obj instanceof String) {
            return Optional.of(sanitiseString((String) obj));
        } else if (obj == null) {
            return Optional.empty();
        } else {
            return Optional.of(sanitiseString(obj.toString()));
        }
    }

    private static <T extends ConfigurationSerializable> Optional<T> asSerializable(Object obj, Class<T> clazz) {
        if (!(obj instanceof DataWrapper)) {
            return Optional.empty();
        }
        ConfigurationSerializable config = DataHelper.unwrapConfigSerializable((DataWrapper) obj);
        if (!clazz.isInstance(config)) {
            return Optional.empty();
        }
        return Optional.of(clazz.cast(config));
    }

    private static String sanitiseNumber(Object obj) {
        String string = obj.toString().trim();
        if (string.length() < 1) {
            return "0";
        }

        Matcher candidate = listPattern.matcher(string);
        if (listBracketsMatch(candidate)) {
            string = candidate.group(2).trim();
        }

        int decimal = string.indexOf('.');
        int comma = string.indexOf(',', decimal);
        if (decimal > -1 && comma > -1) {
            return sanitiseNumber(string.substring(0, comma));
        }

        if (string.indexOf('-', 1) != -1) {
            return "0";
        }

        return string.replace(",", "").split(" ")[0];
    }

    private static String sanitiseString(String string) {
        return new String(string.trim().replaceAll("\u00A7", "&").getBytes(StandardCharsets.UTF_8));
    }

    private static boolean listBracketsMatch(Matcher candidate) {
        return candidate.matches() && listPairings[0].indexOf(candidate.group(1)) == listPairings[1].indexOf(candidate.group(3));
    }

    public DataKey getKey() {
        return key;
    }

    public Optional<DataWrapper> getParent() {
        return Optional.ofNullable(this.parent);
    }

    public String getName() {
        List<String> parts = this.key.getParts();
        return parts.isEmpty() ? "" : parts.get(parts.size() - 1);
    }

    public Optional<Object> get(DataKey key) {
        List<String> queryParts = key.getParts();

        int size = queryParts.size();

        if (size == 0) {
            return Optional.of(this);
        }

        String rootKey = queryParts.get(0);
        if (size == 1) {
            final Object object = this.data.get(rootKey);
            if (object == null) {
                return Optional.empty();
            }
            return Optional.of(object);
        }
        Optional<DataWrapper> oSubWrapper = this.getUnsafeWrapper(rootKey);
        if (!oSubWrapper.isPresent()) {
            return Optional.empty();
        }
        DataWrapper subWrapper = oSubWrapper.get();
        return subWrapper.get(key.popFirst());
    }

    private DataWrapper ofConfig(ConfigurationSerializable configurationSerializable) {
        DataWrapper wrapper = new DataWrapper();
        Map<String, Object> data = configurationSerializable.serialize();
        wrapper.set(DataKeys.CONFIG_CLASS, ConfigurationSerialization.getAlias(configurationSerializable.getClass()));
        data.forEach((key, value) -> {
            DataKey dataKey = DataKey.of(key);
            if (value instanceof ConfigurationSerializable) {
                wrapper.set(dataKey, ofConfig((ConfigurationSerializable) value));
            } else if (value instanceof Collection) {
                wrapper.set(dataKey, ensureSerialization((Collection) value));
            } else if (value instanceof Map) {
                wrapper.set(dataKey, ensureSerialization((Map<?, ?>) value));
            } else {
                wrapper.set(dataKey, value);
            }
        });
        return wrapper;
    }

    private void setMap(String key, Map<?, ?> value) {
        DataWrapper wrapper = createWrapper(of(key));
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            wrapper.set(of(entry.getKey().toString()), entry.getValue());
        }
    }

    private void copyDataWrapper(DataKey key, DataWrapper value) {
        Collection<DataKey> valueKeys = value.getKeys(true);
        for (DataKey oldKey : valueKeys) {
            set(key.then(oldKey), value.get(oldKey).get());
        }
    }

    public DataWrapper remove(DataKey key) {
        checkNotNull(key, "key");
        List<String> parts = key.getParts();
        if (parts.size() > 1) {
            String subKey = parts.get(0);
            DataKey subDataKey = of(subKey);
            Optional<DataWrapper> oWrapper = this.getUnsafeWrapper(subDataKey);
            if (!oWrapper.isPresent()) {
                return this;
            }
            DataWrapper subWrapper = oWrapper.get();
            subWrapper.remove(key.popFirst());
        } else {
            this.data.remove(parts.get(0));
        }
        return this;
    }

    public DataWrapper copy() {
        DataWrapper wrapper = new DataWrapper();
        getKeys(false)
                .forEach(key ->
                        get(key).ifPresent(
                                val -> wrapper.set(key, val)
                        )
                );
        return wrapper;
    }

    public DataWrapper createWrapper(DataKey path) {
        List<String> queryParts = path.getParts();

        int size = queryParts.size();

        checkArgument(size != 0, "The size of the key must be at least 1");

        String key = queryParts.get(0);
        DataKey keyQuery = of(key);

        if (size == 1) {
            DataWrapper result = new DataWrapper(this, keyQuery);
            this.data.put(key, result);
            return result;
        }
        DataKey subQuery = path.popFirst();
        DataWrapper subView = (DataWrapper) this.data.get(key);
        if (subView == null) {
            subView = new DataWrapper(this.parent, keyQuery);
            this.data.put(key, subView);
        }
        return subView.createWrapper(subQuery);
    }

    public Optional<DataWrapper> getWrapper(DataKey key) {
        return get(key).filter(obj -> obj instanceof DataWrapper).map(obj -> (DataWrapper) obj);
    }

    public Optional<? extends Map<?, ?>> getMap(DataKey key) {
        Optional<Object> val = get(key);
        if (val.isPresent()) {
            if (val.get() instanceof DataWrapper) {
                ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
                ((DataWrapper) val.get()).getValues(false).forEach(
                        (iKey, value) -> builder.put(iKey.asString('.'), ensureMappingOf(value))
                );
                return Optional.of(builder.build());
            } else if (val.get() instanceof Map) {
                return Optional.of((Map<?, ?>) ensureMappingOf(val.get()));
            }
        }
        return Optional.empty();
    }

    private Object ensureMappingOf(Object object) {
        if (object instanceof DataWrapper) {
            final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            ((DataWrapper) object).getValues(false).forEach((key, value) -> {
                builder.put(key.asString('.'), ensureMappingOf(value));
            });
            return builder.build();
        } else if (object instanceof Map) {
            final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            ((Map<?, ?>) object).forEach((key, value) -> {
                builder.put(key.toString(), ensureMappingOf(value));
            });
            return builder.build();
        } else if (object instanceof Collection) {
            final ImmutableList.Builder<Object> builder = ImmutableList.builder();
            for (Object entry : (Collection) object) {
                builder.add(ensureMappingOf(entry));
            }
            return builder.build();
        } else {
            return object;
        }
    }

    private Optional<DataWrapper> getUnsafeWrapper(DataKey key) {
        return get(key).filter(obj -> obj instanceof DataWrapper).map(obj -> (DataWrapper) obj);
    }

    private Optional<DataWrapper> getUnsafeWrapper(String key) {
        final Object object = this.data.get(key);
        if (!(object instanceof DataWrapper)) {
            return Optional.empty();
        }
        return Optional.of((DataWrapper) object);
    }

    public Set<DataKey> getKeys(boolean deep) {
        ImmutableSet.Builder<DataKey> builder = ImmutableSet.builder();

        for (Map.Entry<String, Object> entry : this.data.entrySet()) {
            builder.add(of(entry.getKey()));
        }
        if (deep) {
            for (Map.Entry<String, Object> entry : this.data.entrySet()) {
                if (entry.getValue() instanceof DataWrapper) {
                    for (DataKey key : ((DataWrapper) entry.getValue()).getKeys(true)) {
                        builder.add(of(entry.getKey()).then(key));
                    }
                }
            }
        }
        return builder.build();
    }

    public Map<DataKey, Object> getValues(boolean deep) {
        ImmutableMap.Builder<DataKey, Object> builder = ImmutableMap.builder();
        for (DataKey key : getKeys(deep)) {
            Object value = get(key).get();
            if (value instanceof DataWrapper) {
                builder.put(key, ((DataWrapper) value).getValues(deep));
            } else {
                builder.put(key, get(key).get());
            }
        }
        return builder.build();
    }

    public Optional<Boolean> getBoolean(DataKey key) {
        return get(key).map(obj -> (Boolean) obj);
    }

    public Optional<String> getString(DataKey key) {
        return get(key).map(obj -> (String) obj);
    }

    public Optional<Integer> getInt(DataKey key) {
        return get(key).map(obj -> (Integer) obj);
    }

    public <T extends ConfigurationSerializable> Optional<T> getConfigSerializable(DataKey key) {
        return getWrapper(key).map(DataHelper::unwrapConfigSerializable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataWrapper)) return false;
        DataWrapper wrapper = (DataWrapper) o;
        return Objects.equals(data.entrySet(), wrapper.data.entrySet()) &&
                Objects.equals(key, wrapper.key);
    }

    @Override
    public String toString() {
        final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        if (!this.key.toString().isEmpty()) {
            helper.add("key", this.key);
        }
        return helper.add("data", this.data).toString();
    }

    public DataWrapper set(DataKey path, Object value) {
        checkNotNull(path, "key is returning null");
        checkNotNull(value, "value is returning null");

        List<String> parts = path.getParts();
        String key = parts.get(0);
        if (parts.size() > 1) {
            DataKey subQuery = of(key);
            Optional<DataWrapper> oSubWrapper = this.getUnsafeWrapper(subQuery);
            DataWrapper subWrapper;
            if (!oSubWrapper.isPresent()) {
                this.createWrapper(subQuery);
                subWrapper = (DataWrapper) this.data.get(key);
            } else {
                subWrapper = oSubWrapper.get();
            }
            subWrapper.set(path.popFirst(), value);
            return this;
        }
        if (value instanceof DataWrapper) {
            checkArgument(value != this, "Cannot set a DataWrapper to itself");

            copyDataWrapper(path, (DataWrapper) value);
        } else if (value instanceof ConfigurationSerializable) {
            copyDataWrapper(path, ofConfig((ConfigurationSerializable) value));
        } else if (value instanceof Map) {
            setMap(key, (Map) value);
        } else if (value instanceof Collection) {
            setCollection(key, (Collection) value);
        } else if (value.getClass().isArray()) {
            if (value instanceof byte[]) {
                this.data.put(key, ArrayUtils.clone((byte[]) value));
            } else if (value instanceof short[]) {
                this.data.put(key, ArrayUtils.clone((short[]) value));
            } else if (value instanceof int[]) {
                this.data.put(key, ArrayUtils.clone((int[]) value));
            } else if (value instanceof long[]) {
                this.data.put(key, ArrayUtils.clone((long[]) value));
            } else if (value instanceof float[]) {
                this.data.put(key, ArrayUtils.clone((float[]) value));
            } else if (value instanceof double[]) {
                this.data.put(key, ArrayUtils.clone((double[]) value));
            } else if (value instanceof boolean[]) {
                this.data.put(key, ArrayUtils.clone((boolean[]) value));
            } else {
                this.data.put(key, ArrayUtils.clone((Object[]) value));
            }
        } else if (value instanceof String) {
            this.data.put(key, sanitiseString((String) value));
        } else {
            this.data.put(key, value);
        }
        return this;
    }

    private void setCollection(String key, Collection<?> value) {
        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        for (Object object : value) {
            if (object instanceof ConfigurationSerializable) {
                builder.add(ofConfig((ConfigurationSerializable) object));
            } else if (object instanceof Map) {
                builder.add(ensureSerialization((Map) object));
            } else if (object instanceof Collection) {
                builder.add(ensureSerialization((Collection) object));
            } else {
                builder.add(object);
            }
        }

        this.data.put(key, builder.build());
    }

    private ImmutableList<Object> ensureSerialization(Collection<?> collection) {
        ImmutableList.Builder<Object> objectBuilder = ImmutableList.builder();
        collection.forEach(obj -> {
            if (obj instanceof Collection) {
                objectBuilder.add(ensureSerialization((Collection) obj));
            } else if (obj instanceof ConfigurationSerializable) {
                objectBuilder.add(ofConfig((ConfigurationSerializable) obj));
            } else {
                objectBuilder.add(obj);
            }
        });
        return objectBuilder.build();
    }

    private ImmutableMap<?, ?> ensureSerialization(Map<?, ?> map) {
        ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                builder.put(key, ensureSerialization((Map) value));
            } else if (value instanceof ConfigurationSerializable) {
                builder.put(key, ofConfig((ConfigurationSerializable) value));
            } else if (value instanceof Collection) {
                builder.put(key, ensureSerialization((Collection) value));
            } else {
                builder.put(key, value);
            }
        });
        return builder.build();
    }

    private Optional<List<?>> getUnsafeList(DataKey key) {
        return get(key)
                .filter(obj -> obj instanceof List<?> || obj instanceof Object[])
                .map(obj -> {
                            if (obj instanceof List<?>) {
                                return (List<?>) obj;
                            }
                            return Arrays.asList((Object[]) obj);
                        }
                );
    }

    public Optional<List<Byte>> getByteList(DataKey key) {
        return getUnsafeList(key).map(list ->
                list.stream()
                        .map(DataWrapper::asByte)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    public Optional<List<String>> getStringList(DataKey key) {
        return getUnsafeList(key).map(list ->
                list.stream()
                        .map(DataWrapper::asString)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    public <T extends ConfigurationSerializable> Optional<List<T>> getSerializableList(DataKey key, Class<T> clazz) {
        return getUnsafeList(key).map(list -> list.stream()
                .map(obj -> asSerializable(obj, clazz))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
    }
}
