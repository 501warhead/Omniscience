package io.github.warhead501.omniscience.listener;

import io.github.warhead501.omniscience.Omniscience;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginInteractionListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent e) {
        handleToggle(e.getPlugin().getName(), false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent e) {
        handleToggle(e.getPlugin().getName(), true);
    }

    private void handleToggle(String name, boolean on) {
        switch (name) {
            case "WorldEdit":
                Omniscience.onWorldEditStatusChange(on);
                break;
            case "CraftBook":
                break;
        }
    }
}
