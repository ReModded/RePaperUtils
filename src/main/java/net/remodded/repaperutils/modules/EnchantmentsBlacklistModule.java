package net.remodded.repaperutils.modules;

import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantmentsBlacklistModule extends PluginModule<RePaperUtils> {

    private Map<Enchantment, Enchantment> replacementList;

    public EnchantmentsBlacklistModule(RePaperUtils plugin) {
        super("EnchantmentsBlacklist", plugin);
    }

    @Override
    protected boolean init() {
        replacementList = new HashMap<>();

        Map<String, String> replacements = (Map<String, String>) (Object) config.getConfigurationSection("enchantments").getValues(false);

        for (var entry : replacements.entrySet()) {
            Enchantment fromEnchant = Enchantment.getByKey(NamespacedKey.fromString(entry.getKey()));
            Enchantment toEnchant = Enchantment.getByKey(NamespacedKey.fromString(entry.getValue()));

            if (fromEnchant == null || toEnchant == null) {
               error("Unable to map " + entry.getKey() + " to " + entry.getValue() + " enchantments." );
               continue;
            }

            replacementList.put(fromEnchant, toEnchant);
        }

        return true;
    }

    @Override
    protected void deinit(boolean doReload) {
        replacementList = null;
    }

    @Override
    public void setupConfig(ConfigurationSection config) {
        config.addDefault("enchantments", Map.of("minecraft:sharpness", "minecraft:efficiency"));
    }


    @EventHandler
    private void onEnchant(PrepareItemEnchantEvent ev) {
        EnchantmentOffer[] offers = ev.getOffers();

        for (EnchantmentOffer offer : offers) {
            if (offer == null)
                continue;

            Enchantment orginalEnchantment = offer.getEnchantment();
            Enchantment currentEnchantment = replacementList.getOrDefault(orginalEnchantment, orginalEnchantment);

            if (currentEnchantment != orginalEnchantment) {
                offer.setEnchantment(currentEnchantment);
            }
        }
    }

    @EventHandler
    private void onEnchant(EnchantItemEvent ev) {
        Map<Enchantment, Integer> enchantments =
                ev.getEnchantsToAdd().entrySet().stream().map(entry -> {
                    if(replacementList.containsKey(entry.getKey()))
                        return Map.entry(replacementList.get(entry.getKey()), entry.getValue());
                    return entry;
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        ev.getEnchantsToAdd().clear();
        ev.getEnchantsToAdd().putAll(enchantments);
    }
}