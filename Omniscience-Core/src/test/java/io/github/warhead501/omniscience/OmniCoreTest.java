package io.github.warhead501.omniscience;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.warhead501.omniscience.io.StorageHandler;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class OmniCoreTest {

    @Mock
    Omniscience omniscience;

    @Mock
    PluginCommand command;

    @Mock
    PluginManager manager;

    @Mock
    Server server;

    @Mock
    BukkitScheduler scheduler;

    @Mock
    Logger logger;

    @Mock
    StorageHandler handler;

    @Before
    public void setup() {
        /*doNothing().when(omniscience).saveDefaultConfig();
        when(omniscience.getConfig()).thenReturn(getDummyConfiguration());
        when(omniscience.getCommand("omniscience")).thenReturn(command);
        when(omniscience.getCommand("omnitele")).thenReturn(command);
        when(omniscience.getServer()).thenReturn(server);
        when(omniscience.getLogger()).thenReturn(logger);
        when(server.getPluginManager()).thenReturn(manager);*/
    }

    @Test
    public void onEnable() {
        OmniCore core = new OmniCore();
        //core.onEnable(omniscience, scheduler);
        //verify(omniscience, times(1)).saveDefaultConfig();
    }

    @Test
    public void onLoad() {
        OmniCore core = new OmniCore();
        core.onLoad(omniscience);
    }

    @Test
    public void onDisable() {
        OmniCore core = new OmniCore();
        core.onDisable(omniscience);
    }

    private FileConfiguration getDummyConfiguration() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("database.type", "mongodb");
        configuration.set("database.name", "Omniscience");
        configuration.set("database.dataTableName", "DataEntry");
        configuration.set("wand.material", Material.REDSTONE_LAMP.name());
        List<Map<String, Map<String, Object>>> values = Lists.newArrayList();
        Map<String, Object> deepVals = Maps.newHashMap();
        deepVals.put("address", "90.0.12.3");
        deepVals.put("port", 27017);
        deepVals.put("usesauth", false);
        deepVals.put("user", "username");
        deepVals.put("pass", "password");
        Map<String, Map<String, Object>> vals = Maps.newHashMap();
        vals.put("ServerA", deepVals);
        values.add(vals);
        configuration.set("mongodb.servers", values);
        return configuration;
    }
}