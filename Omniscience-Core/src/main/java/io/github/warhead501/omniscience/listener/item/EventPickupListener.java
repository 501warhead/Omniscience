package io.github.warhead501.omniscience.listener.item;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.Omniscience;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.api.util.OmniUtils;
import io.github.warhead501.omniscience.listener.OmniListener;
import lv.voop.essn.paper.utils.item.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import static io.github.warhead501.omniscience.command.OmniSubCommand.GREEN;

public class EventPickupListener extends OmniListener {

    public EventPickupListener() {
        super(ImmutableList.of("pickup"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        OEntry.create().source(event.getEntity()).pickup(event.getItem()).save();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onToolPickup(PlayerPickupItemEvent e) {
        var item = e.getItem().getItemStack();
        var player = e.getPlayer();
        if (!OmniUtils.isOmniTool(item)) return;
        if (!OmniUtils.toolPermissionCheck(player)) {
            e.setCancelled(true);
            e.getItem().remove();
            return;
        }

    }

}
