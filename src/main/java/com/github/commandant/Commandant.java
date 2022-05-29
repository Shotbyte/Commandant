package com.github.commandant;

import com.github.commandant.command.Command;
import com.github.commandant.command.IdentifiableCommand;
import lombok.experimental.UtilityClass;
import net.jodah.typetools.TypeResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

/**
 * A namespace for registering a {@link Command}.
 */
@UtilityClass
public class Commandant {

    /**
     * @param command
     * @param plugin
     */
    @SuppressWarnings("unchecked")
    public void register(final Command<?> command, final JavaPlugin plugin) {
        final PluginCommand pluginCommand = plugin.getCommand(command.getLabel());
        final Command<CommandSender> castedCommand = (Command<CommandSender>) command;
        final Class<?> senderType = TypeResolver.resolveRawArgument(Command.class, castedCommand.getClass());

        pluginCommand.setExecutor((sender, serverCommand, aliasLabel, args) -> {
            if (senderType.isInstance(sender))
                castedCommand.execute(sender, aliasLabel, args);

            return true;
        });

        pluginCommand.setTabCompleter((sender, serverCommand, alias, args) -> castedCommand.tabComplete(sender, args));
    }

    /**
     * @param command
     * @param map
     */
    public void register(final Command<?> command, final CommandMap map) {
        register(new CommandAdapter(command), map);
    }

    /**
     * @param command
     * @param map
     */
    public void register(final IdentifiableCommand<?> command, final CommandMap map) {
        register(new IdentifiableCommandAdapter(command), map);
    }

    /**
     * @param command
     * @param map
     * @param plugin
     */
    public void register(final IdentifiableCommand<?> command, final CommandMap map, final Plugin plugin) {
        register(new PluginIdentifiableCommandAdapter(command, plugin), map);
    }

    private void register(final CommandAdapter adapter, final CommandMap map) {
        map.register(adapter.getLabel(), adapter);
    }

    /**
     * @return
     */
    public CommandMap getServerCommandMap() {
        try {
            final Field field = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getPluginManager());
        } catch (final IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }
}
