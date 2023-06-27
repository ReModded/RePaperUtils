package net.remodded.repaperutils.modules;

import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.CommandsUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlocksCommandsModule extends PluginModule<RePaperUtils> {

    private final ArrayList<BlockAction> actionList = new ArrayList<>();

    public BlocksCommandsModule(RePaperUtils plugin) {
        super("BlocksCommandsModule", plugin);
    }

    @Override
    protected boolean init() {
        this.loadData();
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {}

    @Override
    public void setupConfig(ConfigurationSection config) {
        ConfigurationSection actions = config.getConfigurationSection("actions");
        if(actions == null || actions.getKeys(false).isEmpty()) {
            config.addDefault("actions.crafting.block", "crafting_table");
            config.addDefault("actions.crafting.cancel", true);
            config.addDefault("actions.crafting.shift", false);
            config.addDefault("actions.crafting.commands", List.of("say otwarto crafting"));
        }
    }

    private void loadData() {
        actionList.clear();
        ConfigurationSection actions = config.getConfigurationSection("actions");
        actions.getKeys(false).forEach(key -> {
            try {
                ConfigurationSection node = actions.getConfigurationSection(key);
                String blockName = node.getString("block");
                Material block = Material.valueOf(blockName.toUpperCase(Locale.ROOT));

                boolean cancel = node.getBoolean("cancel", true);
                boolean needShift = node.getBoolean("shift", false);
                List<String> cmd = node.getStringList("commands");
                actionList.add(new BlockAction(block, cancel, needShift, cmd));
                log("Adding handler for " + block + " wiht " + cmd.size() + " commands.");
            } catch (Exception ex) {
                error("Problem with loading " + key + " BlockAction");
            }
        });
        log("Added " + actionList.size() + " handlers!");
    }

    @EventHandler
    public void onPlayerAction(@NotNull PlayerInteractEvent ev) {
        if (ev.useInteractedBlock().equals(Event.Result.DENY))
            return;

        if (!ev.getAction().isRightClick())
            return;

        actionList.forEach(action -> {
            if (action.needShift && !ev.getPlayer().isSneaking())
                return;

            if (!action.block.equals(ev.getClickedBlock().getType()))
                return;

            if(action.cancel)
                ev.setCancelled(true);

            CommandsUtils.executeCommandsOp(ev.getPlayer(), action.commands);
        });
    }

    record BlockAction(Material block, boolean cancel, boolean needShift, List<String> commands) {}
}
