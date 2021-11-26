package io.github.warhead501.omniscience.listener.player;

import com.google.common.collect.ImmutableList;

import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventJoinListener extends OmniListener {

    public EventJoinListener() {
        super(ImmutableList.of("join"));
    }

    //Listen on join LOWEST rather than login MONITOR as at this point the player is fully initialized (the location will be valid, location of a player onLogin is spawn), join LOWEST is called immediately after player initialization before other plugins can modify the location
    //Note: This may not be precise should a plugin modify location on LOWEST, which they /shouldn't/, but who knows. Perhaps a more elegant solution is needed.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        OEntry.create().player(e.getPlayer()).joined(e.getPlayer().getAddress().getAddress().getHostAddress()).save();
    }
}
