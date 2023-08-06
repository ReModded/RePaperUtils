package net.remodded.repaperutils.modules;

import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;

import java.util.Random;

public class HardcoreModule extends PluginModule<RePaperUtils> {
    private final Random random = new Random();

    private boolean blockAnimalBreading;
    private boolean blockChickenEggSpawn;
    
    private boolean slowCropsGrowth;
    private boolean blockPlantGrowthWithoutSky;
    
    public HardcoreModule(RePaperUtils plugin) {
        super("Hardcore", plugin);
    }

    @Override
    protected boolean init() {
        blockAnimalBreading = config.getBoolean("blockAnimalBreading", true);
        blockChickenEggSpawn = config.getBoolean("blockChickenEggSpawn", true);
        
        slowCropsGrowth = config.getBoolean("slowCropsGrowth", true);
        blockPlantGrowthWithoutSky = config.getBoolean("blockPlantGrowthWithoutSky", true); 
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {}

    @Override
    public void setupConfig(ConfigurationSection config) {
        config.addDefault("blockAnimalBreading", true);
        config.addDefault("blockChickenEggSpawn", true);
        
        config.addDefault("slowCropsGrowth", true);
        config.addDefault("blockPlantGrowthWithoutSky", true);
    }

    @EventHandler
    private void onAnimalBreed(EntityEnterLoveModeEvent ev) {
        if (blockAnimalBreading)
            ev.setCancelled(true);
    }

    @EventHandler
    private void onChickenSpawn(CreatureSpawnEvent ev) {
        if(blockChickenEggSpawn && ev.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG)
            ev.setCancelled(true);
    }

    @EventHandler
    private void onPlantGrow(BlockGrowEvent ev) {
        Block block = ev.getBlock();

        // Sky blocking
        if(blockPlantGrowthWithoutSky && block.getLightFromSky() < 15) {
            ev.setCancelled(true);
            return;
        }

        // Slow grow speed x4
        if(slowCropsGrowth && random.nextFloat() > 0.25f)
            ev.setCancelled(true);
    }
}
