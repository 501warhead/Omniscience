package io.github.warhead501.omniscience.listener.entity;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class EventMountListener extends OmniListener {

    public EventMountListener() {
        super(ImmutableList.of("mount", "dismount"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityMount(EntityMountEvent e) {
        if (isEnabled("mount")) {
            OEntry.create().source(e.getEntity()).mount(false, e.getMount()).save();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDismount(EntityDismountEvent e) {
        if (isEnabled("dismount")) {
            OEntry.create().source(e.getEntity()).mount(true, e.getDismounted()).save();
        }
    }
}
