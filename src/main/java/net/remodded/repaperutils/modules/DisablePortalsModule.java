package net.remodded.repaperutils.modules;

import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;

public class DisablePortalsModule extends PluginModule<RePaperUtils> {
    public DisablePortalsModule(RePaperUtils plugin) {
        super("DisablePortal", plugin);
    }

    @Override
    protected boolean init() {
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {}

    @EventHandler
    public void mobSpawnerEvent(PlayerPortalEvent ev) {
        ev.setCancelled(true);
    }
}
