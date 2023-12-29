package net.remodded.repaperutils.commands.arguments;


import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.remodded.repaperutils.RePaperUtils;
import net.remodded.repaperutils.utils.PluginModule;

import java.util.concurrent.CompletableFuture;

public class ModuleArgumentType implements SuggestionProvider<CommandSourceStack> {

    private static final SimpleCommandExceptionType UNKNOWN_MODULE = new SimpleCommandExceptionType(new LiteralMessage("Nieznany moduł."));
    private static final SimpleCommandExceptionType ENABLED_MODULE = new SimpleCommandExceptionType(new LiteralMessage("Moduł jest włączony."));
    private static final SimpleCommandExceptionType DISABLED_MODULE = new SimpleCommandExceptionType(new LiteralMessage("Moduł jest wyłączony."));

    private final boolean allowEnabled;
    private final boolean allowDisabled;

    private ModuleArgumentType(boolean suggestEnabled, boolean allowDisabled) {
        this.allowEnabled = suggestEnabled;
        this.allowDisabled = allowDisabled;
    }

    public static ModuleArgumentType all() {
        return new ModuleArgumentType(true, true);
    }

    public static ModuleArgumentType enabled() {
        return new ModuleArgumentType(true, false);
    }

    public static ModuleArgumentType disabled() {
        return new ModuleArgumentType(false, true);
    }

    public static PluginModule<?> getModule(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        String moduleName = StringArgumentType.getString(ctx, name);
        return RePaperUtils.modules.stream()
                .filter(customer -> moduleName.equalsIgnoreCase(customer.moduleName))
                .findAny()
                .orElseThrow(UNKNOWN_MODULE::create);
    }

    public static PluginModule<?> getEnabledModule(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        PluginModule<?> module = getModule(ctx, name);

        if(!module.isEnabled())
            throw DISABLED_MODULE.create();

        return module;
    }

    public static PluginModule<?> getDisabledModule(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        PluginModule<?> module = getModule(ctx, name);

        if(module.isEnabled())
            throw ENABLED_MODULE.create();

        return module;
    }

    public static ArgumentType<?> get() {
        return StringArgumentType.word();
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(RePaperUtils.modules.stream()
                .filter(m -> (allowDisabled && !m.isEnabled()) || (allowEnabled && m.isEnabled()))
                .map(m -> m.moduleName)
                .toArray(String[]::new), builder);
    }
}
