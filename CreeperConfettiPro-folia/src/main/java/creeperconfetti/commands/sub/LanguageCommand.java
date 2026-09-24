package creeperconfetti.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import creeperconfetti.commands.BaseSubCommand;
import creeperconfetti.util.Schedulers;
import org.bukkit.command.CommandSender;

/**
 * {@code /cc language <info|list|set|reload>} - manages the plugin language.
 */
public class LanguageCommand extends BaseSubCommand {

    private static final List<String> ACTIONS = Arrays.asList("info", "list", "set", "reload");

    @Override
    public String getName() {
        return "language";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("lang");
    }

    @Override
    public String getPermission() {
        return "creeperconfetti.command.language";
    }

    @Override
    public String getDescriptionKey() {
        return "help.sub.language";
    }

    @Override
    public String getUsageKey() {
        return "language.usage";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            showInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list":
                listLanguages(sender);
                return true;
            case "set":
                setLanguage(sender, args);
                return true;
            case "reload":
                reloadLanguage(sender);
                return true;
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void showInfo(CommandSender sender) {
        send(sender, "language.info", lang().getCurrentLanguageDisplayName(), lang().getCurrentLanguage());
    }

    private void listLanguages(CommandSender sender) {
        send(sender, "language.list_header");
        for (String code : lang().getAvailableLanguages()) {
            String displayName = lang().getLanguageDisplayNameFor(code);
            if (code.equalsIgnoreCase(lang().getCurrentLanguage())) {
                send(sender, "language.list_current", code, displayName);
            } else {
                send(sender, "language.list_entry", code, displayName);
            }
        }
    }

    private void setLanguage(CommandSender sender, String[] args) {
        if (args.length < 2) {
            send(sender, "language.set_usage");
            return;
        }

        String code = args[1].toLowerCase(Locale.ROOT);
        if (!lang().getAvailableLanguages().contains(code)) {
            send(sender, "language.invalid", args[1]);
            return;
        }

        lang().setLanguage(code, null);
        send(sender, "language.set_success", lang().getCurrentLanguageDisplayName());
    }

    private void reloadLanguage(CommandSender sender) {
        send(sender, "language.reloading");
        lang().reloadLanguage(() -> Schedulers.runGlobal(plugin(), () ->
                send(sender, "language.reloaded", lang().getCurrentLanguageDisplayName())));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filter(ACTIONS, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return filter(new ArrayList<>(lang().getAvailableLanguages()), args[1]);
        }
        return Collections.emptyList();
    }

    @Override
    public void sendHelp(CommandSender sender) {
        send(sender, "help.sub.language");
        send(sender, "help.detail.language.info");
        send(sender, "help.detail.language.list");
        send(sender, "help.detail.language.set");
        send(sender, "help.detail.language.reload");
    }
}
