package net.remodded.repaperutils.modules;

import me.NoChance.PvPManager.PvPManager;
import me.angeschossen.lands.api.events.player.area.PlayerAreaEnterEvent;
import me.angeschossen.lands.api.flags.Flags;
import me.angeschossen.lands.api.player.LandPlayer;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import net.remodded.repaperutils.RePaperUtils;

public class BlockPvPEscapeModule extends PluginModule<RePaperUtils> {
    public BlockPvPEscapeModule(RePaperUtils plugin) {
        super("BlockPvPEscape", plugin);
    }

    private PvPManager pvpManager;

    @Override
    protected boolean init() {
        pvpManager = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
        if(pvpManager == null) {
            error("PvPManager not enabled!!!");
            return false;
        }

        if(Bukkit.getPluginManager().getPlugin("Lands") == null)
        {
            error("Lands not enabled!!!");
            return false;
        }

        return true;
    }

    @Override
    protected void deinit(boolean doReload) {}

    @EventHandler
    public void onPlayerEnter(PlayerAreaEnterEvent ev) {
        LandPlayer player = ev.getLandPlayer();
        if(ev.getArea().hasRoleFlag(player, Flags.LAND_ENTER, null, false) && pvpManager.getPlayerHandler().get(player.getPlayer()).isInCombat())
            ev.setCancelled(true);
    }
}
