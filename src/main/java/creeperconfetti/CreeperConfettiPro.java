package creeperconfetti;

import creeperconfetti.commands.CreeperConfettiCommand;
import creeperconfetti.events.CreeperExplodeListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CreeperConfettiPro extends JavaPlugin {
    private LanguageManager languageManager;
    private boolean javaVersionChecked = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        languageManager = LanguageManager.getInstance();
        languageManager.setPlugin(this); // 传递插件实例
        languageManager.initialize(this::onLanguageInitialized);

        checkJavaVersion();
    }

    private void checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        if (!isJava14OrAbove(javaVersion)) {
            getLogger().severe(ChatColor.RED + "❌ 检测到服务器Java版本低于14，插件将自动禁用！");
            getLogger().severe(ChatColor.RED + "服务器当前Java版本: " + javaVersion);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        javaVersionChecked = true;
    }

    private void onLanguageInitialized() {
        if (!javaVersionChecked) {
            checkJavaVersion();
            if (!javaVersionChecked) return;
        }

        getLogger().info(ChatColor.GOLD + "----------------------------------------");
        getLogger().info(ChatColor.GREEN + languageManager.getMessage("plugin.loading"));
        getLogger().info(ChatColor.AQUA + languageManager.getMessage("plugin.version") + getDescription().getVersion());
        getLogger().info(ChatColor.YELLOW + languageManager.getMessage("plugin.author"));
        getLogger().info(ChatColor.LIGHT_PURPLE + languageManager.getMessage("plugin.java_version") + System.getProperty("java.version"));
        getLogger().info(ChatColor.GOLD + "----------------------------------------");

        getServer().getPluginManager().registerEvents(new CreeperExplodeListener (), this);
        Objects.requireNonNull(getCommand("creeperconfetti")).setExecutor(new CreeperConfettiCommand ());

        new MetricsHelper(this);
        getLogger().info(ChatColor.BLUE + languageManager.getMessage("plugin.bstats_enabled"));
        getLogger().info(ChatColor.DARK_AQUA + languageManager.getMessage("plugin.bstats_collecting"));

        getLogger().info(ChatColor.GOLD + "----------------------------------------");
        getLogger().info(ChatColor.GREEN + languageManager.getMessage("plugin.enabled"));
        getLogger().info(ChatColor.YELLOW + languageManager.getMessage("plugin.thanks"));
        getLogger().info(ChatColor.GOLD + "----------------------------------------");

        getLogger().info(ChatColor.RESET + "当前插件语言: " + languageManager.getCurrentLanguageDisplayName() + " (" + languageManager.getCurrentLanguage() + ")");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.GOLD + "----------------------------------------");
        getLogger().info(ChatColor.RED + languageManager.getMessage("plugin.disabled"));
        getLogger().info(ChatColor.AQUA + languageManager.getMessage("plugin.version") + getDescription().getVersion());
        getLogger().info(ChatColor.YELLOW + languageManager.getMessage("plugin.thanks"));
        getLogger().info(ChatColor.GOLD + "----------------------------------------");
    }

    private static CreeperConfettiPro instance;
    public static CreeperConfettiPro getInstance() {
        return instance;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    private boolean isJava14OrAbove(String version) {
        try {
            String[] parts = version.split("\\.");
            int majorVersion = Integer.parseInt(parts[0]);
            if (majorVersion > 1) {
                return majorVersion >= 14;
            } else {
                int minorVersion = Integer.parseInt(parts[1]);
                return minorVersion >= 14;
            }
        } catch (Exception e) {
            getLogger().warning(ChatColor.YELLOW + "无法解析Java版本号: " + version);
            return false;
        }
    }
}


