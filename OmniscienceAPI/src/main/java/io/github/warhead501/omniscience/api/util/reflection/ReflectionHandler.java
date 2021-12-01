package io.github.warhead501.omniscience.api.util.reflection;

import lv.voop.essn.paper.utils.EssnPaperUtil;
import lv.voop.essn.paper.utils.GameVersion;
import org.bson.internal.Base64;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * A class I basically stitched together from Sporadic's code. - 501warhead
 *
 * @author Sporadic
 */
public final class ReflectionHandler {

    final private static String CRAFTBUKKIT_PATH = "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".";
    final private static String PATH = GameVersion.get(EssnPaperUtil.getNMSVersion()).equals(GameVersion.v1_18_R1)?"net.minecraft.":"net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".";
    private static Method asNMSCopy;
    private static Method getNMSEntity;
    private static Method loadEntityFromNBT;
    private static Constructor<?> compoundConstructor;
    private static Method saveToJson;
    private static Method saveEntityToJson;
    private static Method setCompoundUUID;
    private static Method setCompoundFloat;

    private static Method streamToolsLoadCompoundFromInput;
    private static Method streamToolsWriteCompoundToOutput;

    static {
        try {
            Class<?> NBTTagCompound;
            Class<?> NMSItemStack;
            Class<?> NMSEntity;
            Class<?> NBTCompressedStreamTools;
            if (GameVersion.get(EssnPaperUtil.getNMSVersion()).isNewNmsName()) {
                NBTTagCompound = Class.forName(PATH + "nbt.NBTTagCompound");
                NBTCompressedStreamTools = Class.forName(PATH + "nbt.NBTCompressedStreamTools");
                NMSItemStack = Class.forName(PATH + "world.item.ItemStack");
                NMSEntity = Class.forName(PATH + "world.entity.Entity");
            } else {
                NBTTagCompound = Class.forName(PATH + "NBTTagCompound");
                NMSItemStack = Class.forName(PATH + "ItemStack");
                NMSEntity = Class.forName(PATH + "Entity");
                NBTCompressedStreamTools = Class.forName(PATH + "NBTCompressedStreamTools");
            }

            Class<?> craftBukkitEntity = Class.forName(CRAFTBUKKIT_PATH + "entity.CraftEntity");
            Class<?> craftBukkitItemStack = Class.forName(CRAFTBUKKIT_PATH + "inventory.CraftItemStack");

            setCompoundFloat = NBTTagCompound.getMethod(GameVersion.get(EssnPaperUtil.getNMSVersion()).isNewNmsName()?"a":"setFloat", String.class, float.class);
            for (Method method : NMSEntity.getMethods()) {
                for (Type type : method.getGenericParameterTypes()) {
                    if (type.getTypeName().equalsIgnoreCase(NBTTagCompound.getTypeName())
                            && method.getReturnType().equals(Void.TYPE)) {
                        loadEntityFromNBT = method;
                        break;
                    }
                }
            }

            for (Method method : NBTTagCompound.getMethods()) {
                boolean stringMatch = false;
                for (Type type : method.getGenericParameterTypes()) {
                    if (type.getTypeName().equalsIgnoreCase(String.class.getTypeName())
                            && !stringMatch) {
                        stringMatch = true;
                    }
                    if (stringMatch
                            && type.getTypeName().equalsIgnoreCase(UUID.class.getTypeName())
                            && method.getReturnType().equals(Void.TYPE)) {
                        setCompoundUUID = method;
                        break;
                    }
                }
            }

            for (Method method : NBTCompressedStreamTools.getMethods()) {
                Type[] parameterTypes = method.getGenericParameterTypes();
                if (method.getReturnType().equals(Void.TYPE)) {
                    if (parameterTypes.length == 2
                            && parameterTypes[0].getTypeName().equals(NBTTagCompound.getTypeName())
                            && parameterTypes[1].getTypeName().equals(OutputStream.class.getTypeName())) {
                        streamToolsWriteCompoundToOutput = method;
                    }
                } else if (method.getReturnType().equals(NBTTagCompound)) {
                    if (parameterTypes.length == 1
                            && parameterTypes[0].getTypeName().equals(InputStream.class.getTypeName())) {
                        streamToolsLoadCompoundFromInput = method;
                    }
                }
            }
            compoundConstructor = NBTTagCompound.getConstructor();

            saveToJson = NMSItemStack.getMethod(GameVersion.get(EssnPaperUtil.getNMSVersion()).isNewNmsName()?"b":"save", NBTTagCompound);
            saveEntityToJson = NMSEntity.getMethod(GameVersion.get(EssnPaperUtil.getNMSVersion()).isNewNmsName()?"f":"save", NBTTagCompound);

            getNMSEntity = craftBukkitEntity.getMethod("getHandle");
            asNMSCopy = craftBukkitItemStack.getMethod("asNMSCopy", ItemStack.class);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static String getItemJson(ItemStack is) {
        try {
            Object nmsStack = getMinecraftItemStack(is);
            Object compound = compoundConstructor.newInstance();

            saveToJson.invoke(nmsStack, compound);
            return compound.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static String getEntityAsBytes(Entity entity) {
        try {
            Object nmsEntity = getMinecraftEntity(entity);
            Object compound = compoundConstructor.newInstance();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            saveEntityToJson.invoke(nmsEntity, compound);
            streamToolsWriteCompoundToOutput.invoke(null, compound, stream);
            byte[] val = stream.toByteArray();
            stream.close();

            return Base64.encode(val);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * This method will load entity data that has been stored in nbt into the entity. This will create a LITERAL COPY OF THE SAVED ENTITY. Basically, everything will be the exact same.
     * Except for UUID and health. UUID is because same uuid entities cause fucking problems. Health because otherwise the entity is fuckin dead
     *
     * @param entity A base entity to overwrite. As we're loading entity data, you're gonna have to give us an entity to brainwash.
     * @param nbt    The nbt string to parse and load into the entity.
     */
    public static void loadEntityFromNBT(Entity entity, String nbt) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.decode(nbt));
            Object compound = streamToolsLoadCompoundFromInput.invoke(null, (InputStream) stream);
            stream.close();
            if (entity instanceof LivingEntity) {
                setCompoundFloat.invoke(compound, "Health", (float) ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            }

            Object nmsEntity = getMinecraftEntity(entity);
            setCompoundUUID.invoke(compound, "UUID", entity.getUniqueId());
            loadEntityFromNBT.invoke(nmsEntity, compound);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static Object getMinecraftItemStack(ItemStack origin) throws Exception {
        return asNMSCopy.invoke(null, origin);
    }

    private static Object getMinecraftEntity(Entity entity) throws Exception {
        return getNMSEntity.invoke(entity);
    }
}
