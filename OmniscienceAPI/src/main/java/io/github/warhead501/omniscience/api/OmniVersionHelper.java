package io.github.warhead501.omniscience.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public enum OmniVersionHelper {
    v1_18_R1("net.minecraft.","nbt.NBTTagCompound",
            "nbt.NBTCompressedStreamTools","world.item.ItemStack","world.entity.Entity","a","b","f"),
    v1_18_R2("net.minecraft.","nbt.NBTTagCompound", "nbt.NBTCompressedStreamTools","world.item.ItemStack","world.entity.Entity","a","b","f"),
    v1_19_R1("net.minecraft.","nbt.NBTTagCompound", "nbt.NBTCompressedStreamTools","world.item.ItemStack","world.entity.Entity","a","b","f"),
    v1_19_R2("net.minecraft.","nbt.NBTTagCompound", "nbt.NBTCompressedStreamTools","world.item.ItemStack","world.entity.Entity","a","b","f"),
    v1_19_R3("net.minecraft.","nbt.NBTTagCompound", "nbt.NBTCompressedStreamTools","world.item.ItemStack","world.entity.Entity","a","b","f"),
    v1_20_R1("net.minecraft.","nbt.NBTTagCompound", "nbt.NBTCompressedStreamTools","world.item.ItemStack","world.entity.Entity","a","b","f");
    @Getter private final String NMS_PATH;
    @Getter private final String NBTTagCompound;
    @Getter private final String NBTCompressedStreamTools;
    @Getter private final String NMSItemStack;
    @Getter private final String NMSEntity;

    @Getter private final String setCompoundFloatMethodName;
    @Getter private final String saveToJsonMethodName;
    @Getter private final String saveEntityToJson;

    @Nullable
    public static OmniVersionHelper get(@NotNull String nmsVersion) {
        for (OmniVersionHelper version : OmniVersionHelper.values()) {
            if (version.name().equals(nmsVersion)) return version;
        }
        return null;
    }
}
