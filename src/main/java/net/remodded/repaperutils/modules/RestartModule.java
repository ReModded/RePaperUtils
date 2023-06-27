package net.remodded.repaperutils.modules;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.CommandsUtils;
import net.remodded.repaperutils.utils.PluginModule;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.RestartCommand;

import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RestartModule extends PluginModule<RePaperUtils> {

    private Command command;

    private BossBar bossBar;

    private int restartTask;

    public RestartModule(RePaperUtils plugin) {
        super("Restart", plugin);
    }

    @Override
    public void setupConfig(ConfigurationSection config) {
        config.addDefault("defaultDelay", 300);
        config.addDefault("bossbarMessage", "&b&eRestart za &2{}");
    }

    @Override
    protected boolean init() {
        initCommand();
        return true;
    }

    @Override
    protected void deinit(boolean doReload) {
        if(!doReload)
            abortRestart();

        CommandsUtils.unregister(command);
        if(!doReload)
            CommandsUtils.updateCommandDispatcher();
    }

    @EventHandler
    private void onPlayerJoined(PlayerJoinEvent ev) {
        if (bossBar != null) {
            bossBar.addPlayer(ev.getPlayer());
        }
    }

    private void initCommand() {
        command = CommandsUtils.register(plugin,
            literal("restart")
                .requires(CommandsUtils.getPermissionRequirements("restart"))
                .then(literal("--abort")
                    .executes(ctx -> {
                        int r = abortRestart();
                        ctx.getSource().getBukkitSender().sendMessage(Component.text("Restart serwera został przerwany.", NamedTextColor.GREEN));
                        return r;
                    }))
                .then(argument("delay", IntegerArgumentType.integer(0))
                    .executes(ctx -> {
                        int delay = IntegerArgumentType.getInteger(ctx, "delay");
                        ctx.getSource().getBukkitSender().sendMessage(Component.text("Restart serwera za " + delay + "s.", NamedTextColor.GREEN));
                        return restart(delay);
                    }))
                .executes(ctx -> {
                    int delay = config.getInt("defaultDelay");
                    ctx.getSource().getBukkitSender().sendMessage(Component.text("Restart serwera za " + delay + "s.", NamedTextColor.GREEN));
                    return restart(delay);
                })
        );
    }

    private int restart(int delay) {
        AtomicInteger timeLeft = new AtomicInteger(delay);

        bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);

        restartTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            int left = timeLeft.decrementAndGet();

            if(left < 0) {
                RestartCommand.restart();
                return;
            }

            int min = left / 60;
            int sec = left % 60;

            StringBuilder message = new StringBuilder();
            if(min > 0)
                message.append(min).append("min ");

            message.append(sec).append("s");

            bossBar.setTitle(this.config.getString("bossbarMessage").replace("{}", message.toString()).replace('&', '§'));
            bossBar.setProgress((double)left / delay);
        }, 1, 20);

        return 1;
    }

    private int abortRestart() {
        if(bossBar == null) {
            return 0;
        }

        Bukkit.getScheduler().cancelTask(restartTask);
        bossBar.removeAll();
        bossBar = null;
        return 1;
    }
}
