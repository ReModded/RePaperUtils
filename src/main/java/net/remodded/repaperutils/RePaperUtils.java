package net.remodded.repaperutils;

import com.google.common.collect.ImmutableList;
import net.remodded.repaperutils.commands.RePaperUtilsCommand;
import net.remodded.repaperutils.modules.*;
import net.remodded.repaperutils.utils.PluginModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class RePaperUtils extends JavaPlugin {

    public static final String ID = "repaperutils";
    public static final String NAME = "RePaperUtils";

    public static RePaperUtils INSTANCE;

    public final List<PluginModule<?>> modules = ImmutableList.of(
        new BlockPvPEscapeModule(this),
        new DisableMobSpawnersModule(this),
        new DisablePortalsModule(this),
        new InvulnerabilityModule(this),
        new MobSpawnSwitchModule(this),
        new RenewableChestsModule(this),
        new StartupCommandsModule(this),
        new TextCommandsModule(this),
        new RestartModule(this),
        new VoucherModule(this),
        new HardcoreModule(this),
        new TimedEffectModule(this),
        new BlocksCommandsModule(this),
        new WitherBuildBlockerModule(this)
    );

    @Override
    public void onEnable() {
        INSTANCE = this;
        Config.setupConfig(modules);
        RePaperUtilsCommand.register();

        for (PluginModule<?> module : modules)
            module.enable();

        log(NAME + " loaded successfully!");
    }

    @Override
    public void onDisable() {
        for (PluginModule<?> module : modules)
            module.disable();

        log(NAME + " disabled!");
    }

    public void reload() {
        warn("Reloading " + NAME + "!");
        reloadConfig();
        Config.setupConfig(modules);
        for (PluginModule<?> module : modules)
            module.reload();

        log(NAME + " reloaded!");
    }

    private static final Logger logger = LogManager.getLogger(NAME);
    public static void log(String log) { logger.info(log); }
    public static void warn(String log) { logger.warn(log); }
    public static void error(String log) { logger.error(log); }
}
