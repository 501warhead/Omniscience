package io.github.warhead501.omniscience.api.util;

import io.github.warhead501.omniscience.api.OmniApi;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class OmniUtils {
    public static boolean toolPermissionCheck(Player player) {
        if (!player.hasPermission("omniscience.commands.tool")) return false;
        if (!player.hasPermission("omniscience.mayuse")) return false;
        return true;
    }
    public static boolean isOmniTool(ItemStack item) {
        var meta = item.getItemMeta();
        if (meta==null) return false;
        var container = meta.getPersistentDataContainer();
        return container.has(OmniApi.getOmniscience().getItemKey(),PersistentDataType.STRING);
    }

    @NotNull
    public static String getNMSVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
