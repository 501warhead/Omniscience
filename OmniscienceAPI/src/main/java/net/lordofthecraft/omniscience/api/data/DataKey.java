package net.lordofthecraft.omniscience.api.data;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * This idea is largely attributed to Sponge. They tend do this system really well in terms of MongoDB/NoSQL solutions.
 * <p>
 * A DataKey is essentially the location of data in a {@link DataWrapper}. If a Key is "name", then it'll get the value at "name" in the wrapper.
 * If a key is "name.first" then it'll go and grab the {@link DataWrapper} that is stored in "name" and then it'll find the value for "first" in that nested wrapper.
 * This can go to extreme depths.
 * </p>
 */
public final class DataKey {

    private final ImmutableList<String> parts;

    private ImmutableList<DataKey> keyParts;

    private DataKey(String... parts) {
        this.parts = ImmutableList.copyOf(parts);
    }

    private DataKey(List<String> parts) {
        ImmutableList<String> newParts;
        try {
             newParts = ImmutableList.copyOf(parts);
        } catch (Exception e) {
            ImmutableList.Builder builder = ImmutableList.<String>builder();
            for(String st : parts) {
                builder.add(st);
            }
            newParts = builder.build();
            e.printStackTrace();
        }
        this.parts = newParts;
    }

    public static DataKey of(String... values) {
        return new DataKey(values);
    }

    public static DataKey of(List<String> values) {
        return new DataKey(values);
    }

    public ImmutableList<String> getParts() {
        return parts;
    }

    public DataKey then(DataKey that) {
        ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();

        builder.addAll(this.parts);
        builder.addAll(that.parts);

        return new DataKey(builder.build());
    }

    public DataKey then(String that) {
        ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();

        builder.addAll(this.parts);
        builder.add(that);

        return new DataKey(builder.build());
    }

    public List<DataKey> getKeyParts() {
        if (this.keyParts == null) {
            ImmutableList.Builder<DataKey> builder = ImmutableList.builder();
            for (String part : getParts()) {
                builder.add(new DataKey(part));
            }
            this.keyParts = builder.build();
        }
        return this.keyParts;
    }

    public DataKey pop() {

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (int i = 0; i < this.parts.size() - 1; i++) {
            builder.add(this.parts.get(i));
        }
        return new DataKey(builder.build());
    }

    public DataKey popFirst() {

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (int i = 1; i < this.parts.size(); i++) {
            builder.add(this.parts.get(i));
        }
        return new DataKey(builder.build());
    }

    public DataKey last() {
        if (this.parts.size() <= 1) {
            return this;
        }
        return new DataKey(this.parts.get(this.parts.size() - 1));
    }

    public String asString(String seperator) {
        return Joiner.on(seperator).join(this.parts);
    }

    public String asString(char separator) {
        return asString(String.valueOf(separator));
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataKey)) return false;
        DataKey dataKey = (DataKey) o;
        return Objects.equals(parts, dataKey.parts);
    }

    @Override
    public String toString() {
        return asString('.');
    }
}
