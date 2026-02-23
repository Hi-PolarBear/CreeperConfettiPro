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

    private static final String IP_API_URL = "http://ip-api.com/json/?fields=countryCode";

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public void initialize() {
        initialize(null);
    }

    public void initialize(Runnable callback) {
        this.onInitializedCallback = callback;
        detectLanguage();
    }

    private void detectLanguage() {
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
            CreeperConfettiPro.getInstance().getLogger().info("检测到服务器地区代码: " + countryCode + ", 使用语言: " + languageName);
            
            loadMessages();
            initialized = true;
            
            if (onInitializedCallback != null) {
                onInitializedCallback.run();
            }
        });
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
        messages.put("plugin.java_version", "§7    Java版本: §f");
        messages.put("plugin.thanks", "§7    感谢使用本插件！");
        messages.put("plugin.bstats_enabled", "§b    ☁️ 云数据统计功能已启用！");
        messages.put("plugin.bstats_collecting", "§7    正在收集插件使用数据以优化体验...");
        messages.put("java.version_low", "§c❌ 检测到Java版本低于14，插件将自动禁用！");
        messages.put("java.current_version", "§7服务器当前Java版本: §f");

        messages.put("command.no_permission", "§c您没有权限使用此命令！");
        messages.put("command.usage", "§c用法: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§a已重新加载CreeperConfettiPro配置！");
        messages.put("command.reset_success", "§a已恢复默认彩带效果！");
        messages.put("command.player_only", "§c只有玩家可以使用此命令！");
        messages.put("command.hold_firework", "§c请在主手中装备带有期望效果的烟花火箭。");
        messages.put("command.effect_set", "§a彩带效果现在设置为您主手中的烟花效果！");
    }

    private void loadTraditionalChineseMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro 插件正在加載中...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro 插件已成功啟用！");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro 插件正在卸載...");
        messages.put("plugin.version", "§7    版本: §f");
        messages.put("plugin.author", "§7    分支作者: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Java版本: §f");
        messages.put("plugin.thanks", "§7    感謝使用本插件！");
        messages.put("plugin.bstats_enabled", "§b    ☁️ 雲數據統計功能已啟用！");
        messages.put("plugin.bstats_collecting", "§7    正在收集插件使用數據以優化體驗...");
        messages.put("java.version_low", "§c❌ 檢測到Java版本低於14，插件將自動禁用！");
        messages.put("java.current_version", "§7伺服器當前Java版本: §f");

        messages.put("command.no_permission", "§c您沒有權限使用此命令！");
        messages.put("command.usage", "§c用法: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§a已重新加載CreeperConfettiPro配置！");
        messages.put("command.reset_success", "§a已恢復默認彩帶效果！");
        messages.put("command.player_only", "§c只有玩家可以使用此命令！");
        messages.put("command.hold_firework", "§c請在主手中裝備帶有期望效果的煙花火箭。");
        messages.put("command.effect_set", "§a彩帶效果現在設置為您主手中的煙花效果！");
    }

    private void loadJapaneseMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro プラグインを読み込んでいます...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro プラグインが正常に有効化されました！");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro プラグインを無効化しています...");
        messages.put("plugin.version", "§7    バージョン: §f");
        messages.put("plugin.author", "§7    作者: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Javaバージョン: §f");
        messages.put("plugin.thanks", "§7    このプラグインをご利用いただきありがとうございます！");
        messages.put("plugin.bstats_enabled", "§b    ☁️ クラウド統計機能が有効になりました！");
        messages.put("plugin.bstats_collecting", "§7    プラグイン使用データを収集してエクスペリエンスを最適化しています...");
        messages.put("java.version_low", "§c❌ Javaバージョンが14未満を検出しました。プラグインは自動的に無効になります！");
        messages.put("java.current_version", "§7サーバーの現在のJavaバージョン: §f");

        messages.put("command.no_permission", "§cこのコマンドを使用する権限がありません！");
        messages.put("command.usage", "§c使用法: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiProの設定を再読み込みしました！");
        messages.put("command.reset_success", "§aデフォルトの紙吹雪効果を復元しました！");
        messages.put("command.player_only", "§cプレイヤーのみがこのコマンドを使用できます！");
        messages.put("command.hold_firework", "§cメインハンドに希望の効果を持つ花火ロケットを装備してください。");
        messages.put("command.effect_set", "§a紙吹雪効果がメインハンドの花火に設定されました！");
    }

    private void loadFrenchMessages() {
        messages.put("plugin.loading", "§6    Le plugin CreeperConfettiPro est en cours de chargement...");
        messages.put("plugin.enabled", "§a    Le plugin CreeperConfettiPro a été activé avec succès !");
        messages.put("plugin.disabled", "§c    Le plugin CreeperConfettiPro est en cours de désactivation...");
        messages.put("plugin.version", "§7    Version: §f");
        messages.put("plugin.author", "§7    Auteur: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Version Java: §f");
        messages.put("plugin.thanks", "§7    Merci d'utiliser ce plugin !");
        messages.put("plugin.bstats_enabled", "§b    ☁️ La fonction de statistiques cloud est activée !");
        messages.put("plugin.bstats_collecting", "§7    Collecte des données d'utilisation du plugin pour optimiser l'expérience...");
        messages.put("java.version_low", "§c❌ Version Java inférieure à 14 détectée, le plugin sera automatiquement désactivé !");
        messages.put("java.current_version", "§7Version Java actuelle du serveur: §f");

        messages.put("command.no_permission", "§cVous n'avez pas la permission d'utiliser cette commande !");
        messages.put("command.usage", "§cUtilisation: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aConfiguration de CreeperConfettiPro rechargée !");
        messages.put("command.reset_success", "§aEffet confetti par défaut restauré !");
        messages.put("command.player_only", "§cSeuls les joueurs peuvent utiliser cette commande !");
        messages.put("command.hold_firework", "§cVeuillez tenir un feu d'artifice avec les effets souhaités dans votre main principale.");
        messages.put("command.effect_set", "§aL'effet confetti est maintenant défini sur le feu d'artifice dans votre main principale !");
    }

    private void loadRussianMessages() {
        messages.put("plugin.loading", "§6    Плагин CreeperConfettiPro загружается...");
        messages.put("plugin.enabled", "§a    Плагин CreeperConfettiPro успешно активирован!");
        messages.put("plugin.disabled", "§c    Плагин CreeperConfettiPro деактивируется...");
        messages.put("plugin.version", "§7    Версия: §f");
        messages.put("plugin.author", "§7    Автор: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Версия Java: §f");
        messages.put("plugin.thanks", "§7    Спасибо за использование этого плагина!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Функция облачной статистики включена!");
        messages.put("plugin.bstats_collecting", "§7    Сбор данных об использовании плагина для оптимизации опыта...");
        messages.put("java.version_low", "§c❌ Обнаружена версия Java ниже 14, плагин будет автоматически отключен!");
        messages.put("java.current_version", "§7Текущая версия Java сервера: §f");

        messages.put("command.no_permission", "§cУ вас нет разрешения на использование этой команды!");
        messages.put("command.usage", "§cИспользование: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aКонфигурация CreeperConfettiPro перезагружена!");
        messages.put("command.reset_success", "§aВосстановлен эффект конфетти по умолчанию!");
        messages.put("command.player_only", "§cТолько игроки могут использовать эту команду!");
        messages.put("command.hold_firework", "§cПожалуйста, держите фейерверк с желаемыми эффектами в основной руке.");
        messages.put("command.effect_set", "§aЭффект конфетти теперь установлен на фейерверк в вашей основной руке!");
    }

    private void loadKoreanMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro 플러그인이 로드 중입니다...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro 플러그인이 성공적으로 활성화되었습니다!");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro 플러그인을 비활성화하는 중입니다...");
        messages.put("plugin.version", "§7    버전: §f");
        messages.put("plugin.author", "§7    작성자: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Java 버전: §f");
        messages.put("plugin.thanks", "§7    이 플러그인을 사용해 주셔서 감사합니다!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ 클라우드 통계 기능이 활성화되었습니다!");
        messages.put("plugin.bstats_collecting", "§7    경험을 최적화하기 위해 플러그인 사용 데이터를 수집하는 중입니다...");
        messages.put("java.version_low", "§c❌ Java 버전이 14 미만으로 감지되어 플러그인이 자동으로 비활성화됩니다!");
        messages.put("java.current_version", "§7서버의 현재 Java 버전: §f");

        messages.put("command.no_permission", "§c이 명령어를 사용할 권한이 없습니다!");
        messages.put("command.usage", "§c사용법: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiPro 설정이 다시 로드되었습니다!");
        messages.put("command.reset_success", "§a기본 컨페티 효과가 복원되었습니다!");
        messages.put("command.player_only", "§c플레이어만 이 명령어를 사용할 수 있습니다!");
        messages.put("command.hold_firework", "§c주요 손에 원하는 효과가 있는 폭죽 로켓을 장착해 주세요.");
        messages.put("command.effect_set", "§a컨페티 효과가 주요 손의 폭죽으로 설정되었습니다!");
    }

    private void loadEnglishMessages() {
        messages.put("plugin.loading", "§6    CreeperConfettiPro plugin is loading...");
        messages.put("plugin.enabled", "§a    CreeperConfettiPro plugin enabled successfully!");
        messages.put("plugin.disabled", "§c    CreeperConfettiPro plugin is unloading...");
        messages.put("plugin.version", "§7    Version: §f");
        messages.put("plugin.author", "§7    Author: §dNice_Cam_");
        messages.put("plugin.java_version", "§7    Java Version: §f");
        messages.put("plugin.thanks", "§7    Thank you for using this plugin!");
        messages.put("plugin.bstats_enabled", "§b    ☁️ Cloud statistics feature enabled!");
        messages.put("plugin.bstats_collecting", "§7    Collecting plugin usage data to optimize experience...");
        messages.put("java.version_low", "§c❌ Detected Java version below 14, plugin will be disabled automatically!");
        messages.put("java.current_version", "§7Current server Java version: §f");

        messages.put("command.no_permission", "§cYou don't have permission to use this command!");
        messages.put("command.usage", "§cUsage: /creeperconfetti <reload | reseteffect | seteffect | reloadlanguage>");
        messages.put("command.reload_success", "§aCreeperConfettiPro configuration reloaded!");
        messages.put("command.reset_success", "§aDefault confetti effect restored!");
        messages.put("command.player_only", "§cOnly players can use this command!");
        messages.put("command.hold_firework", "§cPlease hold a firework rocket with desired effects in your main hand.");
        messages.put("command.effect_set", "§aConfetti effect is now set to the firework in your main hand!");
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, key);
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void reloadLanguage(Runnable callback) {
        this.onInitializedCallback = callback;
        initialized = false;
        detectLanguage();
    }
}
