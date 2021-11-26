package io.github.warhead501.omniscience.listener.entity;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EventHitListener extends OmniListener {

    public EventHitListener() {
        super(ImmutableList.of("hit", "shot"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (isEnabled("shot")
                && e.getDamager() instanceof Projectile
                && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
            OEntry.create().source(e.getDamager()).shot(e.getEntity()).save();
            return;
        }
        if (isEnabled("hit")
                && (e.getDamager() instanceof Player
                || e.getEntity() instanceof Player)) {
            OEntry.create().source(e.getDamager()).hit(e.getEntity()).save();
        }
    }
}
