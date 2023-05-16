package net.remodded.repaperutils;

import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {
    public static FileConfiguration config;

    public static void setupConfig(List<PluginModule<?>> modules) {
        config = RePaperUtils.INSTANCE.getConfig();

        for (PluginModule<?> module : modules) {
            String path = module.moduleName.substring(0, 1).toLowerCase() + module.moduleName.substring(1);
            config.addDefault(path + ".enabled", false);
            module.setupConfig(config.getConfigurationSection(path));
        }

        config.options().copyDefaults(true);
        save();
    }

    public static void save() {
        RePaperUtils.INSTANCE.saveConfig();
    }
}
