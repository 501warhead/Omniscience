package net.lordofthecraft.omniscience;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import net.lordofthecraft.omniscience.api.OmniApi;
import net.lordofthecraft.omniscience.api.display.*;
import net.lordofthecraft.omniscience.api.entry.*;
import net.lordofthecraft.omniscience.api.flag.*;
import net.lordofthecraft.omniscience.api.interfaces.IOmniscience;
import net.lordofthecraft.omniscience.api.interfaces.WorldEditHandler;
import net.lordofthecraft.omniscience.api.parameter.*;
import net.lordofthecraft.omniscience.api.util.PastTenseWithEnabled;
import net.lordofthecraft.omniscience.command.OmniscienceCommand;
import net.lordofthecraft.omniscience.command.OmniscienceTabCompleter;
import net.lordofthecraft.omniscience.command.util.OmniTeleCommand;
import net.lordofthecraft.omniscience.io.StorageHandler;
import net.lordofthecraft.omniscience.listener.CraftBookSignListener;
import net.lordofthecraft.omniscience.listener.PluginInteractionListener;
import net.lordofthecraft.omniscience.listener.WandInteractListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.logging.Level;

final class OmniCore implements IOmniscience {

    private List<ParameterHandler> parameterHandlerList = Lists.newArrayList();
    private Map<String, Class<? extends DataEntry>> eventMap = Maps.newHashMap();
    private List<FlagHandler> flagHandlerList = Lists.newArrayList();
    private List<DisplayHandler> displayHandlerList = Lists.newArrayList();
    private Map<UUID, List<ActionResult>> lastActionResults = Maps.newHashMap();

    private Set<UUID> activeWandList = Sets.newHashSet();

    private WorldEditHandler worldEditHandler;
    private StorageHandler storageHandler;

    OmniCore() {
    }

    void onEnable(Omniscience omniscience, BukkitScheduler scheduler) {
        try {
            OmniApi.setCore(this);
        } catch (IllegalAccessException e) {
            omniscience.getLogger().log(Level.SEVERE, "Failed to register Omniscience with the Omniscience API! This is a critical failure, we're shutting down! Is the plugin already running?", e);
            Bukkit.getPluginManager().disablePlugin(omniscience);
        }
        omniscience.saveDefaultConfig();
        OmniConfig.INSTANCE.setup(omniscience.getConfig());
        try {
            this.storageHandler = OmniConfig.INSTANCE.getDbType().invokeConstructor();
            if (!this.storageHandler.connect(omniscience)) {
                omniscience.getLogger().severe("Failed to connect to the database specified, shutting down.");
                Bukkit.getPluginManager().disablePlugin(omniscience);
                return;
            }
        } catch (Exception e) {
            omniscience.getLogger().log(Level.SEVERE, "Failed to connect to database, shutting down.", e);
            Bukkit.getPluginManager().disablePlugin(omniscience);
            return;
        }

        registerEventWrapperClasses();
        registerParameters();
        registerFlags();
        registerDisplayHandlers();

        registerCommands(omniscience);
        registerEventHandlers(omniscience);

        scheduler.runTaskTimerAsynchronously(omniscience,
                new EntryQueueRunner(),
                20,
                20);

        if (omniscience.getConfig().getBoolean("integration.fastAsyncWorldEdit")
                && Bukkit.getServer().getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {

        }

        if (omniscience.getConfig().getBoolean("integration.worldEdit")
                && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {

        }

        omniscience.getLogger().log(Level.INFO, "Omniscience is Awake. None can escape.");
    }

    void onLoad(Omniscience omniscience) {
    }

    void onDisable(Omniscience omniscience) {

    }

    private void registerCommands(Omniscience omniscience) {
        PluginCommand command = omniscience.getCommand("omniscience");
        command.setExecutor(new OmniscienceCommand(this));
        command.setTabCompleter(new OmniscienceTabCompleter());
        if (CommodoreProvider.isSupported()) {
            Commodore commodore = CommodoreProvider.getCommodore(omniscience);

            OmniscienceCommand.registerCompletions(commodore, command);
        } else {
            omniscience.getLogger().warning("Brigadier isn't supported by this server, Omniscience will not use it for suggestions.");
            omniscience.getLogger().warning("(By the way, if you're seeing this message there is a 99.9999999% chance this plugin isn't going to run. Consider using something else.)");
        }
        //A simple command that will do what we expect every single time. Used for teleporting to locations that could be in different worlds. Shouldn't be, but could be.
        omniscience.getCommand("omnitele").setExecutor(new OmniTeleCommand());
    }

    private void registerEventWrapperClasses() {
        registerEvent("break", BlockEntry.class);
        registerEvent("place", BlockEntry.class);
        registerEvent("grow", BlockEntry.class);
        registerEvent("form", BlockEntry.class);
        registerEvent("death", EntityEntry.class);
        registerEvent("withdraw", ContainerEntry.class);
        registerEvent("deposit", ContainerEntry.class);
    }

    private void registerEventHandlers(Omniscience plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new WandInteractListener(), plugin);
        pm.registerEvents(new PluginInteractionListener(), plugin);
        if (OmniConfig.INSTANCE.doCraftBookInteraction()
                && Bukkit.getServer().getPluginManager().isPluginEnabled("CraftBook")) {
            pm.registerEvents(new CraftBookSignListener(), plugin);
        }
        OmniEventRegistrar.INSTANCE.enableEvents(pm, plugin);
    }

    private void registerParameters() {
        registerParameterHandler(new EventParameter());
        registerParameterHandler(new PlayerParameter());
        registerParameterHandler(new RecipientParameter());
        registerParameterHandler(new MessageParameter());
        registerParameterHandler(new RadiusParameter());
        registerParameterHandler(new WorldParameter());
        registerParameterHandler(new TimeParameter());
        registerParameterHandler(new CauseParameter());
        registerParameterHandler(new BlockParameter());
        registerParameterHandler(new IpParameter());
        registerParameterHandler(new EntityParameter());
        registerParameterHandler(new ItemNameParameter());
        registerParameterHandler(new ItemParameter());
        registerParameterHandler(new ItemDescParameter());
        registerParameterHandler(new CustomItemParameter());
        registerParameterHandler(new TargetParameter());
    }

    private void registerFlags() {
        flagHandlerList.add(new FlagExtended());
        flagHandlerList.add(new FlagNoGroup());
        flagHandlerList.add(new FlagOrder());
        flagHandlerList.add(new FlagDrain());
        flagHandlerList.add(new FlagNoChat());
        if (OmniConfig.INSTANCE.areDefaultsEnabled()) {
            flagHandlerList.add(new FlagIgnoreDefault());
            flagHandlerList.add(new FlagGlobal());
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            onWorldEditStatusChange(true);
        }
    }

    void onWorldEditStatusChange(boolean status) {
        if (status && OmniConfig.INSTANCE.doWorldEditInteraction()) {
            if (flagHandlerList.stream().noneMatch(fh -> fh instanceof FlagWorldEditSel)) {
                flagHandlerList.add(new FlagWorldEditSel(Bukkit.getPluginManager().getPlugin("WorldEdit")));
            }
        } else {
            flagHandlerList.removeIf(fh -> fh instanceof FlagWorldEditSel);
        }
    }

    void onCraftBookStatusChange(boolean status) {
        //TODO turn off craft book related events if craftbook isnt on the server
    }

    private void registerDisplayHandlers() {
        displayHandlerList.add(new MessageDisplayHandler());
        displayHandlerList.add(new ItemDisplayHandler());
        displayHandlerList.add(new DamageDisplayHandler());
        displayHandlerList.add(new TeleportDisplayHandler());
    }

    public StorageHandler getStorageHandler() {
        return storageHandler;
    }

    @Override
    public Optional<ParameterHandler> getParameterHandler(String key) {
        return parameterHandlerList.stream().filter(ph -> ph.canHandle(key)).findFirst();
    }

    Optional<DisplayHandler> getDisplayHandler(String key) {
        return displayHandlerList.stream().filter(dh -> dh.handles(key)).findFirst();
    }

    List<ParameterHandler> getParameterHandlerList() {
        return parameterHandlerList;
    }

    Set<String> getEventSet() {
        return eventMap.keySet();
    }

    List<FlagHandler> getFlagHandlerList() {
        return flagHandlerList;
    }

    boolean hasActiveWand(Player player) {
        return activeWandList.contains(player.getUniqueId());
    }

    void wandActivateFor(Player player) {
        activeWandList.add(player.getUniqueId());
    }

    void wandDeactivateFor(Player player) {
        activeWandList.remove(player.getUniqueId());
    }

    void addLastActionResults(UUID id, List<ActionResult> results) {
        this.lastActionResults.put(id, results);
    }

    Optional<List<ActionResult>> getLastActionResults(UUID id) {
        return Optional.ofNullable(lastActionResults.get(id));
    }

    void registerEvent(String event, Class<? extends DataEntry> clazz) {
        eventMap.put(event, clazz);
    }

    @Override
    public void registerEvent(String event, String pastTense) {
        OmniEventRegistrar.INSTANCE.addEvent(event, pastTense, true);
    }

    void registerDisplayHandler(DisplayHandler handler) {
        displayHandlerList.add(handler);
    }

    @Override
    public void registerFlagHandler(FlagHandler handler) {
        flagHandlerList.add(handler);
    }

    @Override
    public void registerParameterHandler(ParameterHandler handler) {
        if (parameterHandlerList.stream()
                .flatMap(fHandler -> fHandler.getAliases().stream())
                .anyMatch(handler::canHandle)) {
            throw new IllegalArgumentException("A handler was attempted to be registered that has conflicting flags with another handler! " + handler.getClass());
        }
        parameterHandlerList.add(handler);
    }

    @Override
    public Optional<Class<? extends DataEntry>> getEventClass(String name) {
        return Optional.ofNullable(eventMap.get(name));
    }

    @Override
    public Optional<FlagHandler> getFlagHandler(String key) {
        return flagHandlerList.stream().filter(flagHandler -> flagHandler.handles(key)).findFirst();
    }

    @Override
    public void info(String info) {
        Omniscience.getPluginInstance().getLogger().info(info);
    }

    @Override
    public void warning(String warning) {
        Omniscience.getPluginInstance().getLogger().warning(warning);
    }

    @Override
    public void severe(String error) {
        Omniscience.getPluginInstance().getLogger().severe(error);
    }

    @Override
    public void log(Level level, String message, Throwable ex) {
        Omniscience.getPluginInstance().getLogger().log(level, message, ex);
    }

    @Override
    public boolean areDefaultsEnabled() {
        return OmniConfig.INSTANCE.areDefaultsEnabled();
    }

    @Override
    public List<ParameterHandler> getParameters() {
        return ImmutableList.copyOf(parameterHandlerList);
    }

    @Override
    public Map<String, PastTenseWithEnabled> getEvents() {
        return OmniEventRegistrar.INSTANCE.getEventMapping();
    }

    @Override
    public String getDefaultTime() {
        return OmniConfig.INSTANCE.getDefaultSearchTime();
    }

    @Override
    public int getDefaultRadius() {
        return OmniConfig.INSTANCE.getDefaultRadius();
    }

    @Override
    public int getMaxRadius() {
        return OmniConfig.INSTANCE.getRadiusLimit();
    }

    @Override
    public String getSimpleDateFormat() {
        return OmniConfig.INSTANCE.getSimpleDateFormat();
    }

    @Override
    public void registerWorldEditHandler(WorldEditHandler handler) {
        this.worldEditHandler = handler;
    }

    @Override
    public String getDateFormat() {
        return OmniConfig.INSTANCE.getDateFormat();
    }
}
