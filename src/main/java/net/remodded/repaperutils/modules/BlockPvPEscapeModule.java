package net.remodded.repaperutils.modules;

import me.NoChance.PvPManager.PvPManager;
import me.angeschossen.lands.api.events.player.PlayerAreaEnterEvent;
import me.angeschossen.lands.api.flags.Flags;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
        Player player = ev.getLandPlayer().getPlayer();
        if(ev.getArea().hasFlag(player, Flags.LAND_ENTER, false) && pvpManager.getPlayerHandler().get(player).isInCombat())
            ev.setCancelled(true);
    }
}
