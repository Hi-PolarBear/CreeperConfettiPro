package creeperconfetti.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import creeperconfetti.commands.BaseSubCommand;
import creeperconfetti.effects.FireworkStyleManager;
import creeperconfetti.effects.FireworkUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /cc effect <list|add|set|remove|reset|preview|chance>} - manages the
 * available firework styles. Every style may hold one or more effects, has its
 * own trigger chance and a weighted-random style is picked whenever a confetti
 * creeper explodes.
 */
public class EffectCommand extends BaseSubCommand {

    private static final List<String> ACTIONS = Arrays.asList("list", "add", "set", "remove", "reset", "preview", "chance");

    @Override
    public String getName() {
        return "effect";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("effects", "style", "styles", "firework");
    }

    @Override
    public String getPermission() {
        return "creeperconfetti.command.effect";
    }

    @Override
    public String getDescriptionKey() {
        return "help.sub.effect";
    }

    @Override
    public String getUsageKey() {
        return "effect.usage";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list":
                return list(sender);
            case "add":
                return add(sender, args);
            case "set":
                return set(sender, args);
            case "remove":
            case "delete":
                return remove(sender, args);
            case "reset":
                return reset(sender);
            case "preview":
            case "test":
                return preview(sender, args);
            case "chance":
            case "chances":
                return chance(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean list(CommandSender sender) {
        FireworkStyleManager manager = plugin().getFireworkStyleManager();
        List<String> names = manager.getStyleNames();
        if (names.isEmpty()) {
            send(sender, "effect.list_empty");
            return true;
        }

        send(sender, "effect.list_header", names.size());
        int index = 1;
        for (String name : names) {
            send(sender, "effect.list_entry", index++, name, manager.getStyle(name).size(), formatNumber(manager.getChance(name)));
        }
        send(sender, "effect.random_info");
        return true;
    }

    private boolean add(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "command.player_only");
            return true;
        }

        Player player = (Player) sender;
        List<FireworkEffect> effects = FireworkUtil.getEffectsFromItem(player.getInventory().getItemInMainHand());
        if (effects.isEmpty()) {
            send(sender, "command.hold_firework");
            return true;
        }

        FireworkStyleManager manager = plugin().getFireworkStyleManager();
        String name = args.length >= 2 ? args[1] : manager.nextAutoName();
        if (manager.hasStyle(name)) {
            send(sender, "effect.add_exists", name);
            return true;
        }

        manager.addStyle(name, effects);
        manager.save();
        send(sender, "effect.add_success", name);
        FireworkUtil.spawnAndDetonate(plugin(), player.getLocation().add(0, 1, 0), effects, 0);
        return true;
    }

    private boolean set(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "command.player_only");
            return true;
        }
        if (args.length < 2) {
            send(sender, "effect.name_required", "set");
            return true;
        }

        Player player = (Player) sender;
        List<FireworkEffect> effects = FireworkUtil.getEffectsFromItem(player.getInventory().getItemInMainHand());
        if (effects.isEmpty()) {
            send(sender, "command.hold_firework");
            return true;
        }

        FireworkStyleManager manager = plugin().getFireworkStyleManager();
        manager.addStyle(args[1], effects);
        manager.save();
        send(sender, "effect.set_success", args[1]);
        FireworkUtil.spawnAndDetonate(plugin(), player.getLocation().add(0, 1, 0), effects, 0);
        return true;
    }

    private boolean remove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            send(sender, "effect.name_required", "remove");
            return true;
        }

        FireworkStyleManager manager = plugin().getFireworkStyleManager();
        if (!manager.removeStyle(args[1])) {
            send(sender, "effect.not_found", args[1]);
            return true;
        }

        manager.save();
        send(sender, "effect.remove_success", args[1]);
        return true;
    }

    private boolean reset(CommandSender sender) {
        plugin().getFireworkStyleManager().resetToDefaults();
        send(sender, "effect.reset_success");
        return true;
    }

    private boolean preview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            send(sender, "command.player_only");
            return true;
        }

        Player player = (Player) sender;
        FireworkStyleManager manager = plugin().getFireworkStyleManager();

        List<FireworkEffect> effects;
        if (args.length >= 2) {
            if (!manager.hasStyle(args[1])) {
                send(sender, "effect.not_found", args[1]);
                return true;
            }
            effects = manager.getStyle(args[1]);
            send(sender, "effect.preview_success", args[1]);
        } else {
            effects = manager.getRandomEffects();
            send(sender, "effect.preview_random");
        }

        FireworkUtil.spawnAndDetonate(plugin(), player.getLocation().add(0, 1, 0), effects, 0);
        return true;
    }

    private boolean chance(CommandSender sender, String[] args) {
        FireworkStyleManager manager = plugin().getFireworkStyleManager();

        if (args.length == 1) {
            listChances(sender, manager);
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("reset")) {
            manager.resetChances();
            manager.save();
            send(sender, "effect.chance_reset");
            listChances(sender, manager);
            return true;
        }

        if (args.length < 3) {
            send(sender, "effect.chance_usage");
            return true;
        }

        String name = args[1];
        if (!manager.hasStyle(name)) {
            send(sender, "effect.not_found", name);
            return true;
        }

        double value;
        try {
            value = Double.parseDouble(args[2]);
        } catch (NumberFormatException exception) {
            send(sender, "effect.chance_invalid");
            return true;
        }
        if (value < 0.0D) {
            send(sender, "effect.chance_invalid");
            return true;
        }

        manager.setChance(name, value);
        manager.save();
        send(sender, "effect.chance_set", name, formatNumber(manager.getChance(name)));
        listChances(sender, manager);
        return true;
    }

    private void listChances(CommandSender sender, FireworkStyleManager manager) {
        send(sender, "effect.chance_header");
        double total = 0.0D;
        for (Map.Entry<String, Double> entry : manager.getChances().entrySet()) {
            total += entry.getValue();
            send(sender, "effect.chance_entry", entry.getKey(), formatNumber(entry.getValue()));
        }
        send(sender, "effect.chance_sum", formatNumber(total));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filter(ACTIONS, args[0]);
        }
        if (args.length == 2 && isStyleAction(args[0])) {
            List<String> names = new ArrayList<>(plugin().getFireworkStyleManager().getStyleNames());
            if (args[0].equalsIgnoreCase("chance")) {
                names.add("reset");
            }
            return filter(names, args[1]);
        }
        return Collections.emptyList();
    }

    private boolean isStyleAction(String action) {
        return action.equalsIgnoreCase("set")
                || action.equalsIgnoreCase("remove")
                || action.equalsIgnoreCase("preview")
                || action.equalsIgnoreCase("chance");
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    public void sendHelp(CommandSender sender) {
        send(sender, "help.sub.effect");
        send(sender, "help.detail.effect.list");
        send(sender, "help.detail.effect.add");
        send(sender, "help.detail.effect.set");
        send(sender, "help.detail.effect.remove");
        send(sender, "help.detail.effect.reset");
        send(sender, "help.detail.effect.preview");
        send(sender, "help.detail.effect.chance");
    }
}
