package creeperconfetti.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import creeperconfetti.CreeperConfettiPro;
import creeperconfetti.LanguageManager;
import creeperconfetti.commands.sub.ChanceCommand;
import creeperconfetti.commands.sub.EffectCommand;
import creeperconfetti.commands.sub.GuiCommand;
import creeperconfetti.commands.sub.HelpCommand;
import creeperconfetti.commands.sub.LanguageCommand;
import creeperconfetti.commands.sub.ReloadCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * Root command dispatcher. Every feature is exposed through its own subcommand
 * (help / effect / chance / language / reload) instead of a flat command list.
 */
public class CreeperConfettiCommand implements TabExecutor {

    public static final String BASE_PERMISSION = "creeperconfetti.command";

    private final List<SubCommand> subCommands = new ArrayList<>();
    private final HelpCommand helpCommand;

    public CreeperConfettiCommand() {
        this.helpCommand = new HelpCommand(this);
        register(helpCommand);
        register(new EffectCommand());
        register(new GuiCommand());
        register(new ChanceCommand());
        register(new LanguageCommand());
        register(new ReloadCommand());
    }

    public void register(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    public List<SubCommand> getSubCommands() {
        return Collections.unmodifiableList(subCommands);
    }

    /**
     * Finds a subcommand by its name or alias.
     */
    public SubCommand find(String name) {
        if (name == null) {
            return null;
        }
        for (SubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(name)) {
                return subCommand;
            }
            for (String alias : subCommand.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return subCommand;
                }
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        LanguageManager languageManager = CreeperConfettiPro.getInstance().getLanguageManager();

        if (!sender.hasPermission(BASE_PERMISSION)) {
            sender.sendMessage(languageManager.getMessage("command.no_permission"));
            return true;
        }

        if (args.length == 0) {
            helpCommand.execute(sender, new String[0]);
            return true;
        }

        SubCommand subCommand = find(args[0]);
        if (subCommand == null) {
            sender.sendMessage(BaseSubCommand.format(
                    languageManager.getMessage("command.unknown_subcommand"), args[0]));
            helpCommand.execute(sender, new String[0]);
            return true;
        }

        if (!sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(languageManager.getMessage("command.no_permission"));
            return true;
        }

        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, rest);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission(BASE_PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length <= 1) {
            String prefix = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
            List<String> completions = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                if (sender.hasPermission(subCommand.getPermission()) && subCommand.getName().startsWith(prefix)) {
                    completions.add(subCommand.getName());
                }
            }
            return completions;
        }

        SubCommand subCommand = find(args[0]);
        if (subCommand == null || !sender.hasPermission(subCommand.getPermission())) {
            return Collections.emptyList();
        }

        return subCommand.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
    }
}
