package creeperconfetti.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import creeperconfetti.CreeperConfettiPro;
import creeperconfetti.LanguageManager;
import org.bukkit.command.CommandSender;

/**
 * Convenience base class for subcommands.
 */
public abstract class BaseSubCommand implements SubCommand {

    protected CreeperConfettiPro plugin() {
        return CreeperConfettiPro.getInstance();
    }

    protected LanguageManager lang() {
        return plugin().getLanguageManager();
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getPermission() {
        return "creeperconfetti.command";
    }

    @Override
    public String getUsageKey() {
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        send(sender, getDescriptionKey());
        if (getUsageKey() != null) {
            send(sender, getUsageKey());
        }
    }

    protected void send(CommandSender sender, String key) {
        sender.sendMessage(lang().getMessage(key));
    }

    protected void send(CommandSender sender, String key, Object... args) {
        sender.sendMessage(format(lang().getMessage(key), args));
    }

    protected static String format(String message, Object... args) {
        return creeperconfetti.util.MessageUtil.format(message, args);
    }

    /**
     * Filters the given options by the already typed prefix (case-insensitive).
     */
    protected static List<String> filter(List<String> options, String prefix) {
        String lower = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
