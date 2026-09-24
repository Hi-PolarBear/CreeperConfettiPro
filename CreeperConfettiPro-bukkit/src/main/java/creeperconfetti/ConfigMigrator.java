package creeperconfetti;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import creeperconfetti.effects.FireworkEffectParser;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Handles configuration version tracking and migration.
 *
 * <p>Rules:
 * <ul>
 *     <li>When the stored {@code config-version} is up to date nothing happens.</li>
 *     <li>When upgrading from an older version the existing file is backed up to
 *     {@code backups/config-backup-<timestamp>.yml} and new configuration keys are
 *     merged in - user values are always preserved.</li>
 *     <li>Legacy single-effect configs are converted into the new style layout.</li>
 * </ul>
 */
public class ConfigMigrator {

    public static final int CURRENT_VERSION = 2;

    private static final String VERSION_PATH = "config-version";
    private static final String STYLES_PATH = "firework_styles";
    private static final String LEGACY_EFFECT_PATH = "confetti_effect";

    private final CreeperConfettiPro plugin;

    public ConfigMigrator(CreeperConfettiPro plugin) {
        this.plugin = plugin;
    }

    public void migrate() {
        FileConfiguration config = plugin.getConfig();
        int version = config.getInt(VERSION_PATH, 0);

        if (version >= CURRENT_VERSION) {
            return;
        }

        plugin.getLogger().info("[Config] Detected configuration version " + version
                + ", migrating to version " + CURRENT_VERSION + "...");
        backup();

        if (version == 0) {
            migrateLegacy(config);
        }

        mergeMissingDefaults(config);
        config.set(VERSION_PATH, CURRENT_VERSION);
        plugin.saveConfig();
        plugin.getLogger().info("[Config] Migration complete. A backup was saved in the backups/ folder.");
    }

    /**
     * Converts the legacy single effect list into one named style, keeping the
     * user's original effect data.
     */
    private void migrateLegacy(FileConfiguration config) {
        ConfigurationSection styles = config.getConfigurationSection(STYLES_PATH);
        boolean hasStyles = styles != null && !styles.getKeys(false).isEmpty();
        if (hasStyles) {
            return;
        }

        List<FireworkEffect> legacy = FireworkEffectParser.parseEffects(config.get(LEGACY_EFFECT_PATH));
        if (!legacy.isEmpty()) {
            config.set(STYLES_PATH + ".default", legacy);
            plugin.getLogger().info("[Config] Migrated legacy 'confetti_effect' into style 'default'.");
        }
    }

    /**
     * Copies every key that exists in the packaged default config but is missing
     * from the user config, without touching existing values.
     */
    private void mergeMissingDefaults(FileConfiguration config) {
        InputStream input = plugin.getResource("config.yml");
        if (input == null) {
            return;
        }

        YamlConfiguration defaults;
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            defaults = YamlConfiguration.loadConfiguration(reader);
        } catch (Exception exception) {
            plugin.getLogger().warning("[Config] Unable to read default config: " + exception.getMessage());
            return;
        }

        mergeSection(config, defaults, "");
    }

    private void mergeSection(FileConfiguration target, ConfigurationSection defaults, String path) {
        for (String key : defaults.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (defaults.isConfigurationSection(key)) {
                if (!target.isConfigurationSection(fullPath)) {
                    target.createSection(fullPath);
                }
                mergeSection(target, defaults.getConfigurationSection(key), fullPath);
            } else if (!target.isSet(fullPath)) {
                target.set(fullPath, defaults.get(key));
            }
        }
    }

    /**
     * Backs up the current config file before it is modified.
     */
    private void backup() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }

        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            plugin.getLogger().warning("[Config] Unable to create backups folder, skipping backup.");
            return;
        }

        String name = "config-backup-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".yml";
        try {
            Files.copy(configFile.toPath(), new File(backupDir, name).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            plugin.getLogger().warning("[Config] Failed to create config backup: " + exception.getMessage());
        }
    }
}
