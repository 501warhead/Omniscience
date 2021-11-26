package io.github.warhead501.omniscience.listener.item;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class EventDropListener extends OmniListener {

    public EventDropListener() {
        super(ImmutableList.of("drop"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        OEntry.create().source(event.getPlayer()).dropped(event.getItemDrop()).save();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDropItem(EntityDropItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            OEntry.create().source(event.getEntity()).dropped(event.getItemDrop()).save();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockDispense(BlockDispenseEvent event) {
        OEntry.create().source(event.getBlock()).droppedItem(event.getItem(), event.getBlock().getLocation()).save();
    }
}
