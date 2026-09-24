package creeperconfetti.commands.sub;

import java.util.Arrays;
import java.util.List;

import creeperconfetti.commands.BaseSubCommand;
import creeperconfetti.gui.StyleListGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /cc gui} - opens the graphical firework style manager.
 */
public class GuiCommand extends BaseSubCommand {

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("menu");
    }

    @Override
    public String getPermission() {
        return "creeperconfetti.command.effect";
    }

    @Override
    public String getDescriptionKey() {
        return "help.sub.gui";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "command.player_only");
            return true;
        }
        new StyleListGui(plugin(), (Player) sender, 1).open();
        return true;
    }
}
