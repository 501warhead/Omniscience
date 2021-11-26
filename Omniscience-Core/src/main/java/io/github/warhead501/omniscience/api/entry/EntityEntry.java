package io.github.warhead501.omniscience.api.entry;

import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.data.Transaction;
import io.github.warhead501.omniscience.api.util.DataHelper;
import io.github.warhead501.omniscience.api.util.reflection.ReflectionHandler;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class EntityEntry extends DataEntryComplete implements Actionable {

    @Override
    public ActionResult rollback() throws Exception {
        //Get the nbt data that was stored for this entry
        String entityData = data.getString(DataKeys.ENTITY)
                .orElseThrow(() -> skipped(SkipReason.INVALID));
        //Get the entity type string that was stored for this entry
        String entityType = data.getString(DataKeys.ENTITY_TYPE)
                .orElseThrow(() -> skipped(SkipReason.INVALID));

        //Fetch the type of entity that this is
        EntityType type = EntityType.valueOf(entityType);
        //Don't run for players...
        if (type == EntityType.PLAYER) {
            return ActionResult.skipped(SkipReason.INVALID);
        }

        //Create a new entity based on the entity type of this event
        Entity baseEntity = DataHelper.getLocationFromDataWrapper(data)
                .map(loc -> {
                    World world = loc.getWorld();
                    return world.spawnEntity(loc, type);
                }).orElseThrow(() -> skipped(SkipReason.INVALID_LOCATION));

        //Now, complete the rollback by cloning our data into the entity.
        //This is an UNSAFE operation. Data can change from version to version.
        ReflectionHandler.loadEntityFromNBT(baseEntity, entityData);

        return ActionResult.success(new Transaction<>(null, baseEntity));
    }

    @Override
    public ActionResult restore() {
        return ActionResult.skipped(SkipReason.UNIMPLEMENTED);
    }
}
