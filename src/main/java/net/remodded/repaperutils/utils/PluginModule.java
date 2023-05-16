package net.remodded.repaperutils.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class PluginModule<T extends Plugin> implements Listener {
    public final String moduleName;

    private boolean enabled;

    protected ConfigurationSection config;

    protected final T plugin;
    private final Logger logger;

    protected PluginModule(String moduleName, T plugin) {
        this.plugin = plugin;
        this.moduleName = moduleName;

        logger = LogManager.getLogger(plugin.getName() + "] [" + moduleName);
    }

    public void setupConfig(ConfigurationSection config) {
        this.config = config;
    }

    public final void enable(){
        if(!config.getBoolean("enabled") || enabled) return;

        boolean inited = false;
        try{
            inited = init();
        } catch (Exception ex) {
            error("Unable to init module!!! (exception)");
            ex.printStackTrace();
        }

        if(inited) {
            Bukkit.getServer().getPluginManager().registerEvents(this, this.plugin);
            log("Enabled module");
            enabled = true;
        }
        else {
            error("Unable to init module!!!");
        }
    }

    public final void disable(){
        disable(false);
    }

    private void disable(boolean doReload) {
        if(!enabled) return;

        try {
            deinit(doReload);
        } catch (Exception ex) {
            error("Unable to disable " + moduleName + " properly");
            ex.printStackTrace();
            return;
        }

        HandlerList.unregisterAll(this);
        log("Disabled module");
        enabled = false;
    }

    public final void reload(){
        if(!enabled) return;
        disable(true);
        enable();
    }

    protected abstract boolean init();
    protected abstract void deinit(boolean doReload);

    protected void log(String message) {
        logger.info(message);
    }

    protected void warn(String message) {
        logger.warn(message);
    }

    protected void error(String message) {
        logger.error(message);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ConfigurationSection config() {
        return config;
    }
}
