package creeperconfetti.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import creeperconfetti.CreeperConfetti;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class CreeperConfettiCommand implements TabExecutor {

  private final List<FireworkEffect> defaultConfettiEffect = new ArrayList<FireworkEffect>() {

  };

  public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
    if (!commandSender.hasPermission("creeperconfetti.command")) {
      commandSender.sendMessage("" + ChatColor.RED + "您没有权限使用此命令！");
      return true;
    }

    if (args.length == 0) {
      commandSender.sendMessage("" + ChatColor.RED + "用法: /creeperconfetti <reload | reseteffect | seteffect>");
      return true;
    }

    if (args[0].equalsIgnoreCase("reload")) {
      CreeperConfetti.getInstance().reloadConfig();
      commandSender.sendMessage("" + ChatColor.GREEN + "已重新加载CreeperConfetti配置！");
      return true;
    }

    if (args[0].equalsIgnoreCase("reseteffect")) {
      CreeperConfetti.getInstance().getConfig().set("confetti_effect", this.defaultConfettiEffect);
      CreeperConfetti.getInstance().saveConfig();
      commandSender.sendMessage("" + ChatColor.GREEN + "已恢复默认彩带效果！");
      return true;
    }

    if (args[0].equalsIgnoreCase("seteffect")) {
      if (!(commandSender instanceof Player)) {
        commandSender.sendMessage("" + ChatColor.RED + "只有玩家可以使用此命令！");
        return true;
      }

      Player player = (Player)commandSender;

      ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

      if (itemInMainHand.getType() != Material.FIREWORK_ROCKET) {
        commandSender.sendMessage("" + ChatColor.RED + "请在主手中装备带有期望效果的烟花火箭。");
        return true;
      }

      FireworkMeta fireworkMeta = (FireworkMeta)itemInMainHand.getItemMeta().clone();

      if (fireworkMeta.hasEffects()) {
        CreeperConfetti.getInstance().getConfig().set("confetti_effect", fireworkMeta.getEffects());
        CreeperConfetti.getInstance().saveConfig();
        commandSender.sendMessage("" + ChatColor.GREEN + "彩带效果现在设置为您主手中的烟花效果！");

        // 修复：将 EntityType.FIREWORK 替换为 EntityType.FIREWORK_ROCKET
        Firework firework = (Firework)player.getWorld().spawnEntity(player.getLocation().add(new Vector(0, 1, 0)), EntityType.FIREWORK_ROCKET);

        FireworkMeta showcaseFireworkMeta = firework.getFireworkMeta();
        showcaseFireworkMeta.addEffects((List)CreeperConfetti.getInstance().getConfig().get("confetti_effect"));
        showcaseFireworkMeta.setPower(0);
        firework.setFireworkMeta(showcaseFireworkMeta);

        Objects.requireNonNull(firework); Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)CreeperConfetti.getInstance(), firework::detonate, 1L);
      } else {
        commandSender.sendMessage("" + ChatColor.RED + "请在主手中装备带有期望效果的烟花火箭。");
      }

      return true;
    }

    commandSender.sendMessage("" + ChatColor.RED + "用法: /creeperconfetti <reload | reseteffect | seteffect>");

    return true;
  }


  public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
    if (args.length == 1) {
      return List.of("reload", "reseteffect", "seteffect");
    }
    return null;
  }
}

