package creeperconfetti.commands.sub;

import creeperconfetti.commands.BaseSubCommand;
import creeperconfetti.util.Schedulers;
import org.bukkit.command.CommandSender;

/**
 * {@code /cc reload} - reloads the configuration and the firework styles.
 */
public class ReloadCommand extends BaseSubCommand {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public java.util.List<String> getAliases() {
        return java.util.Arrays.asList("rl");
    }

    @Override
    public String getPermission() {
        return "creeperconfetti.command.reload";
    }

    @Override
    public String getDescriptionKey() {
        return "help.sub.reload";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin().reloadConfig();
        plugin().reloadCachedSettings();
        plugin().getFireworkStyleManager().load();
        plugin().getLanguageManager().reloadMessages();
        // Folia: command (un)registration must not happen on a region thread.
        Schedulers.runGlobal(plugin(), () -> plugin().getCommandAliasManager().sync());
        send(sender, "command.reload_success");
        return true;
    }
}
