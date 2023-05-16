package net.remodded.repaperutils.modules;

import com.google.common.collect.ImmutableList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.CommandsUtils;
import net.remodded.repaperutils.utils.PluginModule;

public class StartupCommandsModule extends PluginModule<RePaperUtils> {

    public StartupCommandsModule(RePaperUtils plugin) {
        super("StartupCommands", plugin);
    }

    @Override
    public void setupConfig(ConfigurationSection config) {
        super.setupConfig(config);

        config.addDefault("delay", 200);
        config.addDefault("commands", ImmutableList.of("say Serwer Wystartował"));
    }

    @Override
    protected boolean init() {

        new BukkitRunnable() {
            @Override
            public void run() {
                log("Executing startup commands");
                CommandsUtils.executeCommands(config.getStringList("commands"));
            }
        }.runTaskLater(plugin, config.getLong("delay", 200));

        return true;
    }

    @Override
    protected void deinit(boolean doReload) {}
}
