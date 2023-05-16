package net.remodded.repaperutils.utils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.remodded.repaperutils.RePaperUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.command.VanillaCommandWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class CommandsUtils {

    private static final Commands commands = new Commands();
    private static CommandDispatcher<CommandSource> dispatcher;

    public static void executeCommands(List<String> commands) {
        for (String command : commands)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public static void executeCommandsOp(Player player, List<String> commands) {
        for (String command : commands)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("@p", player.getName()));
    }

    public static boolean register(Plugin plugin, Command command) {
        return Bukkit.getCommandMap().register(plugin.getName(), command);
    }

    public static Command register(final Plugin plugin, final LiteralArgumentBuilder<CommandSourceStack> commandBuilder) {
        return register(plugin, commandBuilder.build());
    }

    public static Command register(final Plugin plugin, final LiteralCommandNode<CommandSourceStack> command) {
        commands.getDispatcher().getRoot().addChild(command);
        VanillaCommandWrapper wrapper = new VanillaCommandWrapper(commands, command);
        wrapper.setPermission(getPermission(plugin, command));
        Bukkit.getCommandMap().register(plugin.getName(), wrapper);
        return wrapper;
    }

    public static void unregister(Command command) {
        if(command == null) return;

        CommandMap commandMap = Bukkit.getCommandMap();
        command.unregister(commandMap);

        Map<String, Command> commands = commandMap.getKnownCommands();
        commands.entrySet().stream().filter(p -> p.getValue() == command).toList().forEach(pair -> {
            commands.remove(pair.getKey());
            if(command instanceof VanillaCommandWrapper)
                CommandsUtils.commands.getDispatcher().getRoot().removeCommand(pair.getKey());
        });
   }

   public static void updateCommandDispatcher() {
       ((CraftServer)Bukkit.getServer()).syncCommands();
   }

   public static String createCommandPermission(final String permission) {
        return RePaperUtils.ID + "." + permission;
   }

    private static String getPermission(final Plugin plugin, final CommandNode<CommandSourceStack> command) {
        final String commandName;
        if (command.getRedirect() == null) {
            commandName = command.getName();
        } else {
            commandName = command.getRedirect().getName();
        }
        return plugin.getName() + "." + stripDefaultNamespace(plugin, commandName);
    }

    private static String stripDefaultNamespace(final Plugin plugin, final String maybeNamespaced) {
        final String prefix = plugin.getName() + ":";
        if (maybeNamespaced.startsWith(prefix)) {
            return maybeNamespaced.substring(prefix.length());
        }
        return maybeNamespaced;
    }
}