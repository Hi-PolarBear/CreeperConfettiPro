
package creeperconfetti;


import creeperconfetti.commands.CreeperConfettiCommand;
import creeperconfetti.events.CreeperExplodeListener;
import creeperconfetti.metrics.MetricsLite;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CreeperConfetti extends JavaPlugin {
  public void onEnable() {
    saveDefaultConfig();
    instance = this;

    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&6    CreeperConfettiPro 插件正在加载中..."));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    版本: &f" + getDescription().getVersion()));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    分支作者: &dNice_Cam_"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
    
    getServer().getPluginManager().registerEvents((Listener)new CreeperExplodeListener(), (Plugin)this);
    getCommand("creeperconfetti").setExecutor((CommandExecutor)new CreeperConfettiCommand());

    MetricsLite metrics = new MetricsLite((Plugin)this, 9232);
    
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&a    CreeperConfettiPro 插件已成功启用！"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7    感谢使用本插件！"));
    getLogger().info(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------"));
  }
  
  private static CreeperConfetti instance;
  public static CreeperConfetti getInstance() {
    return instance;
  }
}


