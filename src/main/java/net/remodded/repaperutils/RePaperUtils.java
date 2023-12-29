package net.remodded.repaperutils;

import net.remodded.repaperutils.commands.RePaperUtilsCommand;
import net.remodded.repaperutils.modules.*;
import net.remodded.repaperutils.utils.PluginModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class RePaperUtils extends JavaPlugin {

    public static final String ID = "repaperutils";
    public static final String NAME = "RePaperUtils";

    public static RePaperUtils INSTANCE;

    public final static List<PluginModule<?>> modules = new ArrayList<>();

    @Override
    public void onEnable() {
        INSTANCE = this;
        
        registerModules();
        
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
        for (PluginModule<?> module : modules)
            module.reload();

        log(NAME + " reloaded!");
    }
    
    private void registerModules() {
        modules.add(new BlockPvPEscapeModule(this));
        modules.add(new DisableMobSpawnersModule(this));
        modules.add(new DisablePortalsModule(this));
        modules.add(new InvulnerabilityModule(this));
        modules.add(new MobSpawnSwitchModule(this));
        modules.add(new RenewableChestsModule(this));
        modules.add(new StartupCommandsModule(this));
        modules.add(new TextCommandsModule(this));
        modules.add(new RestartModule(this));
        modules.add(new VoucherModule(this));
        modules.add(new HardcoreModule(this));
        modules.add(new TimedEffectModule(this));
        modules.add(new BlocksCommandsModule(this));
        modules.add(new EntityBlacklistModule(this));
        modules.add(new PotionsBlacklistModule(this));
        modules.add(new WitherBuildBlockerModule(this));
        modules.add(new EnchantmentsBlacklistModule(this));
    }

    private static final Logger logger = LogManager.getLogger(NAME);
    public static void log(String log) { logger.info(log); }
    public static void warn(String log) { logger.warn(log); }
    public static void error(String log) { logger.error(log); }
}
