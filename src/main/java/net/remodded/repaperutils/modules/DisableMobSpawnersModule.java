package net.remodded.repaperutils.modules;

import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class DisableMobSpawnersModule extends PluginModule<RePaperUtils> {
	public DisableMobSpawnersModule(RePaperUtils plugin) {
		super("DisableMobSpawners", plugin);
	}

	@Override
	protected boolean init() {
		return true;
	}

    @Override
    protected void deinit(boolean doReload) {}

    @EventHandler
	public void mobSpawnerEvent(SpawnerSpawnEvent ev) {
		ev.setCancelled(true);
	}
}
