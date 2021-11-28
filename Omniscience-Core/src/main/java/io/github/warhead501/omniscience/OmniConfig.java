package io.github.warhead501.omniscience;

import io.github.warhead501.omniscience.io.dynamo.DynamoStorageHandler;
import io.github.warhead501.omniscience.io.StorageHandler;
import io.github.warhead501.omniscience.io.mongo.MongoStorageHandler;
import lv.voop.essn.paper.utils.item.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * The singular location for all end user configured directives from the plugin.yml
 *
 * @author 501warhead
 */
public enum OmniConfig {
    /**
     * The one, the only, the instance of OmniConfig
     */
    INSTANCE;

    private String databaseName;
    private String authenticationDatabaseName;
    private DatabaseType dbType;

    private boolean debugEnabled;

    private boolean defaultsEnabled;
    private int defaultRadius;
    private String defaultSearchTime;
    private int radiusLimit;
    private int lookupSizeLimit;
    private String dateFormat;
    private ChatColor primary = ChatColor.AQUA;
    private int actionablesLimit;
    private String recordExpiry;
    private int maxPoolSize;
    private int minPoolSize;
    private int purgeBatchLimit;
    private ChatColor secondary = ChatColor.GREEN;
    private String simpleDateFormat;
    private String tableName;

    private boolean worldEditInteraction;
    private boolean faweInteraction;
    private boolean craftBookInteraction;

    private Material wandMaterial;

    void setup(FileConfiguration configuration) {
        if (dbType == null) {
            dbType = DatabaseType.valueOf(configuration.getString("database.type").toUpperCase());
        }
        this.debugEnabled = configuration.getBoolean("debug");

        this.databaseName = configuration.getString("database.name");
        this.authenticationDatabaseName = configuration.getString("mongodb.authenticationDatabase");
        this.tableName = configuration.getString("database.dataTableName");
        this.defaultsEnabled = configuration.getBoolean("defaults.enabled");
        this.defaultRadius = configuration.getInt("defaults.radius");
        this.defaultSearchTime = configuration.getString("defaults.time");
        this.radiusLimit = configuration.getInt("limits.radius");
        this.lookupSizeLimit = configuration.getInt("limits.lookup.size");
        this.actionablesLimit = configuration.getInt("limits.actionables");
        this.dateFormat = configuration.getString("display.format");
        this.simpleDateFormat = configuration.getString("display.simpleFormat");
        this.recordExpiry = configuration.getString("storage.expireRecords");
        this.maxPoolSize = configuration.getInt("storage.maxPoolSize");
        this.minPoolSize = configuration.getInt("storage.minPoolSize");
        this.purgeBatchLimit = configuration.getInt("storage.purgeBatchLimit");

        this.worldEditInteraction = configuration.getBoolean("integration.worldEdit");
        this.faweInteraction = configuration.getBoolean("integration.fastAsyncWorldEdit");
        this.craftBookInteraction = configuration.getBoolean("integration.craftbookSigns");

        ConfigurationSection section = configuration.getConfigurationSection("events");
        for (String key : section.getKeys(false)) {
            ConfigurationSection innerSection = section.getConfigurationSection(key);
            OmniEventRegistrar.INSTANCE.addEvent(key, innerSection.getString("past"), innerSection.getBoolean("enabled"));
        }

        String wandMaterialName = configuration.getString("wand.material");

        wandMaterial = Material.matchMaterial(wandMaterialName);
        if (wandMaterial == null || !wandMaterial.isBlock()) {
            wandMaterial = Material.REDSTONE_LAMP;
            Omniscience.getPluginInstance().getLogger().warning("Invalid configuration option for wand.material: " + wandMaterialName + ". Defaulting to REDSTONE_LAMP");
        }
    }

    /**
     * @return The database type selected by the end user.
     */
    public DatabaseType getDbType() {
        return dbType;
    }

    /**
     * @return The desired name for database entries
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @return The name of the database where the users are defined for login purposes
     */
    public String getAuthenticationDatabaseName() {
        return authenticationDatabaseName;
    }

    /**
     * @return The name for the main, primary table to store data
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return The desired primary color for all of our command color schemes
     */
    public ChatColor getPrimary() {
        return primary;
    }

    public void setPrimary(ChatColor primary) {
        this.primary = primary;
    }

    /**
     * @return The desired secondary color for all of our command color schemes
     */
    public ChatColor getSecondary() {
        return secondary;
    }

    public void setSecondary(ChatColor secondary) {
        this.secondary = secondary;
    }

    /**
     * @return Whether or not parameter defaults are enabled
     */
    public boolean areDefaultsEnabled() {
        return defaultsEnabled;
    }

    /**
     * @return The specific date format used for aggregate records.
     */
    public String getSimpleDateFormat() {
        return simpleDateFormat;
    }

    /**
     * @return The default radius to use for searches if {@link #areDefaultsEnabled()} is true
     */
    public int getDefaultRadius() {
        return defaultRadius;
    }

    /**
     * @return The default time to use for searches if {@link #areDefaultsEnabled()} is true
     */
    public String getDefaultSearchTime() {
        return defaultSearchTime;
    }

    /**
     * @return The maximum radius a player can use without having the override permission
     */
    public int getRadiusLimit() {
        return radiusLimit;
    }

    /**
     * @return The maximum amount of records one user can parse at one time for lookups
     */
    public int getLookupSizeLimit() {
        return lookupSizeLimit;
    }

    /**
     * @return The specific date format used for non-aggregate records
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * @return Whether or not we'll perform our worldedit related interactions, such as allowing users to specify a worldedit selection to search in
     */
    public boolean doWorldEditInteraction() {
        return worldEditInteraction;
    }

    /**
     * @return Whether or not we'll perform our FastAsyncWorldEdit related interactions, such as async rollbacks [NYI]
     */
    public boolean doFaweInteraction() {
        return faweInteraction;
    }

    /**
     * @return Whether or not we'll perform our CraftBook related interactions, such as logging sign usage.
     */
    public boolean doCraftBookInteraction() {
        return craftBookInteraction;
    }

    /**
     * @return The maximum amount of items that can be rolled back at a single time
     */
    public int getActionablesLimit() {
        return actionablesLimit;
    }

    /**
     * @return How long records are kept for before being discarded
     */
    public String getRecordExpiry() {
        return recordExpiry;
    }

    /**
     * @return The maximum connection pool size
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * @return The minimum connection pool size
     */
    public int getMinPoolSize() {
        return minPoolSize;
    }

    /**
     * @return The maximum amount of records to be purged at once
     */
    public int getPurgeBatchLimit() {
        return purgeBatchLimit;
    }

    /**
     * @return The material used for the search wand. Must be a block.
     */
    @Deprecated public Material getWandMaterial() {
        return wandMaterial;
    }

    /**
     * @return The item stack used for the search wand. Must be a block.
     */
    public ItemStack getWandItem() {
        ItemStack is = new ItemStack(this.wandMaterial);
        ItemUtils.setCustomTag(is,"omnisciencetool","tool");
        return is;
    }

    /**
     * @return Whether debug messages are enabled
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    enum DatabaseType {
        /**
         * https://www.mongodb.com/
         */
        MONGODB(MongoStorageHandler.class),
        /**
         * https://aws.amazon.com/dynamodb/
         */
        DYNAMODB(DynamoStorageHandler.class);

        Class<? extends StorageHandler> storageClass;

        DatabaseType(Class<? extends StorageHandler> storageClass) {
            this.storageClass = storageClass;
        }

        public Class<? extends StorageHandler> getStorageClass() {
            return storageClass;
        }

        public StorageHandler invokeConstructor() throws Exception {
            return storageClass.getConstructor().newInstance();
        }
    }
}
