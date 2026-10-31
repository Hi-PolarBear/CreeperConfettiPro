package creeperconfetti.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import creeperconfetti.CreeperConfettiPro;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

/**
 * Registers and unregisters the optional command aliases ({@code /cc} and
 * {@code /confetti}) at runtime.
 *
 * <p>Bukkit only registers aliases declared in {@code plugin.yml} and offers no
 * API to toggle them afterwards, so no aliases are declared there. They are
 * registered through the server's {@link CommandMap} instead.
 *
 * <p>The command map is obtained reflectively because {@code Server#getCommandMap()}
 * is not part of the 1.9.4 API this plugin is compiled against, although every
 * modern server (Spigot / Paper / Folia) provides it. Unregistering uses
 * {@code SimpleCommandMap#getKnownCommands()} when available and falls back to
 * the private {@code knownCommands} field on very old servers.
 *
 * <p>If an alias is already taken by another plugin it is skipped with a warning,
 * which is exactly the scenario this feature exists for.
 */
public class CommandAliasManager {

    /** Aliases that can be toggled in config.yml under {@code command-aliases}. */
    public static final List<String> SUPPORTED_ALIASES =
            Collections.unmodifiableList(Arrays.asList("cc", "confetti"));

    private static final String FALLBACK_PREFIX = "creeperconfetti";
    private static final String CONFIG_ROOT = "command-aliases";

    private final CreeperConfettiPro plugin;
    private final Map<String, AliasCommand> registered = new LinkedHashMap<>();

    public CommandAliasManager(CreeperConfettiPro plugin) {
        this.plugin = plugin;
    }

    /**
     * Re-reads the configuration and (re)registers the enabled aliases.
     * Safe to call from {@code /cc reload}.
     */
    public synchronized void sync() {
        unregisterAll();

        if (!plugin.getConfig().getBoolean(CONFIG_ROOT + ".enabled", true)) {
            if (plugin.isDebug()) {
                plugin.getLogger().info("[Alias] Command aliases are disabled in config.yml.");
            }
            return;
        }

        Command target = plugin.getCommand("creeperconfetti");
        if (target == null) {
            plugin.getLogger().warning("[Alias] Main command is not registered, skipping aliases.");
            return;
        }

        CommandMap commandMap = resolveCommandMap();
        if (commandMap == null) {
            plugin.getLogger().warning("[Alias] Unable to access the server CommandMap, skipping aliases.");
            return;
        }

        for (String alias : SUPPORTED_ALIASES) {
            if (!plugin.getConfig().getBoolean(CONFIG_ROOT + "." + alias, true)) {
                if (plugin.isDebug()) {
                    plugin.getLogger().info("[Alias] /" + alias + " is disabled in config.yml.");
                }
                continue;
            }

            Command existing = commandMap.getCommand(alias);
            if (existing != null && !(existing instanceof AliasCommand)) {
                plugin.getLogger().warning("[Alias] /" + alias
                        + " is already registered by another plugin - skipped. Disable it in config.yml"
                        + " (command-aliases." + alias + ") if you want to hide this warning.");
                continue;
            }

            AliasCommand command = new AliasCommand(target, alias);
            boolean primaryName = commandMap.register(FALLBACK_PREFIX, command);
            registered.put(alias, command);

            if (!primaryName) {
                plugin.getLogger().warning("[Alias] /" + alias + " could not be registered as a plain name.");
            } else if (plugin.isDebug()) {
                plugin.getLogger().info("[Alias] Registered /" + alias + ".");
            }
        }
    }

    /**
     * Removes every alias this manager registered.
     */
    public synchronized void unregisterAll() {
        if (registered.isEmpty()) {
            return;
        }

        CommandMap commandMap = resolveCommandMap();
        if (commandMap != null) {
            Map<String, Command> known = resolveKnownCommands(commandMap);
            if (known != null) {
                for (AliasCommand command : registered.values()) {
                    known.values().removeIf(value -> value == command);
                }
            }
        }
        registered.clear();
    }

    /**
     * @return {@code true} when the given alias is currently registered by this plugin.
     */
    public synchronized boolean isRegistered(String alias) {
        return registered.containsKey(alias);
    }

    // ------------------------------------------------------------------ reflection

    private static CommandMap resolveCommandMap() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("getCommandMap");
            Object value = method.invoke(Bukkit.getServer());
            return value instanceof CommandMap ? (CommandMap) value : null;
        } catch (Exception exception) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Command> resolveKnownCommands(CommandMap commandMap) {
        try {
            Method method = commandMap.getClass().getMethod("getKnownCommands");
            Object value = method.invoke(commandMap);
            if (value instanceof Map<?, ?>) {
                return (Map<String, Command>) value;
            }
        } catch (Exception ignored) {
            // older servers do not expose a getter - fall through to the field
        }
        try {
            Field field = commandMap.getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);
            Object value = field.get(commandMap);
            if (value instanceof Map<?, ?>) {
                return (Map<String, Command>) value;
            }
        } catch (Exception ignored) {
            // give up silently, aliases simply stay registered
        }
        return null;
    }

    // ------------------------------------------------------------------ alias command

    /**
     * Thin command that forwards everything to the real {@code /creeperconfetti}
     * command, so aliases behave exactly like the main command (including the
     * localized permission message and tab completion).
     */
    private static final class AliasCommand extends Command {

        private final Command target;

        private AliasCommand(Command target, String name) {
            super(name);
            this.target = target;
            if (target.getDescription() != null) {
                setDescription(target.getDescription());
            }
            if (target.getUsage() != null) {
                setUsage(target.getUsage());
            }
            // No permission is set on purpose: the main command checks it itself
            // and sends a localized "no permission" message.
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            return target.execute(sender, commandLabel, args);
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            return target.tabComplete(sender, alias, args);
        }
    }
}
