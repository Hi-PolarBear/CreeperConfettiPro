package creeperconfetti.commands.sub;

import java.util.Arrays;
import java.util.List;

import creeperconfetti.commands.BaseSubCommand;
import org.bukkit.command.CommandSender;

/**
 * {@code /cc chance [percent]} - views or updates the confetti chance.
 */
public class ChanceCommand extends BaseSubCommand {

    private static final String CONFIG_PATH = "confetti_chance";

    @Override
    public String getName() {
        return "chance";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("probability", "percent");
    }

    @Override
    public String getPermission() {
        return "creeperconfetti.command.chance";
    }

    @Override
    public String getDescriptionKey() {
        return "help.sub.chance";
    }

    @Override
    public String getUsageKey() {
        return "chance.usage";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            send(sender, "chance.info", formatNumber(plugin().getConfettiChance()));
            return true;
        }

        double value;
        try {
            value = Double.parseDouble(args[0]);
        } catch (NumberFormatException exception) {
            send(sender, "chance.invalid");
            return true;
        }

        if (value < 0.0D || value > 100.0D) {
            send(sender, "chance.invalid");
            return true;
        }

        plugin().getConfig().set(CONFIG_PATH, value);
        plugin().saveConfig();
        plugin().setConfettiChance(value);
        send(sender, "chance.set", formatNumber(value));
        return true;
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
