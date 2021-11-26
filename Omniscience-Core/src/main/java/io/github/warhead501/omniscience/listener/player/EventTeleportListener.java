package io.github.warhead501.omniscience.listener.player;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EventTeleportListener extends OmniListener {

	public EventTeleportListener() {
		super(ImmutableList.of("teleport"));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void teleport(PlayerTeleportEvent event) {
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN && isEnabled("teleport")) { // Thrown out unknowns
			OEntry.create().player(event.getPlayer()).teleported(event.getFrom(), event.getTo(), event.getCause()).save();
		}
	}
}
