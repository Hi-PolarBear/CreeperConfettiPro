package creeperconfetti.commands;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import creeperconfetti.CreeperConfettiPro;
import creeperconfetti.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

public class CreeperConfettiCommand implements TabExecutor {

    private static final List<FireworkEffect> DEFAULT_CONFETTI_EFFECT = Collections.emptyList();
    private final LanguageManager languageManager = CreeperConfettiPro.getInstance().getLanguageManager();

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("creeperconfetti.command")) {
            sender.sendMessage(languageManager.getMessage("command.no_permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(languageManager.getMessage("command.usage"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "reseteffect":
                handleResetEffect(sender);
                break;
            case "seteffect":
                handleSetEffect(sender);
                break;
            case "reloadlanguage":
                handleReloadLanguage(sender);
                break;
            default:
                sender.sendMessage(languageManager.getMessage("command.usage"));
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        CreeperConfettiPro.getInstance().reloadConfig();
        sender.sendMessage(languageManager.getMessage("command.reload_success"));
    }

    private void handleResetEffect(CommandSender sender) {
        CreeperConfettiPro.getInstance().getConfig().set("confetti_effect", DEFAULT_CONFETTI_EFFECT);
        CreeperConfettiPro.getInstance().saveConfig();
        sender.sendMessage(languageManager.getMessage("command.reset_success"));
    }

    private void handleSetEffect(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.getMessage("command.player_only"));
            return;
        }

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

        if (itemInMainHand.getType() != Material.FIREWORK_ROCKET) {
            sender.sendMessage(languageManager.getMessage("command.hold_firework"));
            return;
        }

        FireworkMeta fireworkMeta = (FireworkMeta) itemInMainHand.getItemMeta();
        if (fireworkMeta == null || !fireworkMeta.hasEffects()) {
            sender.sendMessage(languageManager.getMessage("command.hold_firework"));
            return;
        }

        CreeperConfettiPro.getInstance().getConfig().set("confetti_effect", fireworkMeta.getEffects());
        CreeperConfettiPro.getInstance().saveConfig();
        sender.sendMessage(languageManager.getMessage("command.effect_set"));

        showFireworkEffect(player);
    }

    private void showFireworkEffect(Player player) {
        Firework firework = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);

        FireworkMeta showcaseFireworkMeta = firework.getFireworkMeta();
        List<FireworkEffect> effects = (List<FireworkEffect>) CreeperConfettiPro.getInstance()
                .getConfig().get("confetti_effect");

        if (effects != null && !effects.isEmpty()) {
            showcaseFireworkMeta.addEffects(effects);
        } else {
            showcaseFireworkMeta.addEffects(DEFAULT_CONFETTI_EFFECT);
        }

        showcaseFireworkMeta.setPower(0);
        firework.setFireworkMeta(showcaseFireworkMeta);

        Objects.requireNonNull(firework);
        Bukkit.getScheduler().runTaskLater(
                CreeperConfettiPro.getInstance(),
                firework::detonate,
                1L
        );
    }

    private void handleReloadLanguage(CommandSender sender) {
        sender.sendMessage("§e正在重新检测语言设置...");

        CreeperConfettiPro.getInstance().getLanguageManager().reloadLanguage(() -> {
            Bukkit.getScheduler().runTask(CreeperConfettiPro.getInstance(), () -> {
                sender.sendMessage("§a语言设置已重新加载！当前语言: " +
                    CreeperConfettiPro.getInstance().getLanguageManager().getCurrentLanguage());
            });
        });
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "reseteffect", "seteffect", "reloadlanguage");
        }
        return Collections.emptyList();
    }
}
