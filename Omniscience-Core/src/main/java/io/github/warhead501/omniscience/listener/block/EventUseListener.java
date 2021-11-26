package io.github.warhead501.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import io.github.warhead501.omniscience.api.entry.OEntry;
import io.github.warhead501.omniscience.OmniConfig;
import io.github.warhead501.omniscience.listener.OmniListener;
import org.bukkit.block.Block;
import org.bukkit.block.DaylightDetector;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventUseListener extends OmniListener {

    public EventUseListener() {
        super(ImmutableList.of("use"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (isEnabled("use")) {
            BlockData data = e.getClickedBlock().getBlockData();
            if (data instanceof Openable
                    || data instanceof Switch
                    || data instanceof Repeater
                    || data instanceof NoteBlock
                    || data instanceof DaylightDetector
                    || data instanceof Cake
                    || (OmniConfig.INSTANCE.doCraftBookInteraction() && isCraftBookSign(e.getClickedBlock()))) {
                OEntry.create().source(e.getPlayer()).use(e.getClickedBlock()).save();
            }
        }
    }

    private boolean isCraftBookSign(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Sign || data instanceof WallSign) {
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();
            String line2 = sign.getLine(1);
            return line2 != null && line2.startsWith("[");
        }
        return false;
    }
}
