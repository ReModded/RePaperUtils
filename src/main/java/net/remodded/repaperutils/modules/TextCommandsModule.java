package net.remodded.repaperutils.modules;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.CommandsUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TextCommandsModule extends PluginModule<RePaperUtils> {
    private final List<Command> registeredCommands = new ArrayList<>();

    public TextCommandsModule(RePaperUtils plugin) {
        super("TextCommands", plugin);
    }

    public boolean init() {
        ConfigurationSection commands = config.getConfigurationSection("commands");
        for (String commandName : commands.getKeys(false)) {
            Command command = new TextCommand(commandName, commands.getStringList(commandName));
            if(CommandsUtils.register(plugin, command))
                registeredCommands.add(command);
        }
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {
        for (Command command : registeredCommands)
            CommandsUtils.unregister(command);

        if(!doReload)
            CommandsUtils.updateCommandDispatcher();
    }

    @Override
    public void setupConfig(ConfigurationSection config) {
        ConfigurationSection commands = config.getConfigurationSection("commands");
        if(commands == null || commands.getKeys(false).isEmpty()) {
            config.addDefault("commands.pomoc", ImmutableList.of("&8### &a&1POMOC &8### "));
        }
    }

    private static class TextCommand extends Command {
        private final List<String> lines;

        public TextCommand(String name, List<String> lines){
            super(name);
            this.lines = lines;
            setPermission(CommandsUtils.createCommandPermission(RePaperUtils.INSTANCE, "textCommands." + name));
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
            for (String line : lines)
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            return true;
        }
    }
}
