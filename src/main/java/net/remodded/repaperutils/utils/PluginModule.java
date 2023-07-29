package net.remodded.repaperutils.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;

public abstract class PluginModule<T extends Plugin> implements Listener {
    public final String moduleName;

    private boolean enabled;

    private final File configFile;
    protected FileConfiguration config;

    protected final T plugin;
    private final Logger logger;


    protected PluginModule(String moduleName, T plugin) {
        this.plugin = plugin;
        this.moduleName = moduleName;

        logger = LogManager.getLogger(plugin.getName() + "] [" + moduleName);

        configFile = new File(plugin.getDataFolder(), moduleName + ".yml");
        loadConfig();
        saveConfig();
    }

    public final void enable() {
        enable(false);
    }

    public final void enable(boolean force) {
        if(enabled) return;
        loadConfig();

        if (force) {
            config.set("enabled", true);
            saveConfig();
        }

        if(!config.getBoolean("enabled")) return;

        boolean errored = false;
        boolean inited = false;
        try {
            inited = init();
        } catch (Exception ex) {
            exception(ex, "Unable to init module!!! (exception)");

            config.set("enabled", false);
            saveConfig();
            errored = true;
        }

        if(inited) {
            Bukkit.getServer().getPluginManager().registerEvents(this, this.plugin);
            log("Enabled module");
            enabled = true;
        }
        else if(!errored) {
            error("Unable to init module!!!");
        }
    }

    public final void disable() {
        disable(false, false);
    }

    public final void shutdown() {
        disable(false, true);
    }

    private void disable(boolean doReload, boolean force) {
        if (!enabled) return;

        try {
            deinit(doReload);
        } catch (Exception ex) {
            exception(ex, "Unable to disable " + moduleName + " properly");
            return;
        }

        HandlerList.unregisterAll(this);
        log("Disabled module");
        enabled = false;

        if (!doReload && force) {
            config.set("enabled", false);
            saveConfig();
        }
    }

    public final void reload() {
        if(!enabled) return;
        disable(true, false);
        enable();
    }

    protected abstract boolean init();
    protected abstract void deinit(boolean doReload);

    public void setupConfig(ConfigurationSection config) {}

    protected void log(String message) {
        logger.info(message);
    }

    protected void warn(String message) {
        logger.warn(message);
    }

    protected void error(String message) {
        logger.error(message);
    }

    protected void exception(Exception e, String message) {
        logger.error(message);
        logger.error(e);
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (Exception e){
            exception(e, "Could not save config to " + configFile);
        }
    }

    private void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        config.addDefault("enabled", false);
        setupConfig(config);
        config.options().copyDefaults(true);
    }
}
