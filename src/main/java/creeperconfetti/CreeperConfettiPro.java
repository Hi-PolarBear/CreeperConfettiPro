
package creeperconfetti;


import creeperconfetti.commands.CreeperConfettiCommand  ;
import creeperconfetti.events.CreeperExplodeListener  ;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CreeperConfettiPro extends JavaPlugin {
  public void onEnable() {
    saveDefaultConfig();
    instance = this;

    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&6    CreeperConfettiPro 插件正在加载中..."));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    版本: &f" + getDescription().getVersion()));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    分支作者: &dNice_Cam_"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));

    getServer().getPluginManager().registerEvents(new CreeperExplodeListener(), this);
    Objects.requireNonNull(getCommand("creeperconfetti")).setExecutor(new CreeperConfettiCommand());

      getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&a    CreeperConfettiPro 插件已成功启用！"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    感谢使用本插件！"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
  }

  public void onDisable() {
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&c    CreeperConfettiPro 插件正在卸载..."));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    版本: &f" + getDescription().getVersion()));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    感谢使用本插件！"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
  }

  private static CreeperConfettiPro instance;
  public static CreeperConfettiPro getInstance() {
    return instance;
  }
}


