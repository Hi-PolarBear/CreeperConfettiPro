package creeperconfetti.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import creeperconfetti.commands.BaseSubCommand;
import creeperconfetti.commands.CreeperConfettiCommand;
import creeperconfetti.commands.SubCommand;
import creeperconfetti.util.MessageUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

/**
 * {@code /cc help [page|command]} - paginated help screen. The page buttons are
 * clickable in chat so players can browse without typing commands.
 */
public class HelpCommand extends BaseSubCommand {

    private static final int LINES_PER_PAGE = 8;

    private final CreeperConfettiCommand dispatcher;

    public HelpCommand(CreeperConfettiCommand dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("?", "h");
    }

    @Override
    public String getDescriptionKey() {
        return "help.sub.help";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendPage(sender, 1);
            return true;
        }

        if (isNumber(args[0])) {
            sendPage(sender, (int) parseNumber(args[0]));
            return true;
        }

        SubCommand subCommand = dispatcher.find(args[0]);
        if (subCommand == null) {
            send(sender, "command.unknown_subcommand", args[0]);
            sendPage(sender, 1);
            return true;
        }
        if (!sender.hasPermission(subCommand.getPermission())) {
            send(sender, "command.no_permission");
            return true;
        }
        subCommand.sendHelp(sender);
        return true;
    }

    @Override
    public void sendHelp(CommandSender sender) {
        sendPage(sender, 1);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return java.util.Collections.emptyList();
        }
        List<String> completions = new ArrayList<>();
        for (SubCommand subCommand : dispatcher.getSubCommands()) {
            if (sender.hasPermission(subCommand.getPermission()) && subCommand.getName().startsWith(args[0].toLowerCase(Locale.ROOT))) {
                completions.add(subCommand.getName());
            }
        }
        return completions;
    }

    private void sendPage(CommandSender sender, int page) {
        List<HelpEntry> entries = collect(sender);
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) LINES_PER_PAGE));
        int currentPage = Math.min(Math.max(page, 1), totalPages);

        send(sender, "help.header");

        int start = (currentPage - 1) * LINES_PER_PAGE;
        int end = Math.min(start + LINES_PER_PAGE, entries.size());
        for (int index = start; index < end; index++) {
            send(sender, entries.get(index).key);
        }

        sendPagination(sender, currentPage, totalPages);
    }

    private void sendPagination(CommandSender sender, int page, int totalPages) {
        if (!(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(MessageUtil.format(lang().getMessage("help.pagination_console"), page, totalPages));
            return;
        }

        TextComponent line = new TextComponent("");
        line.addExtra(MessageUtil.plain(lang().getMessage("help.separator")));

        if (page > 1) {
            line.addExtra(MessageUtil.component(lang().getMessage("help.prev"), "/cc help " + (page - 1), lang().getMessage("help.prev_hover")));
        } else {
            line.addExtra(MessageUtil.plain(lang().getMessage("help.prev_disabled")));
        }

        line.addExtra(MessageUtil.plain(lang().getMessage("help.separator")));
        line.addExtra(MessageUtil.plain(MessageUtil.format(lang().getMessage("help.page_info"), page, totalPages)));
        line.addExtra(MessageUtil.plain(lang().getMessage("help.separator")));

        for (int number = 1; number <= totalPages; number++) {
            if (number > 1) {
                line.addExtra(MessageUtil.plain(" "));
            }
            if (number == page) {
                line.addExtra(MessageUtil.plain(MessageUtil.format(lang().getMessage("help.page_current"), number)));
            } else {
                line.addExtra(MessageUtil.component(
                        MessageUtil.format(lang().getMessage("help.page_button"), number),
                        "/cc help " + number,
                        MessageUtil.format(lang().getMessage("help.page_hover"), number)));
            }
        }

        line.addExtra(MessageUtil.plain(lang().getMessage("help.separator")));
        if (page < totalPages) {
            line.addExtra(MessageUtil.component(lang().getMessage("help.next"), "/cc help " + (page + 1), lang().getMessage("help.next_hover")));
        } else {
            line.addExtra(MessageUtil.plain(lang().getMessage("help.next_disabled")));
        }

        MessageUtil.send(sender, line);
    }

    private List<HelpEntry> collect(CommandSender sender) {
        List<HelpEntry> entries = new ArrayList<>();
        add(entries, sender, "help.sub.help", CreeperConfettiCommand.BASE_PERMISSION);

        add(entries, sender, "help.sub.effect", "creeperconfetti.command.effect");
        add(entries, sender, "help.detail.effect.list", "creeperconfetti.command.effect");
        add(entries, sender, "help.detail.effect.add", "creeperconfetti.command.effect");
        add(entries, sender, "help.detail.effect.set", "creeperconfetti.command.effect");
        add(entries, sender, "help.detail.effect.remove", "creeperconfetti.command.effect");
        add(entries, sender, "help.detail.effect.reset", "creeperconfetti.command.effect");
        add(entries, sender, "help.detail.effect.preview", "creeperconfetti.command.effect");
        add(entries, sender, "help.detail.effect.chance", "creeperconfetti.command.effect");
        add(entries, sender, "help.sub.gui", "creeperconfetti.command.effect");

        add(entries, sender, "help.sub.chance", "creeperconfetti.command.chance");
        add(entries, sender, "help.detail.chance.set", "creeperconfetti.command.chance");

        add(entries, sender, "help.sub.language", "creeperconfetti.command.language");
        add(entries, sender, "help.detail.language.info", "creeperconfetti.command.language");
        add(entries, sender, "help.detail.language.list", "creeperconfetti.command.language");
        add(entries, sender, "help.detail.language.set", "creeperconfetti.command.language");
        add(entries, sender, "help.detail.language.reload", "creeperconfetti.command.language");

        add(entries, sender, "help.sub.reload", "creeperconfetti.command.reload");
        add(entries, sender, "help.footer", CreeperConfettiCommand.BASE_PERMISSION);
        return entries;
    }

    private void add(List<HelpEntry> entries, CommandSender sender, String key, String permission) {
        if (sender.hasPermission(permission)) {
            entries.add(new HelpEntry(key));
        }
    }

    private boolean isNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (char character : value.toCharArray()) {
            if (!Character.isDigit(character)) {
                return false;
            }
        }
        return true;
    }

    private long parseNumber(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 1L;
        }
    }

    private static final class HelpEntry {
        private final String key;

        private HelpEntry(String key) {
            this.key = key;
        }
    }
}
