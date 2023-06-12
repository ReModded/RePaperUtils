package net.remodded.repaperutils.modules;

import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.HeightMap;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;

import java.util.Random;

public class HardcoreModule extends PluginModule<RePaperUtils> {
    private final Random random = new Random();

    public HardcoreModule(RePaperUtils plugin) {
        super("Hardcore", plugin);
    }

    @Override
    protected boolean init() {
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {}

    @EventHandler
    private void onAnimalBreed(EntityEnterLoveModeEvent ev) {
        ev.setCancelled(true);
    }

    @EventHandler
    private void onChickenSpawn(CreatureSpawnEvent ev) {
        if(ev.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG)
            ev.setCancelled(true);
    }

    @EventHandler
    private void onPlantGrow(BlockGrowEvent ev) {
        Block block = ev.getBlock();
        World world = block.getWorld();

        // Sky blocking
        Block topBlock = world.getHighestBlockAt(block.getLocation(), HeightMap.OCEAN_FLOOR);
        if(topBlock.getLightFromSky() != 15){
            ev.setCancelled(true);
            return;
        }

        // Slow grow speed x4
        if(random.nextFloat() > 0.25f)
            ev.setCancelled(true);
    }
}
