package creeperconfetti.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * A single top-level subcommand of {@code /creeperconfetti}.
 */
public interface SubCommand {

    /**
     * Primary name of the subcommand.
     */
    String getName();

    /**
     * Alternative names for the subcommand.
     */
    List<String> getAliases();

    /**
     * Permission required to run the subcommand.
     */
    String getPermission();

    /**
     * Translation key of the short help line shown in the command list.
     */
    String getDescriptionKey();

    /**
     * Translation key of the usage line, may be {@code null}.
     */
    String getUsageKey();

    /**
     * Executes the subcommand. {@code args} excludes the subcommand name itself.
     *
     * @return {@code true} when the command was handled.
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * Tab completion for the subcommand. {@code args} excludes the subcommand name.
     */
    List<String> tabComplete(CommandSender sender, String[] args);

    /**
     * Sends detailed help for the subcommand.
     */
    void sendHelp(CommandSender sender);
}
