package net.remodded.repaperutils.modules;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.CommandsUtils;
import net.remodded.repaperutils.utils.PluginModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RenewableChestsModule extends PluginModule<RePaperUtils> {
    private static File dataFile;
    private static FileConfiguration data;

    private int checkTaskID;
    private Command command;

    public RenewableChestsModule(RePaperUtils plugin) {
        super("RenewableChests", plugin);
    }

    @Override
    protected boolean init() {
        initConfig();
        initCommand();

        checkTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::checkForRenew, 1, 200);
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {
        Bukkit.getScheduler().cancelTask(checkTaskID);
        CommandsUtils.unregister(command);
        if(!doReload)
            CommandsUtils.updateCommandDispatcher();
    }

    private void initConfig() {
        ConfigurationSerialization.registerClass(TreasureItem.class, "RenewableItem");
        dataFile = new File(plugin.getDataFolder(), "data/" + moduleName + ".yml");
        data = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection chests = data.getConfigurationSection("chests");
        if (chests == null) {
            data.createSection("chests");
        }
        saveData();
    }

    private void initCommand() {
        command = CommandsUtils.register(plugin,
            literal("renewablechests")
                .requires(CommandsUtils.getPermissionRequirements(plugin, "renewablechests"))
                .then(literal("add")
                        .then(literal("item")
                                .then(argument("Chest name", StringArgumentType.word())
                                        .suggests(TreasureChestType.TYPE)
                                        .then(argument("min", IntegerArgumentType.integer(1, 64))
                                                .then(argument("max", IntegerArgumentType.integer(1, 64))
                                                        .then(argument("chance", FloatArgumentType.floatArg(0.01f, 100))
                                                                .executes(this::addItem)
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("chest")
                                .then(argument("Chest name", StringArgumentType.word())
                                        .then(argument("pos", BlockPosArgument.blockPos())
                                                .executes(this::addChest)
                                        )
                                )
                        )
                )
                .then(literal("set")
                        .then(literal("time")
                                .then(argument("Chest name", StringArgumentType.word())
                                        .suggests(TreasureChestType.TYPE)
                                        .then(argument("hour", IntegerArgumentType.integer(0, 23))
                                                .then(argument("minute", IntegerArgumentType.integer(0, 59))
                                                        .then(argument("second", IntegerArgumentType.integer(0, 59))
                                                                .executes(this::setTime)
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("renew")
                        .then(argument("Chest name", StringArgumentType.word())
                                .suggests(TreasureChestType.TYPE)
                                .executes(this::forceRenew)
                        )
                )
        );
    }


    private static void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            RePaperUtils.error("Could not save config to " + dataFile);
            ex.printStackTrace();
        }
    }

    private void checkForRenew() {
        ConfigurationSection chests = data.getConfigurationSection("chests");
        Set<String> chestKeys = chests.getKeys(false);
        chestKeys.forEach(key -> {
            ConfigurationSection chest = chests.getConfigurationSection(key);

            LocalTime resetTime = LocalTime.parse(chest.getString("resetTime"));
            LocalTime currentTime = LocalTime.now();
            if (currentTime.isAfter(resetTime) && !chest.getBoolean("isRenewedToday")) {
                renewChest(key);
            } else if (currentTime.isBefore(resetTime) && chest.getBoolean("isRenewedToday")) {
                chest.set("isRenewedToday", false);
                saveData();
            }
        });
    }

    private void renewChest(String chestName) {
        ConfigurationSection chestData = data.getConfigurationSection("chests").getConfigurationSection(chestName);
        List<TreasureItem> treasureItems = (List<TreasureItem>) chestData.getList("treasure", new ArrayList<TreasureItem>());

        World world = Bukkit.getWorld("world");
        BlockState state = world.getBlockAt(chestData.getInt("x"), chestData.getInt("y"), chestData.getInt("z")).getState();
        if (state instanceof Chest chest) {
            Inventory chestInv = chest.getBlockInventory();

            chestInv.clear();
            chestInv.addItem(generateRandomLoot(treasureItems));
        } else {
            chestData.set("isRenewedToday", true);
            saveData();
            throw new IllegalArgumentException("Block mentioned in chest" + chestName + " is not a chest!");
        }
        chestData.set("isRenewedToday", true);
        saveData();
        log("Successfully renewed chest named " + chestName);
    }

    private ItemStack @NotNull [] generateRandomLoot(@NotNull List<TreasureItem> treasureList) {
        ArrayList<ItemStack> items = new ArrayList<>();
        treasureList.forEach(treasureItem -> {
            if (Math.random() * 100 > treasureItem.chance())
                return;
            ItemStack item = treasureItem.item().clone();

            item.setAmount(getRandomBetween(treasureItem.min(), treasureItem.max()));
            items.add(item);
        });
        return items.toArray(new ItemStack[]{});
    }

    public int getRandomBetween(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    private void createRenewableChest(String name, int x, int y, int z) {
        ConfigurationSection newChest = data.getConfigurationSection("chests").createSection(name);
        newChest.set("resetTime", "23:59:59");
        newChest.set("isRenewedToday", false);
        newChest.set("x", x);
        newChest.set("y", y);
        newChest.set("z", z);
        newChest.set("treasure", new ArrayList<TreasureItem>());

        saveData();
    }

    private int addItem(@NotNull CommandContext<CommandSourceStack> ctx) {
        Entity ent = ctx.getSource().getBukkitEntity();
        String chestName = StringArgumentType.getString(ctx, "Chest name");

        if (!data.getConfigurationSection("chests").getKeys(false).contains(chestName)) {
            ent.sendMessage(Component.text("Chest with provided name doesn't exist!"));
            return 1;
        }

        ArrayList<TreasureItem> treasures = (ArrayList<TreasureItem>) data.getConfigurationSection("chests")
                .getConfigurationSection(chestName)
                .getList("treasure", new ArrayList<TreasureItem>());

        treasures.add(new TreasureItem(
                ((Player) ent).getInventory().getItemInMainHand(),
                NumberConversions.toInt(ctx.getArgument("min", Integer.class)),
                NumberConversions.toInt(ctx.getArgument("max", Integer.class)),
                NumberConversions.toFloat(ctx.getArgument("chance", Float.class))
        ));
        saveData();
        ent.sendMessage(Component.text("Successfully added item to \"" + chestName + "\""));
        return 0;
    }

    private int addChest(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity ent = ctx.getSource().getBukkitEntity();
        String name = StringArgumentType.getString(ctx, "Chest name");

        if (data.getConfigurationSection("chests").getKeys(false).stream().anyMatch(str -> str.equalsIgnoreCase(name))) {
            ent.sendMessage(Component.text("Chest with provided name already exists!"));
            return 1;
        }

        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        if (!(Bukkit.getWorld("world").getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getState() instanceof Chest)) {
            ent.sendMessage(Component.text("Selected block is not a chest"));
            return 1;
        }

        createRenewableChest(name, pos.getX(), pos.getY(), pos.getZ());
        ent.sendMessage(Component.text("added successfully"));
        return 0;
    }

    private int setTime(@NotNull CommandContext<CommandSourceStack> ctx) {
        Entity ent = ctx.getSource().getBukkitEntity();
        String name = StringArgumentType.getString(ctx, "Chest name");

        if (!data.getConfigurationSection("chests").getKeys(false).contains(name)) {
            ent.sendMessage(Component.text("Chest with provided name doesn't exist!"));
            return 1;
        }

        String hour = String.valueOf(IntegerArgumentType.getInteger(ctx, "hour"));
        String minute = String.valueOf(IntegerArgumentType.getInteger(ctx,"minute"));
        String second = String.valueOf(IntegerArgumentType.getInteger(ctx,"second"));
        String time = "";
        time += (hour.length() <= 1 ? "0" + hour : hour) + ":" + (minute.length() <= 1 ? "0" + minute : minute) + ":" + (second.length() <= 1 ? "0" + second : second);

        if (!time.matches("\\d*:\\d*:\\d*")) {
            ent.sendMessage(Component.text("Provided reset time is incorrect"));
            return 1;
        }

        data.getConfigurationSection("chests").getConfigurationSection(name).set("resetTime", time);
        saveData();
        ent.sendMessage(Component.text("Time set successfully"));

        return 0;
    }

    private int forceRenew(@NotNull CommandContext<CommandSourceStack> ctx) {
        Entity ent = ctx.getSource().getBukkitEntity();

        String name = StringArgumentType.getString(ctx,"Chest name");
        if (!data.getConfigurationSection("chests").getKeys(false).contains(name)) {
            ent.sendMessage("Chest with provided name doesn't exist!");
            return 1;
        }

        renewChest(name);
        ent.sendMessage(Component.text("Forcerenewed chest successfully"));

        return 0;
    }

    public record TreasureItem(ItemStack item, int min, int max, float chance) implements ConfigurationSerializable {

        @Override
        public @NotNull Map<String, Object> serialize() {
            Map<String, Object> ser = new HashMap<>();
            ser.put("item", item);
            ser.put("min", min);
            ser.put("max", max);
            ser.put("chance", chance);
            return ser;
        }

        @Contract("_ -> new")
        @SuppressWarnings("unused")
        public static @NotNull TreasureItem deserialize(@NotNull Map<String, Object> obj) {
            return new TreasureItem(
                    (ItemStack) obj.get("item"),
                    NumberConversions.toInt(obj.get("min")),
                    NumberConversions.toInt(obj.get("max")),
                    NumberConversions.toFloat(obj.get("chance"))
            );
        }
    }

    public static class TreasureChestType implements SuggestionProvider<CommandSourceStack> {

        public static TreasureChestType TYPE = new TreasureChestType();

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) {
            return SharedSuggestionProvider.suggest(data.getConfigurationSection("chests").getKeys(false).toArray(new String[]{}), suggestionsBuilder);
        }
    }
}
