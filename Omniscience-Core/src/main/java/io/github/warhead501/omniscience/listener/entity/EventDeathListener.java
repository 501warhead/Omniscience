package io.github.warhead501.omniscience.listener.entity;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EventDeathListener extends OmniListener {

	public EventDeathListener() {
		super(ImmutableList.of("death"));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent e) {
		if (!(e.getEntityType() == EntityType.ARMOR_STAND && e.getEntity().getKiller() == null))
			OEntry.create().source(e.getEntity().getKiller()).kill(e.getEntity()).save();
		for (ItemStack drop : e.getDrops()) {
			OEntry.create().source(e.getEntity()).droppedItem(drop, e.getEntity().getLocation()).save();
		}
	}

}
