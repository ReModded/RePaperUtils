package net.remodded.repaperutils.modules;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentsBlacklistModule extends PluginModule<RePaperUtils> {

    private Map<Enchantment, Enchantment> replacementList;

    private boolean doReplaceExisting = false;

    public EnchantmentsBlacklistModule(RePaperUtils plugin) {
        super("EnchantmentsBlacklist", plugin);
    }

    @Override
    protected boolean init() {
        doReplaceExisting = config.getBoolean("replaceExisting", false);
        replacementList = new HashMap<>();

        Map<String, String> replacements = (Map<String, String>) (Object) config.getConfigurationSection("enchantments").getValues(false);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
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
        config.addDefault("replaceExisting", false);
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
        replaceEnchantments(ev.getEnchantsToAdd());
    }

    @EventHandler
    private void onItemEquip(PlayerArmorChangeEvent ev) {
        if (!doReplaceExisting)
            return;

        ItemStack item = ev.getNewItem();
        if (replaceEnchantmentsOnItem(item))
            ev.getPlayer().getInventory().setItem(EquipmentSlot.valueOf(ev.getSlotType().name()), item);
    }

    @EventHandler
    private void onItemHeld(PlayerItemHeldEvent ev) {
        if (!doReplaceExisting)
            return;

        ItemStack item = ev.getPlayer().getInventory().getItem(ev.getNewSlot());
        if (replaceEnchantmentsOnItem(item))
            ev.getPlayer().getInventory().setItem(ev.getNewSlot(), item);
    }

    private boolean replaceEnchantmentsOnItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isEmpty())
            return false;

        Map<Enchantment, Integer> enchantments = getReplacedEnchantments(item.getEnchantments());
        if (enchantments.isEmpty())
            return false;

        item.getEnchantments().forEach((k, v) -> item.removeEnchantment(k));

        item.addUnsafeEnchantments(enchantments);
        return true;
    }

    private void replaceEnchantments(Map<Enchantment, Integer> originalEnchantments) {
        if (originalEnchantments.isEmpty())
            return;

        Map<Enchantment, Integer> enchantments = getReplacedEnchantments(originalEnchantments);
        originalEnchantments.clear();
        originalEnchantments.putAll(enchantments);
    }

    private Map<Enchantment, Integer> getReplacedEnchantments(Map<Enchantment, Integer> originalEnchantments) {
        if (originalEnchantments.isEmpty())
            return originalEnchantments;

        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for(var entry : originalEnchantments.entrySet()) {
            Enchantment ench = entry.getKey();
            int value = entry.getValue();

            if(replacementList.containsKey(ench))
                ench = replacementList.get(ench);

            if(enchantments.containsKey(ench))
                value += enchantments.remove(ench);

            enchantments.put(ench, value);
        }

        return enchantments;
    }
}
