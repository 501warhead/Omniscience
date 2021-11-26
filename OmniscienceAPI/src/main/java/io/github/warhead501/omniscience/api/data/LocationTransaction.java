package io.github.warhead501.omniscience.api.data;

import org.bukkit.Location;

/**
 * A {@link Transaction} that also stores a {@link Location} where the transaction occurred
 *
 * @param <T> The class of the object that is being changed
 */
public class LocationTransaction<T> extends Transaction<T> {

    private final Location location;

    public LocationTransaction(Location location, T originalState, T finalState) {
        super(originalState, finalState);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
