package io.github.warhead501.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.Omniscience;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class EventIgniteListener extends OmniListener {

    public EventIgniteListener() {
        super(ImmutableList.of("ignite"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getBlock() == null || event.getBlock().getType().name().contains("AIR")) {
            return;
        }
        //TODO we need to track this... but it's complex.
        if (event.getPlayer() != null) {
            OEntry.create().source(event.getPlayer()).ignited(event.getBlock()).save();
            event.getBlock().setMetadata("player-source", new FixedMetadataValue(Omniscience.getPluginInstance(), event.getPlayer().getUniqueId().toString()));
        } else if (event.getIgnitingBlock() != null && event.getIgnitingBlock().hasMetadata("player-source")) {
            OEntry.create().environment().ignited(event.getBlock()).save();
            List<MetadataValue> metadataValues = event.getIgnitingBlock().getMetadata("player-source");
            for (MetadataValue value : metadataValues) {
                if (value.getOwningPlugin() instanceof Omniscience) {
                    event.getBlock().setMetadata("player-source", value);
                    return;
                }
            }
        }
    }
}
