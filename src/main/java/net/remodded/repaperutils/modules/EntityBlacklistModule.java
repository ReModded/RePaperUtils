package net.remodded.repaperutils.modules;

import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityBlacklistModule extends PluginModule<RePaperUtils> {

    private final Set<EntityType> blacklist = new HashSet<>();

    public EntityBlacklistModule(RePaperUtils plugin) {
        super("EntityBlacklist", plugin);
    }

    @Override
    protected boolean init() {
        blacklist.clear();
        for (String listItem : config.getStringList("blacklist"))
        {
            NamespacedKey key = NamespacedKey.fromString(listItem);
            blacklist.add(EntityType.fromName(key.getKey()));
        }

        return true;
    }

    @Override
    public void setupConfig(ConfigurationSection config) {
        config.addDefault("blacklist", List.of("minecraft:bat"));
    }

    @Override
    protected void deinit(boolean doReload) {
        blacklist.clear();
    }

    @EventHandler
    private void onEntitySpawn(CreatureSpawnEvent ev) {
        if (ev.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM)
            return;

        if (blacklist.contains(ev.getEntityType()))
            ev.setCancelled(true);
    }

    @EventHandler
    private void onEntityLoad(EntitiesLoadEvent ev) {
        for (Entity entity : ev.getEntities())
            if(blacklist.contains(entity.getType()))
                entity.remove();
    }
}
