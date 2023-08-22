package net.remodded.repaperutils.modules;

import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class PotionsBlacklistModule extends PluginModule<RePaperUtils> {

    private List<PotionEffectType> blacklist;
    
    public PotionsBlacklistModule(RePaperUtils plugin) {
        super("PotionsBlacklist", plugin);
    }

    @Override
    protected boolean init() {
        blacklist = new ArrayList<>();
        
        List<String> potions = config.getStringList("potions");

        for (String potionName : potions) {
            PotionEffectType potionEffect = PotionEffectType.getByKey(NamespacedKey.fromString(potionName));
            
            if (potionEffect == null) {
                warn("PotionEffect '" + potionName +"' doesn't exists.");
                continue;
            }
            
            blacklist.add(potionEffect);
        }

        return true;
    }

    @Override
    protected void deinit(boolean doReload) {
        blacklist = null;
    }

    @Override
    public void setupConfig(ConfigurationSection config) {
        config.addDefault("potions", List.of("minecraft:instant_health"));
    }

    @EventHandler
    public void onBrewEvent(BrewEvent event) {
        for (ItemStack stack : event.getResults()) {
            if (stack.getType() != Material.POTION && stack.getType() != Material.SPLASH_POTION && stack.getType() != Material.LINGERING_POTION &&
                !(stack.getItemMeta() instanceof PotionMeta))
                continue;
            
            PotionMeta meta = (PotionMeta)stack.getItemMeta(); 
            
            log("potion: " + meta.getBasePotionData().getType().getEffectType());
        
            if (blacklist.contains(meta.getBasePotionData().getType().getEffectType())) 
                event.setCancelled(true);
        }
    }
}
