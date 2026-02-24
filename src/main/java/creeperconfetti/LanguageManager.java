package creeperconfetti;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LanguageManager {
    private static LanguageManager instance;
    private String currentLanguage = "en";
    private final Map<String, String> messages = new HashMap<>();
    private boolean initialized = false;
    private Runnable onInitializedCallback;
    private CreeperConfettiPro plugin;

    private static final String IP_API_URL = "http://ip-api.com/json/?fields=countryCode";
    private static final String LANGUAGE_CONFIG_PATH = "language";

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
        detectLanguage();
    }

    private void detectLanguage() {
        String configLanguage = plugin.getConfig().getString(LANGUAGE_CONFIG_PATH, "auto");

        if (configLanguage != null && !configLanguage.equalsIgnoreCase("auto") && !configLanguage.trim().isEmpty()) {
            currentLanguage = validateLanguageCode(configLanguage.toLowerCase());
            String languageName = getLanguageDisplayName(currentLanguage);
            plugin.getLogger().info("检测到配置文件语言设置: " + languageName + " (" + currentLanguage + ")");
            loadMessages();
            initialized = true;

            if (onInitializedCallback != null) {
                onInitializedCallback.run();
            }
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(IP_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                InputStream inputStream = connection.getInputStream();
                YamlConfiguration response = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));

                return response.getString("countryCode", "US");
            } catch (Exception e) {
                return "US";
            }
        }).thenAccept(countryCode -> {
            switch (countryCode.toUpperCase()) {
                case "CN":
                case "MO":
                    currentLanguage = "zh";
                    break;
                case "HK":
                case "TW":
                    currentLanguage = "zht";
                    break;
                case "JP":
                    currentLanguage = "ja";
                    break;
                case "FR":
                case "BE":
                case "CH":
                case "LU":
                case "MC":
                    currentLanguage = "fr";
                    break;
                case "RU":
                case "BY":
                case "KZ":
                case "KG":
                    currentLanguage = "ru";
                    break;
                case "KR":
                    currentLanguage = "ko";
                    break;
                default:
                    currentLanguage = "en";
                    break;
            }

            String languageName = getLanguageDisplayName(currentLanguage);
            plugin.getLogger().info("检测到服务器地区代码: " + countryCode + ", 使用语言: " + languageName);

            loadMessages();
            initialized = true;

            if (onInitializedCallback != null) {
                onInitializedCallback.run();
            }
        });
    }

    private String validateLanguageCode(String languageCode) {
        switch (languageCode) {
            case "zh":
            case "zht":
            case "ja":
            case "fr":
            case "ru":
            case "ko":
            case "en":
            case "es":
            case "de":
            case "it":
            case "pt":
            case "ar":
            case "hi":
            case "tr":
            case "nl":
            case "pl":
            case "sv":
            case "th":
                return languageCode;
            default:
                plugin.getLogger().warning("无效的语言代码: " + languageCode + "，使用默认语言: English");
                return "en";
        }
    }

    private String getLanguageDisplayName(String langCode) {
        switch (langCode) {
            case "zh": return "简体中文";
            case "zht": return "繁體中文";
            case "ja": return "日本語";
            case "fr": return "Français";
            case "ru": return "Русский";
            case "ko": return "한국어";
            case "en": return "English";
            case "es": return "Español";
            case "de": return "Deutsch";
            case "it": return "Italiano";
            case "pt": return "Português";
            case "ar": return "العربية";
            case "hi": return "हिन्दी";
            case "tr": return "Türkçe";
            case "nl": return "Nederlands";
            case "pl": return "Polski";
            case "sv": return "Svenska";
            case "th": return "ไทย";
            default: return "Unknown";
        }
    }

    private void loadMessages() {
        messages.clear();

        switch (currentLanguage) {
            case "zh":
                loadChineseMessages();
                break;
            case "zht":
                loadTraditionalChineseMessages();
                break;
            case "ja":
                loadJapaneseMessages();
                break;
            case "fr":
                loadFrenchMessages();
                break;
            case "ru":
                loadRussianMessages();
                break;
            case "ko":
                loadKoreanMessages();
                break;
            case "es":
                loadSpanishMessages();
                break;
            case "de":
                loadGermanMessages();
                break;
            case "it":
                loadItalianMessages();
                break;
            case "pt":
                loadPortugueseMessages();
                break;
            case "ar":
                loadArabicMessages();
                break;
            case "hi":
                loadHindiMessages();
                break;
            case "tr":
                loadTurkishMessages();
                break;
            case "nl":
                loadDutchMessages();
                break;
            case "pl":
                loadPolishMessages();
                break;
            case "sv":
                loadSwedishMessages();
                break;
            case "th":
                loadThaiMessages();
                break;
            default:
                loadEnglishMessages();
                break;
        }
    }

    private void loadChineseMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro 插件正在加载中...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro 插件已成功启用！");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro 插件正在卸载...");
        messages.put("plugin.version", "§7    版本: §f");
        messages.put("plugin.author", "§7    分支作者: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    服务器Java版本: §f");
        messages.put("plugin.thanks", "§7    感谢使用本插件！");
        messages.put("plugin.bstats_enabled", "§b    ☁️ 云数据统计功能已启用！");
        messages.put("plugin.bstats_collecting", "§7    正在收集插件使用数据以优化体验...");
        messages.put("java.version_low", "§c❌ 检测到服务器Java版本低于14，插件将自动禁用！");
        messages.put("java.current_version", "§7服务器当前Java版本: §f");

        messages.put("command.no_permission", "§c您没有权限使用此命令！");
        messages.put("command.usage", "§c用法: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§a已重新加载CreeperConfettiPro配置！");
        messages.put("command.reset_success", "§a已恢复默认彩带效果！");
        messages.put("command.player_only", "§c只有玩家可以使用此命令！");
        messages.put("command.hold_firework", "§c请在主手中装备带有期望效果的烟花火箭。");
        messages.put("command.effect_set", "§a彩带效果现在设置为您主手中的烟花效果！");
        messages.put("command.language_set", "§a插件语言已设置为: ");
        messages.put("command.invalid_language", "§c无效的语言代码！可用选项: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§e当前插件语言: ");
    }

    private void loadTraditionalChineseMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro 插件正在加載中...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro 插件已成功啟用！");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro 插件正在卸載...");
        messages.put("plugin.version", "§7    版本: §f");
        messages.put("plugin.author", "§7    分支作者: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    服务器Java版本: §f");
        messages.put("plugin.thanks", "§7    感謝使用本插件！");
        messages.put("plugin.bstats_enabled", "§b    ☁️ 雲數據統計功能已啟用！");
        messages.put("plugin.bstats_collecting", "§7    正在收集插件使用數據以優化體驗...");
        messages.put("java.version_low", "§c❌ 檢測到服务器Java版本低於14，插件將自動禁用！");
        messages.put("java.current_version", "§7伺服器當前Java版本: §f");

        messages.put("command.no_permission", "§c您沒有權限使用此命令！");
        messages.put("command.usage", "§c用法: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§a已重新加載CreeperConfettiPro配置！");
        messages.put("command.reset_success", "§a已恢復默認彩帶效果！");
        messages.put("command.player_only", "§c只有玩家可以使用此命令！");
        messages.put("command.hold_firework", "§c請在主手中裝備帶有期望效果的煙花火箭。");
        messages.put("command.effect_set", "§a彩帶效果現在設置為您主手中的煙花效果！");
        messages.put("command.language_set", "§a插件語言已設置為: ");
        messages.put("command.invalid_language", "§c無效的語言代碼！可用選項: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§e當前插件語言: ");
    }

    private void loadJapaneseMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro プラグインを読み込んでいます...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro プラグインが正常に有効化されました！");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro プラグインを無効化しています...");
        messages.put("plugin.version", "§7    バージョン: §f");
        messages.put("plugin.author", "§7    作者: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    サーバーJavaバージョン: §f");
        messages.put("plugin.thanks", "§7    このプラグインをご利用いただきありがとうございます！");
        messages.put("plugin.bstats_enabled", "§b    ☁️ クラウド統計機能が有効になりました！");
        messages.put("plugin.bstats_collecting", "§7    プラグイン使用データを収集してエクスペリエンスを最適化しています...");
        messages.put("java.version_low", "§c❌ サーバーJavaバージョンが14未満を検出しました。プラグインは自動的に無効になります！");
        messages.put("java.current_version", "§7サーバーの現在のJavaバージョン: §f");

        messages.put("command.no_permission", "§cこのコマンドを使用する権限がありません！");
        messages.put("command.usage", "§c使用法: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiProの設定を再読み込みしました！");
        messages.put("command.reset_success", "§aデフォルトの紙吹雪効果を復元しました！");
        messages.put("command.player_only", "§cプレイヤーのみがこのコマンドを使用できます！");
        messages.put("command.hold_firework", "§cメインハンドに希望の効果を持つ花火ロケットを装備してください。");
        messages.put("command.effect_set", "§a紙吹雪効果がメインハンドの花火に設定されました！");
        messages.put("command.language_set", "§aプラグイン言語が設定されました: ");
        messages.put("command.invalid_language", "§c無効な言語コードです！利用可能なオプション: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§e現在のプラグイン言語: ");
    }

    private void loadFrenchMessages() {
        messages.put("plugin.loading", "§6    Le plugin CreeperConfettiPro est en cours de chargement...");
        messages.put("plugin.enabled", "§a    Le plugin CreeperConfettiPro a été activé avec succès !");
        messages.put("plugin.disabled", "§c    Le plugin CreeperConfettiPro est en cours de désactivation...");
        messages.put("plugin.version", "§7    Version: §f");
        messages.put("plugin.author", "§7    Auteur: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Version Java du serveur: §f");
        messages.put("plugin.thanks", "§7    Merci d'utiliser ce plugin !");
        messages.put("plugin.bstats_enabled", "§b    ☁️ La fonction de statistiques cloud est activée !");
        messages.put("plugin.bstats_collecting", "§7    Collecte des données d'utilisation du plugin pour optimiser l'expérience...");
        messages.put("java.version_low", "§c❌ Version Java du serveur inférieure à 14 détectée, le plugin sera automatiquement désactivé !");
        messages.put("java.current_version", "§7Version Java actuelle du serveur: §f");

        messages.put("command.no_permission", "§cVous n'avez pas la permission d'utiliser cette commande !");
        messages.put("command.usage", "§cUtilisation: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aConfiguration de CreeperConfettiPro rechargée !");
        messages.put("command.reset_success", "§aEffet confetti par défaut restauré !");
        messages.put("command.player_only", "§cSeuls les joueurs peuvent utiliser cette commande !");
        messages.put("command.hold_firework", "§cVeuillez tenir un feu d'artifice avec les effets souhaités dans votre main principale.");
        messages.put("command.effect_set", "§aL'effet confetti est maintenant défini sur le feu d'artifice dans votre main principale !");
        messages.put("command.language_set", "§aLa langue du plugin est définie sur: ");
        messages.put("command.invalid_language", "§cCode de langue invalide ! Options disponibles: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§eLangue actuelle du plugin: ");
    }

    private void loadRussianMessages() {
        messages.put("plugin.loading", "§6    Плагин CreeperConfettiPro загружается...");
        messages.put("plugin.enabled", "§a    Плагин CreeperConfettiPro успешно активирован!");
        messages.put("plugin.disabled", "§c    Плагин CreeperConfettiPro деактивируется...");
        messages.put("plugin.version", "§7    Версия: §f");
        messages.put("plugin.author", "§7    Автор: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Версия Java сервера: §f");
        messages.put("plugin.thanks", "§7    Спасибо за использование этого плагина!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Функция облачной статистики включена!");
        messages.put("plugin.bstats_collecting", "§7    Сбор данных об использовании плагина для оптимизации опыта...");
        messages.put("java.version_low", "§c❌ Обнаружена версия Java сервера ниже 14, плагин будет автоматически отключен!");
        messages.put("java.current_version", "§7Текущая версия Java сервера: §f");

        messages.put("command.no_permission", "§cУ вас нет разрешения на использование этой команды!");
        messages.put("command.usage", "§cИспользование: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aКонфигурация CreeperConfettiPro перезагружена!");
        messages.put("command.reset_success", "§aВосстановлен эффект конфетти по умолчанию!");
        messages.put("command.player_only", "§cТолько игроки могут использовать эту команду!");
        messages.put("command.hold_firework", "§cПожалуйста, держите фейерверк с желаемыми эффектами в основной руке.");
        messages.put("command.effect_set", "§aЭффект конфетти теперь установлен на фейерверк в вашей основной руке!");
        messages.put("command.language_set", "§aЯзык плагина установлен на: ");
        messages.put("command.invalid_language", "§cНеверный код языка! Доступные варианты: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§eТекущий язык плагина: ");
    }

    private void loadKoreanMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro 플러그인이 로드 중입니다...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro 플러그인이 성공적으로 활성화되었습니다!");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro 플러그인을 비활성화하는 중입니다...");
        messages.put("plugin.version", "§7    버전: §f");
        messages.put("plugin.author", "§7    작성자: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    서버 Java 버전: §f");
        messages.put("plugin.thanks", "§7    이 플러그인을 사용해 주셔서 감사합니다!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ 클라우드 통계 기능이 활성화되었습니다!");
        messages.put("plugin.bstats_collecting", "§7    경험을 최적화하기 위해 플러그인 사용 데이터를 수집하는 중입니다...");
        messages.put("java.version_low", "§c❌ 서버 Java 버전이 14 미만으로 감지되어 플러그인이 자동으로 비활성화됩니다!");
        messages.put("java.current_version", "§7서버의 현재 Java 버전: §f");

        messages.put("command.no_permission", "§c이 명령어를 사용할 권한이 없습니다!");
        messages.put("command.usage", "§c사용법: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiPro 설정이 다시 로드되었습니다!");
        messages.put("command.reset_success", "§a기본 컨페티 효과가 복원되었습니다!");
        messages.put("command.player_only", "§c플레이어만 이 명령어를 사용할 수 있습니다!");
        messages.put("command.hold_firework", "§c주요 손에 원하는 효과가 있는 폭죽 로켓을 장착해 주세요.");
        messages.put("command.effect_set", "§a컨페티 효과가 주요 손의 폭죽으로 설정되었습니다!");
        messages.put("command.language_set", "§a플러그인 언어가 설정되었습니다: ");
        messages.put("command.invalid_language", "§c잘못된 언어 코드입니다! 사용 가능한 옵션: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§e현재 플러그인 언어: ");
    }

    private void loadEnglishMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro plugin is loading...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro plugin enabled successfully!");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro plugin is unloading...");
        messages.put("plugin.version", "§7    Version: §f");
        messages.put("plugin.author", "§7    Author: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Server Java Version: §f");
        messages.put("plugin.thanks", "§7    Thank you for using this plugin!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Cloud statistics feature enabled!");
        messages.put("plugin.bstats_collecting", "§7    Collecting plugin usage data to optimize experience...");
        messages.put("java.version_low", "§c❌ Detected server Java version below 14, plugin will be disabled automatically!");
        messages.put("java.current_version", "§7Current server Java version: §f");

        messages.put("command.no_permission", "§cYou don't have permission to use this command!");
        messages.put("command.usage", "§cUsage: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiPro configuration reloaded!");
        messages.put("command.reset_success", "§aDefault confetti effect restored!");
        messages.put("command.player_only", "§cOnly players can use this command!");
        messages.put("command.hold_firework", "§cPlease hold a firework rocket with desired effects in your main hand.");
        messages.put("command.effect_set", "§aConfetti effect is now set to the firework in your main hand!");
        messages.put("command.language_set", "§aPlugin language set to: ");
        messages.put("command.invalid_language", "§cInvalid language code! Available options: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§eCurrent plugin language: ");
    }

    private void loadSpanishMessages() {
        messages.put("plugin.loading", "§6    El plugin CreeperConfettiPro se está cargando...");
        messages.put("plugin.enabled", "§a    ¡El plugin CreeperConfettiPro se ha habilitado correctamente!");
        messages.put("plugin.disabled", "§c    El plugin CreeperConfettiPro se está deshabilitando...");
        messages.put("plugin.version", "§7    Versión: §f");
        messages.put("plugin.author", "§7    Autor: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Versión Java del servidor: §f");
        messages.put("plugin.thanks", "§7    ¡Gracias por usar este plugin!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ ¡Función de estadísticas en la nube habilitada!");
        messages.put("plugin.bstats_collecting", "§7    Recopilando datos de uso del plugin para optimizar la experiencia...");
        messages.put("java.version_low", "§c❌ ¡Se detectó una versión de Java del servidor inferior a 14, el plugin se deshabilitará automáticamente!");
        messages.put("java.current_version", "§7Versión actual de Java del servidor: §f");

        messages.put("command.no_permission", "§c¡No tienes permiso para usar este comando!");
        messages.put("command.usage", "§cUso: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§a¡Configuración de CreeperConfettiPro recargada!");
        messages.put("command.reset_success", "§a¡Efecto de confeti predeterminado restaurado!");
        messages.put("command.player_only", "§c¡Solo los jugadores pueden usar este comando!");
        messages.put("command.hold_firework", "§cPor favor, sostén un cohete de fuegos artificiales con los efectos deseados en tu mano principal.");
        messages.put("command.effect_set", "§a¡El efecto de confeti ahora está configurado al cohete de fuegos artificiales en tu mano principal!");
        messages.put("command.language_set", "§aIdioma del plugin establecido en: ");
        messages.put("command.invalid_language", "§c¡Código de idioma inválido! Opciones disponibles: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§eIdioma actual del plugin: ");
    }

    private void loadGermanMessages() {
        messages.put("plugin.loading", "§6    Das CreeperConfettiPro-Plugin wird geladen...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro-Plugin erfolgreich aktiviert!");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro-Plugin wird deaktiviert...");
        messages.put("plugin.version", "§7    Version: §f");
        messages.put("plugin.author", "§7    Autor: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Server-Java-Version: §f");
        messages.put("plugin.thanks", "§7    Danke, dass Sie dieses Plugin verwenden!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Cloud-Statistikfunktion aktiviert!");
        messages.put("plugin.bstats_collecting", "§7    Sammeln von Plugin-Nutzungsdaten zur Optimierung der Erfahrung...");
        messages.put("java.version_low", "§c❌ Server-Java-Version unter 14 erkannt, Plugin wird automatisch deaktiviert!");
        messages.put("java.current_version", "§7Aktuelle Server-Java-Version: §f");

        messages.put("command.no_permission", "§cSie haben keine Berechtigung, diesen Befehl zu verwenden!");
        messages.put("command.usage", "§cVerwendung: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiPro-Konfiguration neu geladen!");
        messages.put("command.reset_success", "§aStandard-Confetti-Effekt wiederhergestellt!");
        messages.put("command.player_only", "§cNur Spieler können diesen Befehl verwenden!");
        messages.put("command.hold_firework", "§cBitte halten Sie eine Feuerwerksrakete mit den gewünschten Effekten in Ihrer Haupthand.");
        messages.put("command.effect_set", "§aConfetti-Effekt ist jetzt auf die Feuerwerksrakete in Ihrer Haupthand eingestellt!");
        messages.put("command.language_set", "§aPlugin-Sprache eingestellt auf: ");
        messages.put("command.invalid_language", "§cUngültiger Sprachcode! Verfügbare Optionen: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§eAktuelle Plugin-Sprache: ");
    }

    private void loadItalianMessages() {
        messages.put("plugin.loading", "§6    Il plugin CreeperConfettiPro si sta caricando...");
        messages.put("plugin.enabled", "§a    Plugin CreeperConfettiPro abilitato con successo!");
        messages.put("plugin.disabled", "§c    Il plugin CreeperConfettiPro si sta disabilitando...");
        messages.put("plugin.version", "§7    Versione: §f");
        messages.put("plugin.author", "§7    Autore: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Versione Java del server: §f");
        messages.put("plugin.thanks", "§7    Grazie per aver utilizzato questo plugin!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Funzione di statistiche cloud abilitata!");
        messages.put("plugin.bstats_collecting", "§7    Raccolta dei dati di utilizzo del plugin per ottimizzare l'esperienza...");
        messages.put("java.version_low", "§c❌ Rilevata versione Java del server inferiore a 14, il plugin verrà disabilitato automaticamente!");
        messages.put("java.current_version", "§7Versione Java corrente del server: §f");

        messages.put("command.no_permission", "§cNon hai il permesso di usare questo comando!");
        messages.put("command.usage", "§cUso: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aConfigurazione CreeperConfettiPro ricaricata!");
        messages.put("command.reset_success", "§aEffetto confetti predefinito ripristinato!");
        messages.put("command.player_only", "§cSolo i giocatori possono usare questo comando!");
        messages.put("command.hold_firework", "§cPer favore, tieni un razzo pirotecnico con gli effetti desiderati nella tua mano principale.");
        messages.put("command.effect_set", "§aL'effetto confetti è ora impostato sul razzo pirotecnico nella tua mano principale!");
        messages.put("command.language_set", "§aLingua del plugin impostata su: ");
        messages.put("command.invalid_language", "§cCodice lingua non valido! Opzioni disponibili: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§eLingua attuale del plugin: ");
    }

    private void loadPortugueseMessages() {
        messages.put("plugin.loading", "§6    O plugin CreeperConfettiPro está carregando...");
        messages.put("plugin.enabled", "§a    Plugin CreeperConfettiPro ativado com sucesso!");
        messages.put("plugin.disabled", "§c    O plugin CreeperConfettiPro está sendo desativado...");
        messages.put("plugin.version", "§7    Versão: §f");
        messages.put("plugin.author", "§7    Autor: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Versão Java do servidor: §f");
        messages.put("plugin.thanks", "§7    Obrigado por usar este plugin!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Função de estatísticas na nuvem ativada!");
        messages.put("plugin.bstats_collecting", "§7    Coletando dados de uso do plugin para otimizar a experiência...");
        messages.put("java.version_low", "§c❌ Versão Java do servidor abaixo de 14 detectada, o plugin será desativado automaticamente!");
        messages.put("java.current_version", "§7Versão Java atual do servidor: §f");

        messages.put("command.no_permission", "§cVocê não tem permissão para usar este comando!");
        messages.put("command.usage", "§cUso: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aConfiguração do CreeperConfettiPro recarregada!");
        messages.put("command.reset_success", "§aEfeito de confete padrão restaurado!");
        messages.put("command.player_only", "§cApenas jogadores podem usar este comando!");
        messages.put("command.hold_firework", "§cPor favor, segure um foguete de fogos de artifício com os efeitos desejados na sua mão principal.");
        messages.put("command.effect_set", "§aO efeito de confete agora está definido para o foguete de fogos de artifício na sua mão principal!");
        messages.put("command.language_set", "§aIdioma do plugin definido para: ");
        messages.put("command.invalid_language", "§cCódigo de idioma inválido! Opções disponíveis: zh, zht, ja, fr, ru, ko, en, es, de, it, pt");
        messages.put("command.current_language", "§eIdioma atual do plugin: ");
    }

    private void loadArabicMessages() {
        messages.put("plugin.loading", "§6    جاري تحميل إضافة CreeperConfettiPro...");
        messages.put("plugin.enabled", "§a    تم تفعيل إضافة CreeperConfettiPro بنجاح!");
        messages.put("plugin.disabled", "§c    جاري تعطيل إضافة CreeperConfettiPro...");
        messages.put("plugin.version", "§7    الإصدار: §f");
        messages.put("plugin.author", "§7    المؤلف: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    إصدار جافا للخادم: §f");
        messages.put("plugin.thanks", "§7    شكراً لاستخدامك هذه الإضافة!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ تم تفعيل ميزة إحصائيات السحابة!");
        messages.put("plugin.bstats_collecting", "§7    جاري جمع بيانات استخدام الإضافة لتحسين التجربة...");
        messages.put("java.version_low", "§c❌ تم اكتشاف إصدار جافا للخادم أقل من 14، سيتم تعطيل الإضافة تلقائياً!");
        messages.put("java.current_version", "§7إصدار جافا الحالي للخادم: §f");

        messages.put("command.no_permission", "§cليس لديك الصلاحية لاستخدام هذا الأمر!");
        messages.put("command.usage", "§cالاستخدام: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aتم إعادة تحميل إعدادات CreeperConfettiPro!");
        messages.put("command.reset_success", "§aتم استعادة تأثير الورق الملون الافتراضي!");
        messages.put("command.player_only", "§cيمكن للاعبين فقط استخدام هذا الأمر!");
        messages.put("command.hold_firework", "§cيرجى_HOLD_-held_rocket_بالتأثيرات_المطلوبة في يدك الرئيسية.");
        messages.put("command.effect_set", "§aتم تعيين تأثير الورق الملون الآن إلى القذيفة النارية في يدك الرئيسية!");
        messages.put("command.language_set", "§aتم تعيين لغة الإضافة إلى: ");
        messages.put("command.invalid_language", "§cرمز اللغة غير صالح! الخيارات المتاحة: zh, zht, ja, fr, ru, ko, en, es, de, it, pt, ar, hi, tr, nl, pl, sv, th");
        messages.put("command.current_language", "§eلغة الإضافة الحالية: ");
    }

    private void loadHindiMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro प्लगइन लोड हो रहा है...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro प्लगइन सफलतापूर्वक सक्षम किया गया!");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro प्लगइन अक्षम हो रहा है...");
        messages.put("plugin.version", "§7    संस्करण: §f");
        messages.put("plugin.author", "§7    लेखक: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    सर्वर जावा संस्करण: §f");
        messages.put("plugin.thanks", "§7    इस प्लगइन का उपयोग करने के लिए धन्यवाद!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ क्लाउड सांख्यिकी सुविधा सक्षम की गई!");
        messages.put("plugin.bstats_collecting", "§7    अनुभव को अनुकूलित करने के लिए प्लगइन उपयोग डेटा एकत्र किया जा रहा है...");
        messages.put("java.version_low", "§c❌ सर्वर जावा संस्करण 14 से कम पाया गया, प्लगइन स्वचालित रूप से अक्षम कर दिया जाएगा!");
        messages.put("java.current_version", "§7वर्तमान सर्वर जावा संस्करण: §f");

        messages.put("command.no_permission", "§cआपके पास इस आदेश का उपयोग करने की अनुमति नहीं है!");
        messages.put("command.usage", "§cउपयोग: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiPro विन्यास पुनः लोड किया गया!");
        messages.put("command.reset_success", "§aडिफ़ॉल्ट कॉन्फेटी प्रभाव पुनर्स्थापित किया गया!");
        messages.put("command.player_only", "§cकेवल खिलाड़ी ही इस आदेश का उपयोग कर सकते हैं!");
        messages.put("command.hold_firework", "§cकृपया अपने मुख्य हाथ में वांछित प्रभावों वाला फायरवर्क रॉकेट धारण करें।");
        messages.put("command.effect_set", "§aकॉन्फेटी प्रभाव अब आपके मुख्य हाथ में फायरवर्क पर सेट है!");
        messages.put("command.language_set", "§aप्लगइन भाषा सेट की गई: ");
        messages.put("command.invalid_language", "§cअमान्य भाषा कोड! उपलब्ध विकल्प: zh, zht, ja, fr, ru, ko, en, es, de, it, pt, ar, hi, tr, nl, pl, sv, th");
        messages.put("command.current_language", "§eवर्तमान प्लगइन भाषा: ");
    }

    private void loadTurkishMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro eklentisi yükleniyor...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro eklentisi başarıyla etkinleştirildi!");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro eklentisi devre dışı bırakılıyor...");
        messages.put("plugin.version", "§7    Sürüm: §f");
        messages.put("plugin.author", "§7    Yazar: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Sunucu Java Sürümü: §f");
        messages.put("plugin.thanks", "§7    Bu eklentiyi kullandığınız için teşekkürler!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Bulut istatistikleri özelliği etkinleştirildi!");
        messages.put("plugin.bstats_collecting", "§7    Deneyimi optimize etmek için eklenti kullanım verileri toplanıyor...");
        messages.put("java.version_low", "§c❌ Sunucu Java sürümü 14'ün altında algılandı, eklenti otomatik olarak devre dışı bırakılacak!");
        messages.put("java.current_version", "§7Geçerli sunucu Java sürümü: §f");

        messages.put("command.no_permission", "§cBu komutu kullanma izniniz yok!");
        messages.put("command.usage", "§cKullanım: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiPro yapılandırması yeniden yüklendi!");
        messages.put("command.reset_success", "§aVarsayılan konfeti efekti geri yüklendi!");
        messages.put("command.player_only", "§cSadece oyuncular bu komutu kullanabilir!");
        messages.put("command.hold_firework", "§cLütfen ana elinizde istediğiniz efektleri içeren bir havai fişek roketi tutun.");
        messages.put("command.effect_set", "§aKonfeti efekti artık ana elinizdeki havai fişek roketine ayarlandı!");
        messages.put("command.language_set", "§aEklenti dili şu şekilde ayarlandı: ");
        messages.put("command.invalid_language", "§cGeçersiz dil kodu! Kullanılabilir seçenekler: zh, zht, ja, fr, ru, ko, en, es, de, it, pt, ar, hi, tr, nl, pl, sv, th");
        messages.put("command.current_language", "§eMevcut eklenti dili: ");
    }

    private void loadDutchMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro plugin wordt geladen...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro plugin succesvol geactiveerd!");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro plugin wordt uitgeschakeld...");
        messages.put("plugin.version", "§7    Versie: §f");
        messages.put("plugin.author", "§7    Auteur: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Server Java-versie: §f");
        messages.put("plugin.thanks", "§7    Bedankt voor het gebruik van deze plugin!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Cloud-statistiekenfunctie ingeschakeld!");
        messages.put("plugin.bstats_collecting", "§7    Plugin-gebruiksgegevens verzamelen om de ervaring te optimaliseren...");
        messages.put("java.version_low", "§c❌ Server Java-versie onder 14 gedetecteerd, plugin wordt automatisch uitgeschakeld!");
        messages.put("java.current_version", "§7Huidige server Java-versie: §f");

        messages.put("command.no_permission", "§cJe hebt geen toestemming om dit commando te gebruiken!");
        messages.put("command.usage", "§cGebruik: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiPro configuratie herladen!");
        messages.put("command.reset_success", "§aStandaard confetti-effect hersteld!");
        messages.put("command.player_only", "§cAlleen spelers kunnen dit commando gebruiken!");
        messages.put("command.hold_firework", "§cHoud alstublieft een vuurwerk raket met de gewenste effecten in je hoofdhand.");
        messages.put("command.effect_set", "§aConfetti-effect is nu ingesteld op het vuurwerk in je hoofdhand!");
        messages.put("command.language_set", "§aPlugin-taal ingesteld op: ");
        messages.put("command.invalid_language", "§cOngeldige taalcode! Beschikbare opties: zh, zht, ja, fr, ru, ko, en, es, de, it, pt, ar, hi, tr, nl, pl, sv, th");
        messages.put("command.current_language", "§eHuidige plugin-taal: ");
    }

    private void loadPolishMessages() {
        messages.put("plugin.loading", "§6    Wtyczka CreeperConfettiPro się ładuje...");
        messages.put("plugin.enabled", "§a    Wtyczka CreeperConfettiPro została pomyślnie aktywowana!");
        messages.put("plugin.disabled", "§c    Wtyczka CreeperConfettiPro jest wyłączana...");
        messages.put("plugin.version", "§7    Wersja: §f");
        messages.put("plugin.author", "§7    Autor: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Wersja Javy serwera: §f");
        messages.put("plugin.thanks", "§7    Dziękujemy za korzystanie z tej wtyczki!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Funkcja statystyk chmurowych została włączona!");
        messages.put("plugin.bstats_collecting", "§7    Zbieranie danych użytkowania wtyczki w celu optymalizacji doświadczenia...");
        messages.put("java.version_low", "§c❌ Wykryto wersję Javy serwera poniżej 14, wtyczka zostanie automatycznie wyłączona!");
        messages.put("java.current_version", "§7Aktualna wersja Javy serwera: §f");

        messages.put("command.no_permission", "§cNie masz uprawnień do użycia tego polecenia!");
        messages.put("command.usage", "§cUżycie: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aKonfiguracja CreeperConfettiPro została ponownie załadowana!");
        messages.put("command.reset_success", "§aPrzywrócono domyślny efekt konfetti!");
        messages.put("command.player_only", "§cTylko gracze mogą używać tego polecenia!");
        messages.put("command.hold_firework", "§cProszę trzymać rakietę fajerwerkową z pożądanymi efektami w głównej ręce.");
        messages.put("command.effect_set", "§aEfekt konfetti jest teraz ustawiony na fajerwerki w twojej głównej ręce!");
        messages.put("command.language_set", "§aJęzyk wtyczki został ustawiony na: ");
        messages.put("command.invalid_language", "§cNieprawidłowy kod języka! Dostępne opcje: zh, zht, ja, fr, ru, ko, en, es, de, it, pt, ar, hi, tr, nl, pl, sv, th");
        messages.put("command.current_language", "§eAktualny język wtyczki: ");
    }

    private void loadSwedishMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro-pluginen laddas...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro-pluginen aktiverades framgångsrikt!");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro-pluginen inaktiveras...");
        messages.put("plugin.version", "§7    Version: §f");
        messages.put("plugin.author", "§7    Författare: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Serverns Java-version: §f");
        messages.put("plugin.thanks", "§7    Tack för att du använder detta plugin!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Molnstatistikfunktionen är aktiverad!");
        messages.put("plugin.bstats_collecting", "§7    Samlar in pluginanvändningsdata för att optimera upplevelsen...");
        messages.put("java.version_low", "§c❌ Serverns Java-version under 14 upptäckt, pluginen kommer att inaktiveras automatiskt!");
        messages.put("java.current_version", "§7Nuvarande serverns Java-version: §f");

        messages.put("command.no_permission", "§cDu har inte behörighet att använda detta kommando!");
        messages.put("command.usage", "§cAnvändning: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiPro-konfigurationen har laddats om!");
        messages.put("command.reset_success", "§aStandardkonfettieffekten har återställts!");
        messages.put("command.player_only", "§cEndast spelare kan använda detta kommando!");
        messages.put("command.hold_firework", "§cHåll en fyrverkeriraket med önskade effekter i din huvudhand.");
        messages.put("command.effect_set", "§aKonfettieffekten är nu inställd på fyrverkeriet i din huvudhand!");
        messages.put("command.language_set", "§aPlugin-språket är inställt på: ");
        messages.put("command.invalid_language", "§cOgiltig språkkod! Tillgängliga alternativ: zh, zht, ja, fr, ru, ko, en, es, de, it, pt, ar, hi, tr, nl, pl, sv, th");
        messages.put("command.current_language", "§eNuvarande plugin-språk: ");
    }

    private void loadThaiMessages() {
        messages.put("plugin.loading", "§6    กำลังโหลดปลั๊กอิน CreeperConfettiPro...");
        messages.put("plugin.enabled", "§a    เปิดใช้งานปลั๊กอิน CreeperConfettiPro สำเร็จแล้ว!");
        messages.put("plugin.disabled", "§c    กำลังปิดใช้งานปลั๊กอิน CreeperConfettiPro...");
        messages.put("plugin.version", "§7    เวอร์ชัน: §f");
        messages.put("plugin.author", "§7    ผู้เขียน: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    เวอร์ชัน Java ของเซิร์ฟเวอร์: §f");
        messages.put("plugin.thanks", "§7    ขอบคุณที่ใช้ปลั๊กอินนี้!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ เปิดใช้งานคุณสมบัติสถิติบนคลาวด์แล้ว!");
        messages.put("plugin.bstats_collecting", "§7    กำลังรวบรวมข้อมูลการใช้งานปลั๊กอินเพื่อปรับปรุงประสบการณ์...");
        messages.put("java.version_low", "§c❌ ตรวจพบเวอร์ชัน Java ของเซิร์ฟเวอร์ต่ำกว่า 14 ปลั๊กอินจะถูกปิดใช้งานโดยอัตโนมัติ!");
        messages.put("java.current_version", "§7เวอร์ชัน Java ปัจจุบันของเซิร์ฟเวอร์: §f");

        messages.put("command.no_permission", "§cคุณไม่มีสิทธิ์ในการใช้คำสั่งนี้!");
        messages.put("command.usage", "§cการใช้งาน: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aโหลดการกำหนดค่า CreeperConfettiPro ใหม่แล้ว!");
        messages.put("command.reset_success", "§aกู้คืนเอฟเฟกต์คอนเฟตตี้เริ่มต้นแล้ว!");
        messages.put("command.player_only", "§cเฉพาะผู้เล่นเท่านั้นที่สามารถใช้คำสั่งนี้ได้!");
        messages.put("command.hold_firework", "§cโปรดถือดอกไม้ไฟที่มีเอฟเฟกต์ที่ต้องการในมือหลักของคุณ");
        messages.put("command.effect_set", "§เอฟเฟกต์คอนเฟตตี้ได้รับการตั้งค่าเป็นดอกไม้ไฟในมือหลักของคุณแล้ว!");
        messages.put("command.language_set", "§aตั้งค่าภาษาปลั๊กอินเป็น: ");
        messages.put("command.invalid_language", "§cรหัสภาษาไม่ถูกต้อง! ตัวเลือกที่มี: zh, zht, ja, fr, ru, ko, en, es, de, it, pt, ar, hi, tr, nl, pl, sv, th");
        messages.put("command.current_language", "§eภาษาปลั๊กอินปัจจุบัน: ");
    }

    public String getMessage(String key) {
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
        detectLanguage();
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
}
