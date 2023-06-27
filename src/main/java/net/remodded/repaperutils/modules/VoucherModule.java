package net.remodded.repaperutils.modules;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.CommandsUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class VoucherModule extends PluginModule<RePaperUtils> {

    private static File dataFile;
    private static FileConfiguration data;

    private Command command;

    public VoucherModule(RePaperUtils plugin) {
        super("Vouchers", plugin);
    }

    public boolean init() {
        dataFile = new File(plugin.getDataFolder(), "data/" + moduleName + ".yml");
        data = YamlConfiguration.loadConfiguration(dataFile);
        saveData();
        command = new VoucherCommand();
        CommandsUtils.register(plugin, command);
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {
        CommandsUtils.unregister(command);

        if(!doReload)
            CommandsUtils.updateCommandDispatcher();
    }

    private static void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            RePaperUtils.error("Could not save config to " + dataFile);
            ex.printStackTrace();
        }
    }

    private static class VoucherCommand extends Command {

        private VoucherCommand() {
            super("voucher");
            setPermission(CommandsUtils.createCommandPermission("voucher"));
            setUsage("/voucher <kod>");
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
            if (!sender.hasPermission(getPermission() + ".add"))
                return ImmutableList.of();
            if (args.length == 1)
                return ImmutableList.of("add");
            return ImmutableList.of();
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Komenda może zostać wykonana jedynie przez gracza!", NamedTextColor.RED));
                return false;
            }

            if (args.length == 0) {
                sender.sendMessage(Component.text("Podaj kod!", NamedTextColor.RED));
                sender.sendMessage(Component.text("/voucher <kod>", NamedTextColor.RED));
                return false;
            }

            if (args[0].equalsIgnoreCase("add") && sender.hasPermission(getPermission() + ".add")) {
                if (args.length < 2 || args[1].isEmpty()) {
                    sender.sendMessage(Component.text("Brakuje Kodu!", NamedTextColor.RED));
                    sender.sendMessage(Component.text("/voucher add <kod>", NamedTextColor.RED));
                    return false;
                }

                if (args.length > 2) {
                    sender.sendMessage(Component.text("Podano za dużo argumentów.", NamedTextColor.RED));
                    sender.sendMessage(Component.text("/voucher add <kod>", NamedTextColor.RED));
                    return false;
                }

                String code = args[1].toLowerCase();

                if (code.equals("add")) {
                    sender.sendMessage(Component.text("Kod nie może być \"add\"", NamedTextColor.RED));
                    return false;
                }

                if (data.contains(code)) {
                    sender.sendMessage(Component.text("Taki kod jest już w istnieje!", NamedTextColor.RED));
                    return false;
                }

                ItemStack stack = player.getInventory().getItemInMainHand();

                if (stack.getType() == Material.AIR || stack.getAmount() < 1) {
                    sender.sendMessage(Component.text("Musisz trzymać jakiś przedmiot!", NamedTextColor.RED));
                    return false;
                }

                data.set(code, stack);
                saveData();

                sender.sendMessage(
                        Component.text("Dodano nowy Voucher!")
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to copy code.")))
                );
                return true;
            }

            if (args.length > 1) {
                sender.sendMessage(Component.text("Podano za dużo argumentów.", NamedTextColor.RED));
                return false;
            }

            String code = args[0].toLowerCase();
            ItemStack stack = data.getItemStack(code, null);

            if (stack == null) {
                sender.sendMessage(Component.text("Nie ma takiego kodu lub został już wykorzystany", NamedTextColor.RED));
                return false;
            }

            data.set(code, null);
            saveData();

            HashMap<Integer, ItemStack> toDrop = player.getInventory().addItem(stack);

            World world = player.getWorld();
            for (ItemStack drop : toDrop.values())
                world.dropItem(player.getLocation(), drop);

            sender.sendMessage(Component.text("Wykorzystano Voucher!!!", NamedTextColor.GREEN));

            return true;
        }
    }
}
