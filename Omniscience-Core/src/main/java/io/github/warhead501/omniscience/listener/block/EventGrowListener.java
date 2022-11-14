package io.github.warhead501.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.data.LocationTransaction;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

public class EventGrowListener extends OmniListener {

    public EventGrowListener() {
        super(ImmutableList.of("grow"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStructureGrow(StructureGrowEvent e) {
        if (isEnabled("grow")) {
            for (BlockState block : e.getBlocks()) {
                OEntry.create().source(e.getPlayer()).grewBlock(new LocationTransaction<>(block.getLocation(), null, block)).save();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockGrow(BlockGrowEvent e) {
        if (isEnabled("grow")) {
            OEntry.create().source(null).grewBlock(new LocationTransaction<>(e.getBlock().getLocation(), e.getBlock().getState(), e.getNewState())).save();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockSpread(BlockSpreadEvent e) {
        if (isEnabled("grow")) {
            OEntry.create().source(null).grewBlock(new LocationTransaction<>(e.getBlock().getLocation(), e.getBlock().getState(), e.getNewState())).save();
        }
    }
}
