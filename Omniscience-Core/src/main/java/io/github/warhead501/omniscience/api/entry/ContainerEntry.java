package io.github.warhead501.omniscience.api.entry;

import io.github.warhead501.omniscience.api.data.DataKeys;
import io.github.warhead501.omniscience.api.data.LocationTransaction;
import io.github.warhead501.omniscience.api.util.DataHelper;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ContainerEntry extends DataEntryComplete implements Actionable {

    @Override
    public ActionResult rollback() throws Exception {
        Location location = DataHelper.getLocationFromDataWrapper(data)
                .orElseThrow(() -> new ActionableException(ActionResult.skipped(SkipReason.INVALID_LOCATION)));

        if (!(location.getBlock().getState() instanceof Container)) {
            throw new ActionableException(ActionResult.skipped(SkipReason.INVALID));
        }

        int slotAffected = data.getInt(DataKeys.ITEM_SLOT)
                .orElseThrow(() -> new ActionableException(ActionResult.skipped(SkipReason.INVALID)));

        ItemStack before = (ItemStack) data.getConfigSerializable(DataKeys.BEFORE.then(DataKeys.ITEMSTACK)).orElse(null);

        Container container = (Container) location.getBlock().getState();

        Inventory snapshot = container.getSnapshotInventory();

        container.getInventory().setItem(slotAffected, before);

        return ActionResult.success(new LocationTransaction<>(location, snapshot, container.getInventory()));
    }

    @Override
    public ActionResult restore() throws Exception {
        Location location = DataHelper.getLocationFromDataWrapper(data)
                .orElseThrow(() -> new ActionableException(ActionResult.skipped(SkipReason.INVALID_LOCATION)));

        if (!(location.getBlock().getState() instanceof Container)) {
            throw new ActionableException(ActionResult.skipped(SkipReason.INVALID));
        }

        int slotAffected = data.getInt(DataKeys.ITEM_SLOT)
                .orElseThrow(() -> new ActionableException(ActionResult.skipped(SkipReason.INVALID)));

        ItemStack after = (ItemStack) data.getConfigSerializable(DataKeys.AFTER.then(DataKeys.ITEMSTACK)).orElse(null);

        Container container = (Container) location.getBlock().getState();

        Inventory snapshot = container.getSnapshotInventory();

        container.getInventory().setItem(slotAffected, after);

        return ActionResult.success(new LocationTransaction<>(location, snapshot, container.getInventory()));
    }
}
