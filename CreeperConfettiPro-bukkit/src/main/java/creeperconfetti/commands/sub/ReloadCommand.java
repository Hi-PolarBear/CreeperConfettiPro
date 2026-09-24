package creeperconfetti.commands.sub;

import creeperconfetti.commands.BaseSubCommand;
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
        plugin().getFireworkStyleManager().load();
        plugin().getLanguageManager().reloadMessages();
        send(sender, "command.reload_success");
        return true;
    }
}
