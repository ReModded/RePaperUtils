package net.remodded.repaperutils.modules;

import com.google.common.collect.ImmutableList;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.GameRule;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MobSpawnSwitchModule extends PluginModule<RePaperUtils> {
    public MobSpawnSwitchModule(RePaperUtils plugin) {
        super("MobSpawnSwitch", plugin);
    }

    public boolean init() {
        int offCount = config.getInt("offPlayerCount");
        if(offCount <= config.getInt("onPlayerCount")){
            config.set("onPlayerCount", offCount - 1);
            plugin.saveConfig();

            warn("AutoMobSpawnSwitch: offPlayerCount has to be grater then onPlayerCount!!!");
        }

        setGameRule(offCount > 0, false);

        return true;
    }

    @Override
    protected void deinit(boolean doReload) {}

    @Override
    public void setupConfig(ConfigurationSection config) {
        config.addDefault("worlds", ImmutableList.of("world"));
        config.addDefault("onPlayerCount", 40);
        config.addDefault("offPlayerCount", 60);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        Server server = plugin.getServer();
        if(config.getInt("offPlayerCount") == server.getOnlinePlayers().size())
            setGameRule(false);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent ev) {
        Server server = plugin.getServer();
        if(config.getInt("onPlayerCount") == server.getOnlinePlayers().size() - 1)
            setGameRule(true);
    }

    private void setGameRule(boolean value) {
        setGameRule(value, true);
    }

    private void setGameRule(boolean value, boolean doLog) {
        config.getStringList("worlds").forEach(worldName -> {
            World world = plugin.getServer().getWorld(worldName);
            if(world == null) return;
            world.setGameRule(GameRule.DO_MOB_SPAWNING, value);
        });

        if(doLog)
            log((value ? "Enabling" : "Disabling") + " mob spawn.");
    }
}
