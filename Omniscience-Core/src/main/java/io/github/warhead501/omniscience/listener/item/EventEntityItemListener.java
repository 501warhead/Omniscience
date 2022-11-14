package io.github.warhead501.omniscience.listener.item;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class EventEntityItemListener extends OmniListener {

    public EventEntityItemListener() {
        super(ImmutableList.of("entity-withdraw", "entity-deposit"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        if ((e.getPlayerItem() != null && e.getPlayerItem().getType() != Material.AIR)
                && isEnabled("entity-deposit")) {
            OEntry.create().source(e.getPlayer()).putIntoArmorStand(e.getRightClicked(), e.getPlayerItem()).save();
        }
        if ((e.getArmorStandItem() != null && e.getArmorStandItem().getType() != Material.AIR)
                && isEnabled("entity-withdraw")) {
            OEntry.create().source(e.getPlayer()).removedFromArmorStand(e.getRightClicked(), e.getArmorStandItem()).save();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ItemFrame
                && isEnabled("entity-deposit")) {
            ItemFrame frame = (ItemFrame) e.getRightClicked();
            ItemStack item = e.getHand() == EquipmentSlot.HAND ? e.getPlayer().getInventory().getItemInMainHand() : e.getPlayer().getInventory().getItemInOffHand();
            if ((frame.getItem() == null || frame.getItem().getType() == Material.AIR)
                    && item != null) {
                OEntry.create().source(e.getPlayer()).putIntoItemFrame(frame, item).save();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ItemFrame
                && isEnabled("entity-withdraw")) {
            ItemFrame frame = (ItemFrame) e.getEntity();
            if (frame.getItem() != null) {
                OEntry.create().source(e.getDamager()).removedFromItemFrame(frame, frame.getItem()).save();
            }
        }
    }
}
