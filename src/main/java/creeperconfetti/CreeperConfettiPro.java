package creeperconfetti;

import creeperconfetti.commands.CreeperConfettiCommand;
import creeperconfetti.events.CreeperExplodeListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CreeperConfettiPro extends JavaPlugin {
  public void onEnable() {
    saveDefaultConfig();
    instance = this;

    String javaVersion = System.getProperty("java.version");
    if (!isJava17OrAbove(javaVersion)) {
      getLogger().severe(ChatColor.translateAlternateColorCodes('&', "&c❌ 检测到Java版本低于17，插件将自动禁用！"));
      getLogger().severe(ChatColor.translateAlternateColorCodes('&', "&7当前Java版本: &f" + javaVersion));
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&6    CreeperConfettiPro 插件正在加载中..."));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    版本: &f" + getDescription().getVersion()));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    分支作者: &dNice_Cam_"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    Java版本: &f" + javaVersion));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));

    getServer().getPluginManager().registerEvents(new CreeperExplodeListener(), this);
    Objects.requireNonNull(getCommand("creeperconfetti")).setExecutor(new CreeperConfettiCommand());

    new MetricsHelper(this);
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&b    ☁️ 云数据统计功能已启用！"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    正在收集插件使用数据以优化体验..."));

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

  private boolean isJava17OrAbove(String version) {
    try {
      String[] parts = version.split("\\.");
      int majorVersion = Integer.parseInt(parts[0]);
      if (majorVersion > 1) {
        return majorVersion >= 17;
      } else {
        int minorVersion = Integer.parseInt(parts[1]);
        return minorVersion >= 17;
      }
    } catch (Exception e) {
      getLogger().warning("无法解析Java版本号: " + version);
      return false;
    }
  }
}


