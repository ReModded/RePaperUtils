package net.remodded.repaperutils.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.commands.arguments.ModuleArgumentType;
import net.remodded.repaperutils.utils.CommandsUtils;
import net.remodded.repaperutils.utils.PluginModule;

import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RePaperUtilsCommand {

    public static void register() {
        var command =
            literal(RePaperUtils.ID)
                .then(literal("reload")
                    .executes(RePaperUtilsCommand::reloadAll)
                    .then(argument("module", ModuleArgumentType.get())
                        .suggests(ModuleArgumentType.enabled())
                        .executes(RePaperUtilsCommand::reload)
                    )
                )
                .then(literal("enable")
                    .then(argument("module", ModuleArgumentType.get())
                        .suggests(ModuleArgumentType.disabled())
                        .executes(RePaperUtilsCommand::enable)
                ))
                .then(literal("disable")
                    .then(argument("module", ModuleArgumentType.get())
                        .suggests(ModuleArgumentType.enabled())
                        .executes(RePaperUtilsCommand::disable)
                ));

        CommandsUtils.register(RePaperUtils.INSTANCE, command, List.of("reu"));
    }


    private static int reloadAll(CommandContext<CommandSourceStack> ctx) {
        RePaperUtils.INSTANCE.reload();
        ctx.getSource().sendSuccess(() -> Component.literal("Przeładowano plugin"), false);
        CommandsUtils.updateCommandDispatcher();
        return 0;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PluginModule<?> module = ModuleArgumentType.getEnabledModule(ctx, "module");
        module.reload();

        CommandsUtils.updateCommandDispatcher();
        ctx.getSource().sendSuccess(() -> Component.literal("Przeładowano moduł " + module.moduleName).withStyle(s -> s.withColor(ChatFormatting.GREEN)), false);
        return 0;
    }


    private static int enable(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PluginModule<?> module = ModuleArgumentType.getDisabledModule(ctx, "module");
        module.enable(true);

        if (!module.isEnabled()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Wystąpił problem z włączeniem moduł " + module.moduleName).withStyle(s -> s.withColor(ChatFormatting.RED)), false);
        }
        else {
            CommandsUtils.updateCommandDispatcher();
            ctx.getSource().sendSuccess(() -> Component.literal("Włączono moduł " + module.moduleName).withStyle(s -> s.withColor(ChatFormatting.GREEN)), false);
        }
        return 0;
    }

    private static int disable(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PluginModule<?> module = ModuleArgumentType.getEnabledModule(ctx, "module");
        module.shutdown();

        ctx.getSource().sendSuccess(() -> Component.literal("Wyłączono moduł " + module.moduleName).withStyle(s -> s.withColor(ChatFormatting.GREEN)), false);
        return 0;
    }
}
