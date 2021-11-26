package io.github.warhead501.omniscience.listener.entity;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

public class EventInteractAtEntity extends OmniListener {

    public EventInteractAtEntity() {
        super(ImmutableList.of("named"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityName(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (!(entity instanceof LivingEntity) || entity instanceof Player || entity instanceof EnderDragon) {
            return;
        }

        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() == Material.NAME_TAG
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && !hasSameName(entity, item)) {
            OEntry.create().source(e.getPlayer())
                    .named(entity, entity.getCustomName(), item.getItemMeta().getDisplayName()).save();
        }
    }

    private boolean hasSameName(Entity entity, ItemStack item) {
        String name = entity.getCustomName();
        if (entity.getCustomName() != null) {
            return name.equals(item.getItemMeta().getDisplayName());
        }
        return false;
    }
}
