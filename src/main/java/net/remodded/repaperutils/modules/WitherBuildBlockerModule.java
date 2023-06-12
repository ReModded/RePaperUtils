package net.remodded.repaperutils.modules;

import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WitherBuildBlockerModule extends PluginModule<RePaperUtils> {
    public WitherBuildBlockerModule(RePaperUtils plugin) {
        super("WitherBuildBlocker", plugin);
    }

    @Override
    protected boolean init() {
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {}

    @EventHandler
    private void onWitherBuild(CreatureSpawnEvent ev) {
        if(ev.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_WITHER)
            ev.setCancelled(true);
    }
}
