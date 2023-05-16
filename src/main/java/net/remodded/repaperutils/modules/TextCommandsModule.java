package net.remodded.repaperutils.modules;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.CommandsUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextCommandsModule extends PluginModule<RePaperUtils> {

    private static FileConfiguration commandsConfig;

    private final List<Command> registeredCommands = new ArrayList<>();

    public TextCommandsModule(RePaperUtils plugin) {
        super("TextCommands", plugin);
    }

    public boolean init() {
        for (String commandName : commandsConfig.getKeys(false)) {
            Command command = new TextCommand(commandName, commandsConfig.getStringList(commandName));
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
        super.setupConfig(config);

        File configFile = new File(RePaperUtils.INSTANCE.getDataFolder(), "textCommands.yml");
        commandsConfig = YamlConfiguration.loadConfiguration(configFile);

        // Default commandsConfig
        commandsConfig.addDefault("pomoc", ImmutableList.of("&8### &a&1POMOC &8### "));

        commandsConfig.options().copyDefaults(true);
        try {
            commandsConfig.save(configFile);
        } catch (IOException ex) {
            error("Could not save config to " + configFile);
            ex.printStackTrace();
        }
    }

    private static class TextCommand extends Command {
        private final List<String> lines;

        public TextCommand(String name, List<String> lines){
            super(name);
            this.lines = lines;
            setPermission(CommandsUtils.createCommandPermission("textCommands." + name));
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            for (String line : lines)
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            return true;
        }
    }
}
