package creeperconfetti;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import creeperconfetti.util.GeoIpResolver;
import creeperconfetti.util.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class LanguageManager {
    private static LanguageManager instance;
    private volatile String currentLanguage = "en_us";
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, String> defaultMessages = new HashMap<>();
    private boolean defaultMessagesLoaded = false;
    private boolean initialized = false;
    private Runnable onInitializedCallback;
    private CreeperConfettiPro plugin;

    private static final String LANGUAGE_CONFIG_PATH = "language";
    private static final String LANGUAGES_FOLDER = "languages";
    private static final String DEFAULT_LANGUAGE = "en_us";

    private static final Set<String> SUPPORTED_LANGUAGES = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "zh_cn", "zh_tw", "en_us", "ja_jp", "ko_kr", "fr_fr", "de_de", "es_es", "it_it",
            "pt_pt", "ru_ru", "nl_nl", "pl_pl", "sv_se", "tr_tr", "ar_sa", "hi_in", "th_th"
    )));

    /**
     * Legacy language codes (before the {@code xx_yy} format was introduced)
     * mapped onto the new codes, so old configs keep working.
     */
    private static final Map<String, String> LEGACY_ALIASES = new HashMap<>();

    static {
        LEGACY_ALIASES.put("zh", "zh_cn");
        LEGACY_ALIASES.put("zht", "zh_tw");
        LEGACY_ALIASES.put("en", "en_us");
        LEGACY_ALIASES.put("ja", "ja_jp");
        LEGACY_ALIASES.put("ko", "ko_kr");
        LEGACY_ALIASES.put("fr", "fr_fr");
        LEGACY_ALIASES.put("de", "de_de");
        LEGACY_ALIASES.put("es", "es_es");
        LEGACY_ALIASES.put("it", "it_it");
        LEGACY_ALIASES.put("pt", "pt_pt");
        LEGACY_ALIASES.put("ru", "ru_ru");
        LEGACY_ALIASES.put("nl", "nl_nl");
        LEGACY_ALIASES.put("pl", "pl_pl");
        LEGACY_ALIASES.put("sv", "sv_se");
        LEGACY_ALIASES.put("tr", "tr_tr");
        LEGACY_ALIASES.put("ar", "ar_sa");
        LEGACY_ALIASES.put("hi", "hi_in");
        LEGACY_ALIASES.put("th", "th_th");
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public void setPlugin(CreeperConfettiPro plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        initialize(null);
    }

    public void initialize(Runnable callback) {
        this.onInitializedCallback = callback;
        copyDefaultLanguageFiles();
        detectLanguage();
    }

    private void copyDefaultLanguageFiles() {
        File languagesDir = new File(plugin.getDataFolder(), LANGUAGES_FOLDER);
        if (!languagesDir.exists()) {
            languagesDir.mkdirs();
        }

        // Rename files using the legacy codes (zh.yml -> zh_cn.yml) so user edits survive.
        for (Map.Entry<String, String> alias : LEGACY_ALIASES.entrySet()) {
            File legacyFile = new File(languagesDir, alias.getKey() + ".yml");
            File newFile = new File(languagesDir, alias.getValue() + ".yml");
            if (legacyFile.exists() && !newFile.exists() && legacyFile.renameTo(newFile)) {
                plugin.getLogger().info("[Lang] Renamed " + legacyFile.getName() + " -> " + newFile.getName());
            }
        }

        for (String langCode : SUPPORTED_LANGUAGES) {
            File langFile = new File(languagesDir, langCode + ".yml");
            if (!langFile.exists()) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(LANGUAGES_FOLDER + "/" + langCode + ".yml")) {
                    if (is != null) {
                        plugin.saveResource(LANGUAGES_FOLDER + "/" + langCode + ".yml", false);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("无法复制默认语言文件: " + langCode + ".yml - " + e.getMessage());
                }
            }
        }
    }

    private void detectLanguage() {
        String configLanguage = plugin.getConfig().getString(LANGUAGE_CONFIG_PATH, "auto");

        if (configLanguage != null && !configLanguage.equalsIgnoreCase("auto") && !configLanguage.trim().isEmpty()) {
            currentLanguage = validateLanguageCode(configLanguage.toLowerCase());
            String languageName = getLanguageDisplayName(currentLanguage);
            plugin.getLogger().info(CreeperConfettiPro.colorizeConsole("§7" + plugin.getConfig().getString("language") + " → " + languageName + " (" + currentLanguage + ")"));
            loadMessages();
            initialized = true;

            if (onInitializedCallback != null) {
                onInitializedCallback.run();
            }
            return;
        }

        // Folia: run the network lookup asynchronously, then hop back onto the
        // global region thread before touching Bukkit state.
        Schedulers.runAsync(plugin, () -> {
            GeoIpResolver resolver = new GeoIpResolver(plugin.getLogger(),
                    "CreeperConfettiPro/" + plugin.getDescription().getVersion(), plugin.isDebug());
            String countryCode = resolver.resolve();

            Schedulers.runGlobal(plugin, () -> applyCountry(countryCode));
        });
    }

    private void applyCountry(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            plugin.getLogger().warning("[GeoIP] All region APIs failed, falling back to English.");
            countryCode = "US";
        }

        currentLanguage = mapCountryToLanguage(countryCode);
        if (plugin.isDebug()) {
            String languageName = getLanguageDisplayName(currentLanguage);
            plugin.getLogger().info(CreeperConfettiPro.colorizeConsole("§7[GeoIP] " + countryCode + " → " + languageName + " (" + currentLanguage + ")"));
        }

        loadMessages();
        initialized = true;

        if (onInitializedCallback != null) {
            onInitializedCallback.run();
        }
    }

    private String mapCountryToLanguage(String countryCode) {
        switch (countryCode.toUpperCase()) {
            case "CN":
            case "MO":
                return "zh_cn";
            case "HK":
            case "TW":
                return "zh_tw";
            case "JP":
                return "ja_jp";
            case "FR":
            case "BE":
            case "CH":
            case "LU":
            case "MC":
            case "CA":
                return "fr_fr";
            case "RU":
            case "BY":
            case "KZ":
            case "KG":
            case "UA":
                return "ru_ru";
            case "KR":
                return "ko_kr";
            case "ES":
            case "MX":
            case "AR":
            case "CO":
            case "CL":
            case "PE":
            case "VE":
                return "es_es";
            case "DE":
            case "AT":
            case "LI":
                return "de_de";
            case "IT":
            case "SM":
                return "it_it";
            case "PT":
            case "BR":
                return "pt_pt";
            case "SA":
            case "AE":
            case "EG":
            case "DZ":
            case "MA":
                return "ar_sa";
            case "IN":
                return "hi_in";
            case "TR":
                return "tr_tr";
            case "NL":
                return "nl_nl";
            case "PL":
                return "pl_pl";
            case "SE":
                return "sv_se";
            case "TH":
                return "th_th";
            default:
                return "en_us";
        }
    }

    private String validateLanguageCode(String languageCode) {
        if (languageCode == null) {
            return DEFAULT_LANGUAGE;
        }
        String code = languageCode.toLowerCase().trim();
        String alias = LEGACY_ALIASES.get(code);
        if (alias != null) {
            code = alias;
        }
        if (SUPPORTED_LANGUAGES.contains(code)) {
            return code;
        }
        plugin.getLogger().warning("无效的语言代码: " + languageCode + "，使用默认语言: English");
        return DEFAULT_LANGUAGE;
    }

    private String getLanguageDisplayName(String langCode) {
        switch (langCode) {
            case "zh_cn": return "简体中文";
            case "zh_tw": return "繁體中文";
            case "ja_jp": return "日本語";
            case "fr_fr": return "Français";
            case "ru_ru": return "Русский";
            case "ko_kr": return "한국어";
            case "en_us": return "English";
            case "es_es": return "Español";
            case "de_de": return "Deutsch";
            case "it_it": return "Italiano";
            case "pt_pt": return "Português";
            case "ar_sa": return "العربية";
            case "hi_in": return "हिन्दी";
            case "tr_tr": return "Türkçe";
            case "nl_nl": return "Nederlands";
            case "pl_pl": return "Polski";
            case "sv_se": return "Svenska";
            case "th_th": return "ไทย";
            default: return "Unknown";
        }
    }

    private synchronized void loadMessages() {
        ensureDefaultMessages();
        messages.clear();

        File langFile = new File(new File(plugin.getDataFolder(), LANGUAGES_FOLDER), currentLanguage + ".yml");

        if (langFile.exists()) {
            YamlConfiguration config;
            try (FileInputStream fis = new FileInputStream(langFile);
                 InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                config = YamlConfiguration.loadConfiguration(reader);
            } catch (Exception e) {
                plugin.getLogger().warning("无法加载语言文件 " + currentLanguage + ".yml: " + e.getMessage());
                messages.putAll(defaultMessages);
                loadConsoleMessages();
                return;
            }

            loadSection(config, "command");
            loadSection(config, "help");
            loadSection(config, "effect");
            loadSection(config, "gui");
            loadSection(config, "language");
            loadSection(config, "chance");
        } else {
            messages.putAll(defaultMessages);
        }

        loadConsoleMessages();
    }

    /**
     * Recursively flattens a configuration section into dotted message keys.
     */
    private void loadSection(YamlConfiguration config, String section) {
        ConfigurationSection configSection = config.getConfigurationSection(section);
        if (configSection == null) {
            return;
        }
        for (String key : configSection.getKeys(false)) {
            String fullKey = section + "." + key;
            if (configSection.isConfigurationSection(key)) {
                loadSection(config, fullKey);
            } else {
                String value = configSection.getString(key);
                if (value != null) {
                    messages.put(fullKey, translateAlternateColorCodes(value));
                }
            }
        }
    }

    private String translateAlternateColorCodes(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void ensureDefaultMessages() {
        if (defaultMessagesLoaded) {
            return;
        }
        loadDefaultCommandMessages();
        loadDefaultHelpMessages();
        loadDefaultEffectMessages();
        loadDefaultGuiMessages();
        loadDefaultLanguageMessages();
        loadDefaultChanceMessages();
        defaultMessagesLoaded = true;
    }

    private void putDefault(String key, String value) {
        defaultMessages.put(key, translateAlternateColorCodes(value));
    }

    private void loadDefaultCommandMessages() {
        putDefault("command.no_permission", "&cYou don't have permission to use this command!");
        putDefault("command.player_only", "&cOnly players can use this command!");
        putDefault("command.hold_firework", "&cPlease hold a firework rocket with effects in your main hand.");
        putDefault("command.reload_success", "&aCreeperConfettiPro configuration and firework styles reloaded!");
        putDefault("command.unknown_subcommand", "&cUnknown subcommand: &e{0}&c. Type &e/cc help&c for a list.");
        putDefault("command.invalid_number", "&cInvalid number: &e{0}");
    }

    private void loadDefaultHelpMessages() {
        putDefault("help.header", "&6&l=== CreeperConfettiPro Help ===");
        putDefault("help.footer", "&7Tip: use &e/cc effect chance &7to tune how often each style fires.");
        putDefault("help.separator", " &8| ");
        putDefault("help.prev", "&e[<< Prev]");
        putDefault("help.prev_disabled", "&8[<< Prev]");
        putDefault("help.next", "&e[Next >>]");
        putDefault("help.next_disabled", "&8[Next >>]");
        putDefault("help.prev_hover", "&7Click to view the previous page");
        putDefault("help.next_hover", "&7Click to view the next page");
        putDefault("help.page_info", "&7Page &e{0}&7/&e{1}");
        putDefault("help.page_button", "&e[{0}]");
        putDefault("help.page_current", "&a&l[{0}]");
        putDefault("help.page_hover", "&7Click to open page {0}");
        putDefault("help.pagination_console", "&7Page &e{0}&7/&e{1} &8| &7Use &e/cc help <page> &7to switch pages.");
        putDefault("help.sub.help", "&e/cc help [page|command] &8- &7Show this help");
        putDefault("help.sub.effect", "&e/cc effect <list|add|set|remove|reset|preview|chance> &8- &7Manage firework styles");
        putDefault("help.sub.gui", "&e/cc gui &8- &7Open the graphical style manager");
        putDefault("help.sub.chance", "&e/cc chance [percent] &8- &7View or set the overall confetti chance");
        putDefault("help.sub.language", "&e/cc language <info|list|set|reload> &8- &7Manage the plugin language");
        putDefault("help.sub.reload", "&e/cc reload &8- &7Reload the configuration and firework styles");
        putDefault("help.detail.effect.list", "&8  &e/cc effect list &7- List all firework styles and their chances");
        putDefault("help.detail.effect.add", "&8  &e/cc effect add [name] &7- Add the held firework as a new style");
        putDefault("help.detail.effect.set", "&8  &e/cc effect set <name> &7- Overwrite a style with the held firework");
        putDefault("help.detail.effect.remove", "&8  &e/cc effect remove <name> &7- Delete a style");
        putDefault("help.detail.effect.reset", "&8  &e/cc effect reset &7- Restore the default styles");
        putDefault("help.detail.effect.preview", "&8  &e/cc effect preview [name] &7- Preview a style");
        putDefault("help.detail.effect.chance", "&8  &e/cc effect chance [name] [percent] &7- View / set per-style chances");
        putDefault("help.detail.chance.set", "&8  &e/cc chance <0-100> &7- Set the overall confetti chance");
        putDefault("help.detail.language.info", "&8  &e/cc language info &7- Show the current language");
        putDefault("help.detail.language.list", "&8  &e/cc language list &7- List all available languages");
        putDefault("help.detail.language.set", "&8  &e/cc language set <code> &7- Change the language");
        putDefault("help.detail.language.reload", "&8  &e/cc language reload &7- Re-detect the language");
    }

    private void loadDefaultEffectMessages() {
        putDefault("effect.list_header", "&6=== Firework Styles (&e{0}&6) ===");
        putDefault("effect.list_entry", "&e{0}. &f{1} &7({2} effect(s), &f{3}%&7)");
        putDefault("effect.list_empty", "&cNo firework styles configured.");
        putDefault("effect.random_info", "&7A weighted-random style is picked on every trigger.");
        putDefault("effect.set_success", "&aStyle '&e{0}&a' saved from the firework in your main hand!");
        putDefault("effect.add_success", "&aStyle '&e{0}&a' added!");
        putDefault("effect.add_exists", "&cStyle '&e{0}&c' already exists. Use '&e/cc effect set {0}&c' to overwrite it.");
        putDefault("effect.remove_success", "&aStyle '&e{0}&a' removed!");
        putDefault("effect.not_found", "&cStyle '&e{0}&c' does not exist.");
        putDefault("effect.reset_success", "&aDefault firework styles restored!");
        putDefault("effect.preview_success", "&aPreviewing style '&e{0}&a'...");
        putDefault("effect.preview_random", "&aPreviewing a random style...");
        putDefault("effect.name_required", "&cYou must specify a style name. Usage: &e/cc effect {0} <name>");
        putDefault("effect.usage", "&cUsage: /cc effect <list|add|set|remove|reset|preview|chance>");
        putDefault("effect.chance_header", "&6=== Firework Style Chances ===");
        putDefault("effect.chance_entry", "&e{0}&7: &f{1}%");
        putDefault("effect.chance_sum", "&7Total: &f{0}% &7(kept at 100% automatically)");
        putDefault("effect.chance_set", "&aStyle '&e{0}&a' chance set to &f{1}%&a; the other styles were auto-adjusted.");
        putDefault("effect.chance_reset", "&aAll style chances were reset to an equal distribution!");
        putDefault("effect.chance_invalid", "&cInvalid value! Please provide a number equal to or greater than 0.");
        putDefault("effect.chance_usage", "&cUsage: /cc effect chance [<name> <percent> | reset]");
    }

    private void loadDefaultGuiMessages() {
        putDefault("gui.title", "&8Firework Style Manager");
        putDefault("gui.edit_title", "&8Style: {0}");
        putDefault("gui.style_effects", "&7Effects: &f{0}");
        putDefault("gui.style_chance", "&7Chance: &f{0}%");
        putDefault("gui.style_click", "&eLeft-click &7edit &8| &eRight-click &7preview");
        putDefault("gui.add", "&a&lAdd firework style");
        putDefault("gui.add_lore", "&7Add the firework in your main hand as a new style");
        putDefault("gui.add_success", "&aStyle '&e{0}&a' added!");
        putDefault("gui.reset_chances", "&eReset chances (equal split)");
        putDefault("gui.reset_chances_lore", "&7Reset every style to the same chance");
        putDefault("gui.prev", "&ePrevious page");
        putDefault("gui.next", "&eNext page");
        putDefault("gui.page", "&7Page &e{0}&7/&e{1}");
        putDefault("gui.preview_lore", "&7Click to preview this style");
        putDefault("gui.minus", "&c-{0}%");
        putDefault("gui.plus", "&a+{0}%");
        putDefault("gui.chance_name", "&f{0}");
        putDefault("gui.chance_lore", "&7Current chance: &e{0}% &7(total stays at 100%)");
        putDefault("gui.chance_set", "&aStyle '&e{0}&a' chance is now &f{1}%&a.");
        putDefault("gui.set_from_hand", "&bOverwrite with held firework");
        putDefault("gui.set_from_hand_lore", "&7Write the firework in your main hand into this style");
        putDefault("gui.set_from_hand_success", "&aStyle '&e{0}&a' updated from your main hand!");
        putDefault("gui.delete", "&c&lDelete this style");
        putDefault("gui.delete_lore", "&7Click to delete '&f{0}&7'");
        putDefault("gui.delete_success", "&aStyle '&e{0}&a' deleted!");
        putDefault("gui.back", "&eBack to list");
    }

    private void loadDefaultLanguageMessages() {
        putDefault("language.info", "&eCurrent plugin language: &f{0} &7({1})");
        putDefault("language.list_header", "&6=== Available Languages ===");
        putDefault("language.list_entry", "&7- &f{0} &7({1})");
        putDefault("language.list_current", "&a> &f{0} &7({1}) &a<- current");
        putDefault("language.set_success", "&aPlugin language set to: &f{0}");
        putDefault("language.reloading", "&eRe-detecting language settings...");
        putDefault("language.reloaded", "&aLanguage reloaded! Current language: &f{0}");
        putDefault("language.invalid", "&cInvalid language code: &e{0}");
        putDefault("language.set_usage", "&cUsage: /cc language set <language_code>");
        putDefault("language.usage", "&cUsage: /cc language <info|list|set|reload>");
    }

    private void loadDefaultChanceMessages() {
        putDefault("chance.info", "&eCurrent confetti chance: &f{0}%");
        putDefault("chance.set", "&aConfetti chance set to &f{0}%&a.");
        putDefault("chance.invalid", "&cInvalid value! Please provide a number between 0 and 100.");
        putDefault("chance.usage", "&cUsage: /cc chance <0-100>");
    }

    private void loadConsoleMessages() {
        switch (currentLanguage) {
            case "zh_cn":
                loadZhConsoleMessages();
                break;
            case "zh_tw":
                loadZhtConsoleMessages();
                break;
            case "ja_jp":
                loadJaConsoleMessages();
                break;
            case "fr_fr":
                loadFrConsoleMessages();
                break;
            case "ru_ru":
                loadRuConsoleMessages();
                break;
            case "ko_kr":
                loadKoConsoleMessages();
                break;
            case "es_es":
                loadEsConsoleMessages();
                break;
            case "de_de":
                loadDeConsoleMessages();
                break;
            case "it_it":
                loadItConsoleMessages();
                break;
            case "pt_pt":
                loadPtConsoleMessages();
                break;
            case "ar_sa":
                loadArConsoleMessages();
                break;
            case "hi_in":
                loadHiConsoleMessages();
                break;
            case "tr_tr":
                loadTrConsoleMessages();
                break;
            case "nl_nl":
                loadNlConsoleMessages();
                break;
            case "pl_pl":
                loadPlConsoleMessages();
                break;
            case "sv_se":
                loadSvConsoleMessages();
                break;
            case "th_th":
                loadThConsoleMessages();
                break;
            default:
                loadEnConsoleMessages();
                break;
        }
    }

    private void loadEnConsoleMessages() {
        messages.put("console.loading", "§6    CreeperConfettiPro plugin is loading...");
        messages.put("console.enabled", "§a    CreeperConfettiPro plugin enabled successfully!");
        messages.put("console.disabled", "§c    CreeperConfettiPro plugin is unloading...");
        messages.put("console.version", "§7    Version: §f");
        messages.put("console.author", "§7    Branch Author: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Server Java Version: §f");
        messages.put("console.thanks", "§7    Thank you for using this plugin!");
        messages.put("console.bstats_enabled", "§b    ☁️ Cloud statistics feature enabled!");
        messages.put("console.bstats_collecting", "§7    Collecting plugin usage data to optimize experience...");
        messages.put("console.java_version_low", "§c❌ Detected server Java version below 14, plugin will be disabled automatically!");
        messages.put("console.java_current_version", "§7Server current Java version: §f");
        messages.put("console.language_info", "§rCurrent plugin language: ");
        messages.put("console.language_config_detected", "§7Detected config language setting: ");
        messages.put("console.language_region_detected", "§7Detected server region code: ");
        messages.put("console.java_parse_error", "§eUnable to parse Java version: ");
    }

    private void loadZhConsoleMessages() {
        messages.put("console.loading", "§6    CreeperConfettiPro 插件正在加载中...");
        messages.put("console.enabled", "§a    CreeperConfettiPro 插件已成功启用！");
        messages.put("console.disabled", "§c    CreeperConfettiPro 插件正在卸载...");
        messages.put("console.version", "§7    版本: §f");
        messages.put("console.author", "§7    分支作者: §dHi_PolarBear");
        messages.put("console.java_version", "§7    服务器Java版本: §f");
        messages.put("console.thanks", "§7    感谢使用本插件！");
        messages.put("console.bstats_enabled", "§b    ☁️ 云数据统计功能已启用！");
        messages.put("console.bstats_collecting", "§7    正在收集插件使用数据以优化体验...");
        messages.put("console.java_version_low", "§c❌ 检测到服务器Java版本低于14，插件将自动禁用！");
        messages.put("console.java_current_version", "§7服务器当前Java版本: §f");
        messages.put("console.language_info", "§r当前插件语言: ");
        messages.put("console.language_config_detected", "§7检测到配置文件语言设置: ");
        messages.put("console.language_region_detected", "§7检测到服务器地区代码: ");
        messages.put("console.java_parse_error", "§e无法解析Java版本号: ");
    }

    private void loadZhtConsoleMessages() {
        messages.put("console.loading", "§6    CreeperConfettiPro 插件正在加載中...");
        messages.put("console.enabled", "§a    CreeperConfettiPro 插件已成功啟用！");
        messages.put("console.disabled", "§c    CreeperConfettiPro 插件正在卸載...");
        messages.put("console.version", "§7    版本: §f");
        messages.put("console.author", "§7    分支作者: §dHi_PolarBear");
        messages.put("console.java_version", "§7    伺服器Java版本: §f");
        messages.put("console.thanks", "§7    感謝使用本插件！");
        messages.put("console.bstats_enabled", "§b    ☁️ 雲數據統計功能已啟用！");
        messages.put("console.bstats_collecting", "§7    正在收集插件使用數據以優化體驗...");
        messages.put("console.java_version_low", "§c❌ 檢測到伺服器Java版本低於14，插件將自動禁用！");
        messages.put("console.java_current_version", "§7伺服器當前Java版本: §f");
        messages.put("console.language_info", "§r當前插件語言: ");
        messages.put("console.language_config_detected", "§7檢測到配置文件語言設置: ");
        messages.put("console.language_region_detected", "§7檢測到伺服器地區代碼: ");
        messages.put("console.java_parse_error", "§e無法解析Java版本號: ");
    }

    private void loadJaConsoleMessages() {
        messages.put("console.loading", "§6    CreeperConfettiPro プラグインを読み込んでいます...");
        messages.put("console.enabled", "§a    CreeperConfettiPro プラグインが正常に有効化されました！");
        messages.put("console.disabled", "§c    CreeperConfettiPro プラグインを無効化しています...");
        messages.put("console.version", "§7    バージョン: §f");
        messages.put("console.author", "§7    作者: §dHi_PolarBear");
        messages.put("console.java_version", "§7    サーバーJavaバージョン: §f");
        messages.put("console.thanks", "§7    このプラグインをご利用いただきありがとうございます！");
        messages.put("console.bstats_enabled", "§b    ☁️ クラウド統計機能が有効になりました！");
        messages.put("console.bstats_collecting", "§7    プラグイン使用データを収集してエクスペリエンスを最適化しています...");
        messages.put("console.java_version_low", "§c❌ サーバーJavaバージョンが14未満を検出しました。プラグインは自動的に無効になります！");
        messages.put("console.java_current_version", "§7サーバーの現在のJavaバージョン: §f");
        messages.put("console.language_info", "§r現在のプラグイン言語: ");
        messages.put("console.language_config_detected", "§7設定ファイルの言語設定を検出しました: ");
        messages.put("console.language_region_detected", "§7サーバーの地域コードを検出しました: ");
        messages.put("console.java_parse_error", "§eJavaバージョン番号の解析に失敗しました: ");
    }

    private void loadFrConsoleMessages() {
        messages.put("console.loading", "§6    Le plugin CreeperConfettiPro est en cours de chargement...");
        messages.put("console.enabled", "§a    Le plugin CreeperConfettiPro a été activé avec succès !");
        messages.put("console.disabled", "§c    Le plugin CreeperConfettiPro est en cours de désactivation...");
        messages.put("console.version", "§7    Version: §f");
        messages.put("console.author", "§7    Auteur: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Version Java du serveur: §f");
        messages.put("console.thanks", "§7    Merci d'utiliser ce plugin !");
        messages.put("console.bstats_enabled", "§b    ☁️ La fonction de statistiques cloud est activée !");
        messages.put("console.bstats_collecting", "§7    Collecte des données d'utilisation du plugin pour optimiser l'expérience...");
        messages.put("console.java_version_low", "§c❌ Version Java du serveur inférieure à 14 détectée, le plugin sera automatiquement désactivé !");
        messages.put("console.java_current_version", "§7Version Java actuelle du serveur: §f");
        messages.put("console.language_info", "§rLangue actuelle du plugin: ");
        messages.put("console.language_config_detected", "§7Paramètre de langue détecté dans la configuration: ");
        messages.put("console.language_region_detected", "§7Code de région du serveur détecté: ");
        messages.put("console.java_parse_error", "§eImpossible de parser la version Java: ");
    }

    private void loadRuConsoleMessages() {
        messages.put("console.loading", "§6    Плагин CreeperConfettiPro загружается...");
        messages.put("console.enabled", "§a    Плагин CreeperConfettiPro успешно активирован!");
        messages.put("console.disabled", "§c    Плагин CreeperConfettiPro деактивируется...");
        messages.put("console.version", "§7    Версия: §f");
        messages.put("console.author", "§7    Автор: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Версия Java сервера: §f");
        messages.put("console.thanks", "§7    Спасибо за использование этого плагина!");
        messages.put("console.bstats_enabled", "§b    ☁️ Функция облачной статистики включена!");
        messages.put("console.bstats_collecting", "§7    Сбор данных об использовании плагина для оптимизации опыта...");
        messages.put("console.java_version_low", "§c❌ Обнаружена версия Java сервера ниже 14, плагин будет автоматически отключен!");
        messages.put("console.java_current_version", "§7Текущая версия Java сервера: §f");
        messages.put("console.language_info", "§rТекущий язык плагина: ");
        messages.put("console.language_config_detected", "§7Обнаружена настройка языка в конфигурации: ");
        messages.put("console.language_region_detected", "§7Обнаружен код региона сервера: ");
        messages.put("console.java_parse_error", "§eНе удалось разобрать версию Java: ");
    }

    private void loadKoConsoleMessages() {
        messages.put("console.loading", "§6    CreeperConfettiPro 플러그인이 로드 중입니다...");
        messages.put("console.enabled", "§a    CreeperConfettiPro 플러그인이 성공적으로 활성화되었습니다!");
        messages.put("console.disabled", "§c    CreeperConfettiPro 플러그인을 비활성화하는 중입니다...");
        messages.put("console.version", "§7    버전: §f");
        messages.put("console.author", "§7    작성자: §dHi_PolarBear");
        messages.put("console.java_version", "§7    서버 Java 버전: §f");
        messages.put("console.thanks", "§7    이 플러그인을 사용해 주셔서 감사합니다!");
        messages.put("console.bstats_enabled", "§b    ☁️ 클라우드 통계 기능이 활성화되었습니다!");
        messages.put("console.bstats_collecting", "§7    경험을 최적화하기 위해 플러그인 사용 데이터를 수집하는 중입니다...");
        messages.put("console.java_version_low", "§c❌ 서버 Java 버전이 14 미만으로 감지되어 플러그인이 자동으로 비활성화됩니다!");
        messages.put("console.java_current_version", "§7서버의 현재 Java 버전: §f");
        messages.put("console.language_info", "§r현재 플러그인 언어: ");
        messages.put("console.language_config_detected", "§7설정 파일에서 언어 설정을 감지했습니다: ");
        messages.put("console.language_region_detected", "§7서버 지역 코드 감지: ");
        messages.put("console.java_parse_error", "§eJava 버전을 구문 분석할 수 없습니다: ");
    }

    private void loadEsConsoleMessages() {
        messages.put("console.loading", "§6    El plugin CreeperConfettiPro se está cargando...");
        messages.put("console.enabled", "§a    ¡El plugin CreeperConfettiPro se ha habilitado correctamente!");
        messages.put("console.disabled", "§c    El plugin CreeperConfettiPro se está deshabilitando...");
        messages.put("console.version", "§7    Versión: §f");
        messages.put("console.author", "§7    Autor: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Versión Java del servidor: §f");
        messages.put("console.thanks", "§7    ¡Gracias por usar este plugin!");
        messages.put("console.bstats_enabled", "§b    ☁️ ¡Función de estadísticas en la nube habilitada!");
        messages.put("console.bstats_collecting", "§7    Recopilando datos de uso del plugin para optimizar la experiencia...");
        messages.put("console.java_version_low", "§c❌ ¡Se detectó una versión de Java del servidor inferior a 14, el plugin se deshabilitará automáticamente!");
        messages.put("console.java_current_version", "§7Versión actual de Java del servidor: §f");
        messages.put("console.language_info", "§rIdioma actual del plugin: ");
        messages.put("console.language_config_detected", "§7Configuración de idioma detectada: ");
        messages.put("console.language_region_detected", "§7Código de región del servidor detectado: ");
        messages.put("console.java_parse_error", "§eNo se pudo analizar la versión de Java: ");
    }

    private void loadDeConsoleMessages() {
        messages.put("console.loading", "§6    Das CreeperConfettiPro-Plugin wird geladen...");
        messages.put("console.enabled", "§a    CreeperConfettiPro-Plugin erfolgreich aktiviert!");
        messages.put("console.disabled", "§c    CreeperConfettiPro-Plugin wird deaktiviert...");
        messages.put("console.version", "§7    Version: §f");
        messages.put("console.author", "§7    Autor: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Server-Java-Version: §f");
        messages.put("console.thanks", "§7    Danke, dass Sie dieses Plugin verwenden!");
        messages.put("console.bstats_enabled", "§b    ☁️ Cloud-Statistikfunktion aktiviert!");
        messages.put("console.bstats_collecting", "§7    Sammeln von Plugin-Nutzungsdaten zur Optimierung der Erfahrung...");
        messages.put("console.java_version_low", "§c❌ Server-Java-Version unter 14 erkannt, Plugin wird automatisch deaktiviert!");
        messages.put("console.java_current_version", "§7Aktuelle Server-Java-Version: §f");
        messages.put("console.language_info", "§rAktuelle Plugin-Sprache: ");
        messages.put("console.language_config_detected", "§7Spracheinstellung aus Konfiguration erkannt: ");
        messages.put("console.language_region_detected", "§7Server-Regionscode erkannt: ");
        messages.put("console.java_parse_error", "§eJava-Version kann nicht analysiert werden: ");
    }

    private void loadItConsoleMessages() {
        messages.put("console.loading", "§6    Il plugin CreeperConfettiPro si sta caricando...");
        messages.put("console.enabled", "§a    Plugin CreeperConfettiPro abilitato con successo!");
        messages.put("console.disabled", "§c    Il plugin CreeperConfettiPro si sta disabilitando...");
        messages.put("console.version", "§7    Versione: §f");
        messages.put("console.author", "§7    Autore: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Versione Java del server: §f");
        messages.put("console.thanks", "§7    Grazie per aver utilizzato questo plugin!");
        messages.put("console.bstats_enabled", "§b    ☁️ Funzione di statistiche cloud abilitata!");
        messages.put("console.bstats_collecting", "§7    Raccolta dei dati di utilizzo del plugin per ottimizzare l'esperienza...");
        messages.put("console.java_version_low", "§c❌ Rilevata versione Java del server inferiore a 14, il plugin verrà disabilitato automaticamente!");
        messages.put("console.java_current_version", "§7Versione Java corrente del server: §f");
        messages.put("console.language_info", "§rLingua attuale del plugin: ");
        messages.put("console.language_config_detected", "§7Impostazione lingua rilevata dalla configurazione: ");
        messages.put("console.language_region_detected", "§7Codice regione server rilevato: ");
        messages.put("console.java_parse_error", "§eImpossibile analizzare la versione Java: ");
    }

    private void loadPtConsoleMessages() {
        messages.put("console.loading", "§6    O plugin CreeperConfettiPro está carregando...");
        messages.put("console.enabled", "§a    Plugin CreeperConfettiPro ativado com sucesso!");
        messages.put("console.disabled", "§c    O plugin CreeperConfettiPro está sendo desativado...");
        messages.put("console.version", "§7    Versão: §f");
        messages.put("console.author", "§7    Autor: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Versão Java do servidor: §f");
        messages.put("console.thanks", "§7    Obrigado por usar este plugin!");
        messages.put("console.bstats_enabled", "§b    ☁️ Função de estatísticas na nuvem ativada!");
        messages.put("console.bstats_collecting", "§7    Coletando dados de uso do plugin para otimizar a experiência...");
        messages.put("console.java_version_low", "§c❌ Versão Java do servidor abaixo de 14 detectada, o plugin será desativado automaticamente!");
        messages.put("console.java_current_version", "§7Versão Java atual do servidor: §f");
        messages.put("console.language_info", "§rIdioma atual do plugin: ");
        messages.put("console.language_config_detected", "§7Configuração de idioma detectada: ");
        messages.put("console.language_region_detected", "§7Código de região do servidor detectado: ");
        messages.put("console.java_parse_error", "§eNão foi possível analisar a versão Java: ");
    }

    private void loadArConsoleMessages() {
        messages.put("console.loading", "§6    جاري تحميل إضافة CreeperConfettiPro...");
        messages.put("console.enabled", "§a    تم تفعيل إضافة CreeperConfettiPro بنجاح!");
        messages.put("console.disabled", "§c    جاري تعطيل إضافة CreeperConfettiPro...");
        messages.put("console.version", "§7    الإصدار: §f");
        messages.put("console.author", "§7    المؤلف: §dHi_PolarBear");
        messages.put("console.java_version", "§7    إصدار جافا للخادم: §f");
        messages.put("console.thanks", "§7    شكراً لاستخدامك هذه الإضافة!");
        messages.put("console.bstats_enabled", "§b    ☁️ تم تفعيل ميزة إحصائيات السحابة!");
        messages.put("console.bstats_collecting", "§7    جاري جمع بيانات استخدام الإضافة لتحسين التجربة...");
        messages.put("console.java_version_low", "§c❌ تم اكتشاف إصدار جافا للخادم أقل من 14، سيتم تعطيل الإضافة تلقائياً!");
        messages.put("console.java_current_version", "§7إصدار جافا الحالي للخادم: §f");
        messages.put("console.language_info", "§rلغة الإضافة الحالية: ");
        messages.put("console.language_config_detected", "§7تم اكتشاف إعداد اللغة من الملف: ");
        messages.put("console.language_region_detected", "§7تم اكتشاف رمز منطقة الخادم: ");
        messages.put("console.java_parse_error", "§eتعذر تحليل إصدار جافا: ");
    }

    private void loadHiConsoleMessages() {
        messages.put("console.loading", "§6    CreeperConfettiPro प्लगइन लोड हो रहा है...");
        messages.put("console.enabled", "§a    CreeperConfettiPro प्लगइन सफलतापूर्वक सक्षम किया गया!");
        messages.put("console.disabled", "§c    CreeperConfettiPro प्लगइन अक्षम हो रहा है...");
        messages.put("console.version", "§7    संस्करण: §f");
        messages.put("console.author", "§7    लेखक: §dHi_PolarBear");
        messages.put("console.java_version", "§7    सर्वर जावा संस्करण: §f");
        messages.put("console.thanks", "§7    इस प्लगइन का उपयोग करने के लिए धन्यवाद!");
        messages.put("console.bstats_enabled", "§b    ☁️ क्लाउड सांख्यिकी सुविधा सक्षम की गई!");
        messages.put("console.bstats_collecting", "§7    अनुभव को अनुकूलित करने के लिए प्लगइन उपयोग डेटा एकत्र किया जा रहा है...");
        messages.put("console.java_version_low", "§c❌ सर्वर जावा संस्करण 14 से कम पाया गया, प्लगइन स्वचालित रूप से अक्षम कर दिया जाएगा!");
        messages.put("console.java_current_version", "§7वर्तमान सर्वर जावा संस्करण: §f");
        messages.put("console.language_info", "§rवर्तमान प्लगइन भाषा: ");
        messages.put("console.language_config_detected", "§7कॉन्फ़िग से भाषा सेटिंग का पता चला: ");
        messages.put("console.language_region_detected", "§7सर्वर क्षेत्र कोड का पता चला: ");
        messages.put("console.java_parse_error", "§eजावा संस्करण को पार्स नहीं किया जा सका: ");
    }

    private void loadTrConsoleMessages() {
        messages.put("console.loading", "§6    CreeperConfettiPro eklentisi yükleniyor...");
        messages.put("console.enabled", "§a    CreeperConfettiPro eklentisi başarıyla etkinleştirildi!");
        messages.put("console.disabled", "§c    CreeperConfettiPro eklentisi devre dışı bırakılıyor...");
        messages.put("console.version", "§7    Sürüm: §f");
        messages.put("console.author", "§7    Yazar: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Sunucu Java Sürümü: §f");
        messages.put("console.thanks", "§7    Bu eklentiyi kullandığınız için teşekkürler!");
        messages.put("console.bstats_enabled", "§b    ☁️ Bulut istatistikleri özelliği etkinleştirildi!");
        messages.put("console.bstats_collecting", "§7    Deneyimi optimize etmek için eklenti kullanım verileri toplanıyor...");
        messages.put("console.java_version_low", "§c❌ Sunucu Java sürümü 14'ün altında algılandı, eklenti otomatik olarak devre dışı bırakılacak!");
        messages.put("console.java_current_version", "§7Geçerli sunucu Java sürümü: §f");
        messages.put("console.language_info", "§rMevcut eklenti dili: ");
        messages.put("console.language_config_detected", "§7Yapılandırmadan dil ayarı algılandı: ");
        messages.put("console.language_region_detected", "§7Sunucu bölge kodu algılandı: ");
        messages.put("console.java_parse_error", "§eJava sürümü çözülemedi: ");
    }

    private void loadNlConsoleMessages() {
        messages.put("console.loading", "§6    CreeperConfettiPro plugin wordt geladen...");
        messages.put("console.enabled", "§a    CreeperConfettiPro plugin succesvol geactiveerd!");
        messages.put("console.disabled", "§c    CreeperConfettiPro plugin wordt uitgeschakeld...");
        messages.put("console.version", "§7    Versie: §f");
        messages.put("console.author", "§7    Auteur: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Server Java-versie: §f");
        messages.put("console.thanks", "§7    Bedankt voor het gebruik van deze plugin!");
        messages.put("console.bstats_enabled", "§b    ☁️ Cloud-statistiekenfunctie ingeschakeld!");
        messages.put("console.bstats_collecting", "§7    Plugin-gebruiksgegevens verzamelen om de ervaring te optimaliseren...");
        messages.put("console.java_version_low", "§c❌ Server Java-versie onder 14 gedetecteerd, plugin wordt automatisch uitgeschakeld!");
        messages.put("console.java_current_version", "§7Huidige server Java-versie: §f");
        messages.put("console.language_info", "§rHuidige plugin-taal: ");
        messages.put("console.language_config_detected", "§7Taalaanwerking gedetecteerd uit config: ");
        messages.put("console.language_region_detected", "§7Serverregiocode gedetecteerd: ");
        messages.put("console.java_parse_error", "§eKan Java-versie niet analyseren: ");
    }

    private void loadPlConsoleMessages() {
        messages.put("console.loading", "§6    Wtyczka CreeperConfettiPro się ładuje...");
        messages.put("console.enabled", "§a    Wtyczka CreeperConfettiPro została pomyślnie aktywowana!");
        messages.put("console.disabled", "§c    Wtyczka CreeperConfettiPro jest wyłączana...");
        messages.put("console.version", "§7    Wersja: §f");
        messages.put("console.author", "§7    Autor: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Wersja Javy serwera: §f");
        messages.put("console.thanks", "§7    Dziękujemy za korzystanie z tej wtyczki!");
        messages.put("console.bstats_enabled", "§b    ☁️ Funkcja statystyk chmurowych została włączona!");
        messages.put("console.bstats_collecting", "§7    Zbieranie danych użytkowania wtyczki w celu optymalizacji doświadczenia...");
        messages.put("console.java_version_low", "§c❌ Wykryto wersję Javy serwera poniżej 14, wtyczka zostanie automatycznie wyłączona!");
        messages.put("console.java_current_version", "§7Aktualna wersja Javy serwera: §f");
        messages.put("console.language_info", "§rAktualny język wtyczki: ");
        messages.put("console.language_config_detected", "§7Wykryto ustawienie języka z konfiguracji: ");
        messages.put("console.language_region_detected", "§7Wykryto kod regionu serwera: ");
        messages.put("console.java_parse_error", "§eNie można przeanalizować wersji Javy: ");
    }

    private void loadSvConsoleMessages() {
        messages.put("console.loading", "§6    CreeperConfettiPro-pluginen laddas...");
        messages.put("console.enabled", "§a    CreeperConfettiPro-pluginen aktiverades framgångsrikt!");
        messages.put("console.disabled", "§c    CreeperConfettiPro-pluginen inaktiveras...");
        messages.put("console.version", "§7    Version: §f");
        messages.put("console.author", "§7    Författare: §dHi_PolarBear");
        messages.put("console.java_version", "§7    Serverns Java-version: §f");
        messages.put("console.thanks", "§7    Tack för att du använder detta plugin!");
        messages.put("console.bstats_enabled", "§b    ☁️ Molnstatistikfunktionen är aktiverad!");
        messages.put("console.bstats_collecting", "§7    Samlar in pluginanvändningsdata för att optimera upplevelsen...");
        messages.put("console.java_version_low", "§c❌ Serverns Java-version under 14 upptäckt, pluginen kommer att inaktiveras automatiskt!");
        messages.put("console.java_current_version", "§7Nuvarande serverns Java-version: §f");
        messages.put("console.language_info", "§rNuvarande plugin-språk: ");
        messages.put("console.language_config_detected", "§7Språkinställning upptäckt från konfiguration: ");
        messages.put("console.language_region_detected", "§7Serverregionkod upptäckt: ");
        messages.put("console.java_parse_error", "§eKan inte analysera Java-version: ");
    }

    private void loadThConsoleMessages() {
        messages.put("console.loading", "§6    กำลังโหลดปลั๊กอิน CreeperConfettiPro...");
        messages.put("console.enabled", "§a    เปิดใช้งานปลั๊กอิน CreeperConfettiPro สำเร็จแล้ว!");
        messages.put("console.disabled", "§c    กำลังปิดใช้งานปลั๊กอิน CreeperConfettiPro...");
        messages.put("console.version", "§7    เวอร์ชัน: §f");
        messages.put("console.author", "§7    ผู้เขียน: §dHi_PolarBear");
        messages.put("console.java_version", "§7    เวอร์ชัน Java ของเซิร์ฟเวอร์: §f");
        messages.put("console.thanks", "§7    ขอบคุณที่ใช้ปลั๊กอินนี้!");
        messages.put("console.bstats_enabled", "§b    ☁️ เปิดใช้งานคุณสมบัติสถิติบนคลาวด์แล้ว!");
        messages.put("console.bstats_collecting", "§7    กำลังรวบรวมข้อมูลการใช้งานปลั๊กอินเพื่อปรับปรุงประสบการณ์...");
        messages.put("console.java_version_low", "§c❌ ตรวจพบเวอร์ชัน Java ของเซิร์ฟเวอร์ต่ำกว่า 14 ปลั๊กอินจะถูกปิดใช้งานโดยอัตโนมัติ!");
        messages.put("console.java_current_version", "§7เวอร์ชัน Java ปัจจุบันของเซิร์ฟเวอร์: §f");
        messages.put("console.language_info", "§rภาษาปลั๊กอินปัจจุบัน: ");
        messages.put("console.language_config_detected", "§7ตรวจพบการตั้งค่าภาษาจากคอนฟิก: ");
        messages.put("console.language_region_detected", "§7ตรวจพบรหัสพื้นที่เซิร์ฟเวอร์: ");
        messages.put("console.java_parse_error", "§eไม่สามารถแยกวิเคราะห์เวอร์ชัน Java ได้: ");
    }

    public synchronized String getMessage(String key) {
        ensureDefaultMessages();
        String value = messages.get(key);
        if (value != null) {
            return value;
        }
        String fallback = defaultMessages.get(key);
        return fallback != null ? fallback : key;
    }

    public synchronized String getConsoleMessage(String key) {
        return messages.getOrDefault(key, key);
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public String getCurrentLanguageDisplayName() {
        if (currentLanguage == null || currentLanguage.isEmpty()) {
            return "Unknown";
        }
        String displayName = getLanguageDisplayName(currentLanguage);
        return displayName != null ? displayName : "Unknown";
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void reloadLanguage(Runnable callback) {
        this.onInitializedCallback = callback;
        initialized = false;
        copyDefaultLanguageFiles();
        detectLanguage();
    }

    /**
     * Re-reads the current language file from disk without re-detecting the
     * language. Used by {@code /cc reload} so edited texts apply immediately.
     */
    public void reloadMessages() {
        loadMessages();
    }

    public void setLanguage(String languageCode, Runnable callback) {
        String validatedLanguage = validateLanguageCode(languageCode.toLowerCase());
        if (!validatedLanguage.equals(currentLanguage)) {
            currentLanguage = validatedLanguage;
            loadMessages();
            plugin.getConfig().set(LANGUAGE_CONFIG_PATH, currentLanguage);
            plugin.saveConfig();
        }

        if (callback != null) {
            callback.run();
        }
    }

    public Set<String> getAvailableLanguages() {
        return Collections.unmodifiableSet(SUPPORTED_LANGUAGES);
    }

    public String getLanguageDisplayNameFor(String langCode) {
        return getLanguageDisplayName(langCode);
    }
}
