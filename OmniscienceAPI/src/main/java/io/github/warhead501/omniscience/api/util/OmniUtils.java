package io.github.warhead501.omniscience.api.util;

import lombok.experimental.UtilityClass;
import lv.voop.essn.paper.utils.item.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class OmniUtils {
    public static boolean toolPermissionCheck(Player player) {
        if (!player.hasPermission("omniscience.commands.tool")) return false;
        if (!player.hasPermission("omniscience.mayuse")) return false;
        return true;
    }
    public static boolean isOmniTool(ItemStack item) {
        if (ItemUtils.hasCustomTag(item,"omnisciencetool")) return true;
        return false;
    }
}
