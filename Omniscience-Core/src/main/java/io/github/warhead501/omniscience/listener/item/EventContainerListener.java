package io.github.warhead501.omniscience.listener.item;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class EventContainerListener extends OmniListener {

    public EventContainerListener() {
        super(ImmutableList.of("open", "close"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (isEnabled("open")
                && (event.getInventory().getHolder() instanceof Container || event.getInventory().getHolder() instanceof DoubleChest)) {
            OEntry.create().source(event.getPlayer()).opened(event.getInventory().getHolder()).save();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (isEnabled("close")
                && (event.getInventory().getHolder() instanceof Container || event.getInventory().getHolder() instanceof DoubleChest)) {
            OEntry.create().source(event.getPlayer()).closed(event.getInventory().getHolder()).save();
        }
    }
}
