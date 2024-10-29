package net.remodded.repaperutils.utils;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.ApiMirrorRootNode;
import io.papermc.paper.command.brigadier.ShadowBrigNode;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.remodded.repaperutils.RePaperUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class CommandsUtils {

    private record Cmd(Plugin plugin, LiteralCommandNode<CommandSourceStack> command, List<String> aliases) {}

    private static List<Cmd> commands = new ArrayList<>();

    private static final Pattern COMMAND_EXECUTOR_PATTERN = Pattern.compile("@s");

    static {
        RePaperUtils.INSTANCE.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, ev -> {
//            var dispatcher = ((ApiMirrorRootNode)ev.registrar().getDispatcher().getRoot()).getDispatcher();
            for (Cmd cmd : commands) {
                ev.registrar().getDispatcher().getRoot().addChild(new ShadowBrigNode(cmd.command));
//                dispatcher.register(cmd.command.createBuilder());
            }
        });
    }

    public static void executeCommand(CommandSender commandSender, String command) {
        Bukkit.dispatchCommand(commandSender, command);
    }

    public static void executeCommand(String command) {
        executeCommand(Bukkit.getConsoleSender(), command);
    }

    public static void executeCommands(CommandSender commandSender, List<String> commands) {
        for (String command : commands)
            executeCommand(commandSender, command);
    }

    public static void executeCommands(List<String> commands) {
        for (String command : commands)
            executeCommand(command);
    }

    public static void executeCommandOp(Player player, String command) {
        executeCommand(Bukkit.getConsoleSender(), COMMAND_EXECUTOR_PATTERN.matcher(command).replaceAll(player.getName()));
    }

    public static void executeCommandsOp(Player player, List<String> commands) {
        for (String command : commands)
            executeCommandOp(player, command);
    }

    public static boolean register(Plugin plugin, Command command) {
        return Bukkit.getCommandMap().register(plugin.getName(), command);
    }

    public static Command register(final Plugin plugin, final LiteralArgumentBuilder<CommandSourceStack> commandBuilder) {
        return register(plugin, commandBuilder, List.of());
    }

    public static Command register(final Plugin plugin, final LiteralArgumentBuilder<CommandSourceStack> commandBuilder, List<String> aliases) {
        return register(plugin, commandBuilder.build(), aliases);
    }

    public static Command register(final Plugin plugin, final LiteralCommandNode<CommandSourceStack> command) {
        return register(plugin, command, List.of());
    }

    public static Command register(final Plugin plugin, final LiteralCommandNode<CommandSourceStack> command, List<String> aliases) {
        commands.add(new Cmd(plugin, command, aliases));
//        VanillaCommandWrapper wrapper = new VanillaCommandWrapper(command.getName(), null, null, aliases, command);
//        wrapper.setPermission(getPermission(plugin, command));
//        Bukkit.getCommandMap().register(plugin.getName(), wrapper);
        return null;
    }

    public static void unregister(Command command) {
        if(command == null) return;

        CommandMap commandMap = Bukkit.getCommandMap();
        command.unregister(commandMap);

        Map<String, Command> commands = commandMap.getKnownCommands();
        commands.entrySet().stream().filter(p -> p.getValue() == command).toList().forEach(pair -> {
            commands.remove(pair.getKey());
            if(command instanceof VanillaCommandWrapper)
                MinecraftServer.getServer().getCommands().getDispatcher().getRoot().removeCommand(pair.getKey());
        });
    }

    public static void updateCommandDispatcher() {
       ((CraftServer)Bukkit.getServer()).syncCommands();
   }

    public static String createCommandPermission(Plugin plugin, final String permission) {
        return plugin.getName().toLowerCase() + "." + permission;
   }

    public static Predicate<CommandSourceStack> getPermissionRequirements(Plugin plugin, String permission) {
        return src -> src.getBukkitSender().hasPermission(CommandsUtils.createCommandPermission(plugin, permission));
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
