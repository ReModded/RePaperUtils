package net.remodded.repaperutils.modules;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.CommandsUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TimedEffectModule extends PluginModule<RePaperUtils> {

    private final List<PotionEffect> effects = new ArrayList<>();

    private static File dataFile;
    private static FileConfiguration data;

    private Command command;
    private BukkitTask task;

    public TimedEffectModule(RePaperUtils plugin) {
        super("TimedEffect", plugin);
    }

    @Override
    protected boolean init() {
        dataFile = new File(plugin.getDataFolder(), moduleName + ".yml");
        data = YamlConfiguration.loadConfiguration(dataFile);
        data.addDefault("effects", new ArrayList<PotionEffect>());
        saveData();
        createCommand();

        effects.clear();
        for (Object effect : data.getList("effects", new ArrayList<PotionEffect>())) {
            effects.add((PotionEffect) effect);
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for(Player player : Bukkit.getOnlinePlayers())
                player.addPotionEffects(effects.stream().map(e -> e.withDuration(100)).toList());

            for(int i = 0; i < effects.size(); i++) {
                PotionEffect effect = effects.get(i);
                if(effect.getDuration() > 1)
                    effects.set(i, effect.withDuration(effect.getDuration() - 1));
                else {
                    effects.remove(i);
                    i--;
                }
            }
        }, 0, 20);
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {
        CommandsUtils.unregister(command);
        task.cancel();

        if (!doReload)
        {
            updateData();
            CommandsUtils.updateCommandDispatcher();
        }
    }

    private void updateData() {
        data.set("effects", effects);
        saveData();
    }

    private static void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            RePaperUtils.error("Could not save config to " + dataFile);
            ex.printStackTrace();
        }
    }

    private void createCommand() {
        if(command != null)
            return;

        CommandBuildContext commandbuildcontext = Commands.createValidationContext(VanillaRegistries.createLookup());
        command = CommandsUtils.register(plugin,
            literal("timedeffect")
                .requires(CommandsUtils.getPermissionRequirements("timedeffect"))
                .then(literal("list")
                    .executes(src -> {
                        var msg = Component.literal("Działające efekty:");

                        for(PotionEffect effect : effects)
                            msg.append("\n\t" + effect.getType().getName());

                        src.getSource().sendSystemMessage(msg);
                        return 1;
                    }))
                .then(literal("add")
                    .then(argument("effect", ResourceArgument.resource(commandbuildcontext, Registries.MOB_EFFECT))
                        .then(argument("time", IntegerArgumentType.integer(1, 1000000))
                            .then(argument("strength", IntegerArgumentType.integer(1, 255))
                                .executes(ctx -> handleTimedEffectAdd(ResourceArgument.getMobEffect(ctx, "effect"), IntegerArgumentType.getInteger(ctx, "time"), IntegerArgumentType.getInteger(ctx, "strength")))
                            )
                            .executes(ctx -> handleTimedEffectAdd(ResourceArgument.getMobEffect(ctx, "effect"), IntegerArgumentType.getInteger(ctx, "time"), 1))
                        )
                    )
                )
        );
    }

    private int handleTimedEffectAdd(Holder.Reference<MobEffect> effect, int time, int strength) {
        try {
            String location = effect.key().location().toString();
            PotionEffectType potionEffectType = PotionEffectType.getByKey(NamespacedKey.fromString(location));
            effects.add(new PotionEffect(potionEffectType, time, strength));
            updateData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }
}
