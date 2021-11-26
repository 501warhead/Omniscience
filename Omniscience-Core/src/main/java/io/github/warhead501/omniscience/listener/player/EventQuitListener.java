package io.github.warhead501.omniscience.listener.player;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventQuitListener extends OmniListener {

    public EventQuitListener() {
        super(ImmutableList.of("quit"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        OEntry.create().player(e.getPlayer()).quit().save();
    }
}
