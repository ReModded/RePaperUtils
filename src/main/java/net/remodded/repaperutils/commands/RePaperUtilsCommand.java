package net.remodded.repaperutils.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.remodded.repaperutils.Config;
import net.remodded.repaperutils.commands.arguments.ModuleArgumentType;
import net.remodded.repaperutils.utils.PluginModule;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.CommandsUtils;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RePaperUtilsCommand {

    public static void register() {
        LiteralCommandNode<CommandSourceStack> command =
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
                ))
        .build();

        CommandsUtils.register(RePaperUtils.INSTANCE, command);
        CommandsUtils.register(RePaperUtils.INSTANCE, literal("reu").redirect(command));
    }


    private static int reloadAll(CommandContext<CommandSourceStack> ctx) {
        RePaperUtils.INSTANCE.reload();
        ctx.getSource().sendSuccess(Component.literal("Przeładowano plugin"), false);
        CommandsUtils.updateCommandDispatcher();
        return 0;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PluginModule<?> module = ModuleArgumentType.getEnabledModule(ctx, "module");
        module.reload();
        CommandsUtils.updateCommandDispatcher();
        ctx.getSource().sendSuccess(Component.literal("Przeładowano moduł " + module.moduleName).withStyle(s -> s.withColor(ChatFormatting.GREEN)), false);
        return 0;
    }


    private static int enable(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PluginModule<?> module = ModuleArgumentType.getDisabledModule(ctx, "module");
        module.config().set("enabled", true);
        Config.save();
        module.enable();
        CommandsUtils.updateCommandDispatcher();
        ctx.getSource().sendSuccess(Component.literal("Włączono moduł " + module.moduleName).withStyle(s -> s.withColor(ChatFormatting.GREEN)), false);
        return 0;
    }

    private static int disable(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PluginModule<?> module = ModuleArgumentType.getEnabledModule(ctx, "module");
        module.disable();
        module.config().set("enabled", false);
        Config.save();
        ctx.getSource().sendSuccess(Component.literal("Wyłączono moduł " + module.moduleName).withStyle(s -> s.withColor(ChatFormatting.GREEN)), false);
        return 0;
    }
}
