package io.github.warhead501.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.data.LocationTransaction;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.LeavesDecayEvent;

public class EventDecayListener extends OmniListener {

    public EventDecayListener() {
        super(ImmutableList.of("decay"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLeavesDecay(LeavesDecayEvent event) {
        OEntry.create().environment().decayedBlock(new LocationTransaction<>(event.getBlock().getLocation(), event.getBlock().getState(), null)).save();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockFade(BlockFadeEvent event) {
        OEntry.create().environment().decayedBlock(new LocationTransaction<>(event.getBlock().getLocation(), event.getBlock().getState(), event.getNewState())).save();
    }
}
