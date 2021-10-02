package net.lordofthecraft.omniscience.api.data;

/**
 * A collection of easily accessed keys that are used by this plugin
 *
 * @author 501warhead
 */
public final class DataKeys {

    public static final DataKey WORLD = DataKey.of("World");
    public static final DataKey X = DataKey.of("X");
    public static final DataKey Y = DataKey.of("Y");
    public static final DataKey Z = DataKey.of("Z");
    public static final DataKey EVENT_NAME = DataKey.of("Event");
    public static final DataKey PLAYER_ID = DataKey.of("Player");
    public static final DataKey CAUSE = DataKey.of("Cause");
    public static final DataKey TARGET = DataKey.of("Target");
    public static final DataKey TARGET_META = DataKey.of("TargetMeta");
    public static final DataKey COUNT = DataKey.of("Count");
    public static final DataKey CREATED = DataKey.of("Created");
    public static final DataKey BLOCK_DATA = DataKey.of("BlockData");
    public static final DataKey MATERIAL_TYPE = DataKey.of("MaterialType");
    public static final DataKey ENTITY_TYPE = DataKey.of("EntityType");
    public static final DataKey ENTITY_ID = DataKey.of("EntityID");
    public static final DataKey ITEMSTACK = DataKey.of("ItemStack");
    public static final DataKey ORIGINAL_BLOCK = DataKey.of("OriginalBlock");
    public static final DataKey NEW_BLOCK = DataKey.of("NewBlock");
    public static final DataKey IPADDRESS = DataKey.of("IpAddress");
    public static final DataKey QUANTITY = DataKey.of("Quantity");
    public static final DataKey MESSAGE = DataKey.of("Message");
    public static final DataKey LOCATION = DataKey.of("Location");
    public static final DataKey ENTITY = DataKey.of("Entity");
    public static final DataKey SIGN_TEXT = DataKey.of("SignText");
    public static final DataKey INVENTORY = DataKey.of("Inventory");
    public static final DataKey ITEM_SLOT = DataKey.of("ItemSlot");
    public static final DataKey CONFIG_CLASS = DataKey.of("ClassName");
    public static final DataKey BANNER_PATTERNS = DataKey.of("BannerPatterns");
    public static final DataKey NAME = DataKey.of("Name");
    public static final DataKey BEFORE = DataKey.of("Before");
    public static final DataKey AFTER = DataKey.of("After");
    public static final DataKey RECORD = DataKey.of("Record");
    public static final DataKey DAMAGE_CAUSE = DataKey.of("DamageCause");
    public static final DataKey DAMAGE_AMOUNT = DataKey.of("DamageAmount");
    public static final DataKey TELEPORT_CAUSE = DataKey.of("TeleportCause");

    //META TAGS
    public static final DataKey DISPLAY_METHOD = DataKey.of("DisplayMethod");

    private DataKeys() {
    }
}
