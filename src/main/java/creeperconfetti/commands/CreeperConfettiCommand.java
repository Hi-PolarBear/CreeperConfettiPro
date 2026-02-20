package creeperconfetti.commands;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import creeperconfetti.CreeperConfettiPro ;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

  @Override
  public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
    if (!sender.hasPermission("creeperconfetti.command")) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>您没有权限使用此命令！"));
      return true;
    }

    if (args.length == 0) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>用法: /creeperconfetti <reload | reseteffect | seteffect>"));
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
      default:
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>用法: /creeperconfetti <reload | reseteffect | seteffect>"));
        break;
    }

    return true;
  }

  private void handleReload(CommandSender sender) {
    CreeperConfettiPro.getInstance().reloadConfig();
    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>已重新加载CreeperConfettiPro配置！"));
  }

  private void handleResetEffect(CommandSender sender) {
    CreeperConfettiPro.getInstance().getConfig().set("confetti_effect", DEFAULT_CONFETTI_EFFECT);
    CreeperConfettiPro.getInstance().saveConfig();
    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>已恢复默认彩带效果！"));
  }

  private void handleSetEffect(CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>只有玩家可以使用此命令！"));
      return;
    }

    ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

    if (itemInMainHand.getType() != Material.FIREWORK_ROCKET) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>请在主手中装备带有期望效果的烟花火箭。"));
      return;
    }

    FireworkMeta fireworkMeta = (FireworkMeta) itemInMainHand.getItemMeta();
    if (fireworkMeta == null || !fireworkMeta.hasEffects()) {
      sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>请在主手中装备带有期望效果的烟花火箭。"));
      return;
    }


    CreeperConfettiPro.getInstance().getConfig().set("confetti_effect", fireworkMeta.getEffects());
    CreeperConfettiPro.getInstance().saveConfig();
    sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>彩带效果现在设置为您主手中的烟花效果！"));


    showFireworkEffect(player);
  }


  private void showFireworkEffect(Player player) {

    Firework firework = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);

    FireworkMeta showcaseFireworkMeta = firework.getFireworkMeta();
    @SuppressWarnings("unchecked")
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


  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (args.length == 1) {
      return List.of("reload", "reseteffect", "seteffect");
    }
    return Collections.emptyList();
  }
}
