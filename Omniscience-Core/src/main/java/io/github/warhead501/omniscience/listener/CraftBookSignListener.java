package io.github.warhead501.omniscience.listener;

import io.github.warhead501.omniscience.api.entry.OEntry;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CraftBookSignListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock()
                && event.getClickedBlock().getState() instanceof Sign) {
            OEntry.create().player((OfflinePlayer) event.getPlayer()).signInteract(event.getClickedBlock().getLocation(), (Sign) event.getClickedBlock().getState()).save();
        }
    }
}
