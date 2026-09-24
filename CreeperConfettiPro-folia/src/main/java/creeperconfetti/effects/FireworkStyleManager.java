package creeperconfetti.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import creeperconfetti.CreeperConfettiPro;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Loads, stores and persists the configured firework styles and their trigger
 * chances. Every style may contain one or more effects; when a creeper is
 * "confetti-fied" a weighted random style is picked and played.
 *
 * <p>The per-style chances are always auto-compensated so that they sum up to
 * exactly 100% - no matter what the user configured.
 */
public class FireworkStyleManager {

    private static final String STYLES_PATH = "firework_styles";
    private static final String CHANCES_PATH = "firework_chances";
    private static final String LEGACY_PATH = "confetti_effect";

    private final CreeperConfettiPro plugin;
    private final Map<String, List<FireworkEffect>> styles = new LinkedHashMap<>();
    private final Map<String, Double> chances = new LinkedHashMap<>();

    public FireworkStyleManager(CreeperConfettiPro plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads the styles and chances from the plugin configuration.
     */
    public synchronized void load() {
        styles.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection(STYLES_PATH);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                List<FireworkEffect> effects = FireworkEffectParser.parseEffects(section.get(key));
                if (!effects.isEmpty()) {
                    styles.put(key, effects);
                }
            }
        }

        if (styles.isEmpty()) {
            List<FireworkEffect> legacy = FireworkEffectParser.parseEffects(plugin.getConfig().get(LEGACY_PATH));
            if (!legacy.isEmpty()) {
                styles.put("default", legacy);
            }
        }

        if (styles.isEmpty()) {
            styles.putAll(builtInDefaults());
        }

        loadChances();
        rebalance();
    }

    private void loadChances() {
        chances.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection(CHANCES_PATH);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String styleKey = findKey(key);
                if (styleKey != null) {
                    chances.put(styleKey, Math.max(0.0D, section.getDouble(key)));
                }
            }
        }

        // Styles without an explicit chance receive the average of the others.
        double total = 0.0D;
        int defined = 0;
        for (double value : chances.values()) {
            if (value > 0) {
                total += value;
                defined++;
            }
        }
        double fallback = defined > 0 ? total / defined : 1.0D;
        for (String name : styles.keySet()) {
            if (!chances.containsKey(name)) {
                chances.put(name, fallback);
            }
        }
    }

    /**
     * Persists the current styles and chances to disk.
     */
    public synchronized void save() {
        Map<String, Object> styleData = new LinkedHashMap<>();
        for (Map.Entry<String, List<FireworkEffect>> entry : styles.entrySet()) {
            styleData.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        plugin.getConfig().set(STYLES_PATH, styleData);
        plugin.getConfig().set(LEGACY_PATH, null);

        Map<String, Object> chanceData = new LinkedHashMap<>();
        for (String name : styles.keySet()) {
            chanceData.put(name, chanceOf(name));
        }
        plugin.getConfig().set(CHANCES_PATH, chanceData);

        plugin.saveConfig();
    }

    public synchronized void resetToDefaults() {
        styles.clear();
        styles.putAll(builtInDefaults());
        chances.clear();
        rebalance();
        save();
    }

    public synchronized List<String> getStyleNames() {
        return new ArrayList<>(styles.keySet());
    }

    public synchronized int getStyleCount() {
        return styles.size();
    }

    public synchronized boolean hasStyle(String name) {
        return findKey(name) != null;
    }

    public synchronized List<FireworkEffect> getStyle(String name) {
        String key = findKey(name);
        return key == null ? Collections.emptyList() : styles.get(key);
    }

    /**
     * Returns the effective chances (already compensated to sum up to 100%).
     */
    public synchronized Map<String, Double> getChances() {
        return new LinkedHashMap<>(chances);
    }

    public synchronized double getChance(String name) {
        String key = findKey(name);
        return key == null ? 0.0D : chanceOf(key);
    }

    /**
     * Returns the effects of a weighted-randomly chosen style.
     */
    public synchronized List<FireworkEffect> getRandomEffects() {
        String name = getRandomStyleName();
        List<FireworkEffect> effects = name == null ? null : styles.get(name);
        if (effects == null || effects.isEmpty()) {
            effects = builtInDefaults().values().iterator().next();
        }
        return new ArrayList<>(effects);
    }

    /**
     * Returns a weighted-random style name or {@code null} when none is set.
     */
    public synchronized String getRandomStyleName() {
        if (styles.isEmpty()) {
            return null;
        }

        double total = 0.0D;
        for (String name : styles.keySet()) {
            total += chanceOf(name);
        }

        if (total <= 0.0D) {
            List<String> names = new ArrayList<>(styles.keySet());
            return names.get(ThreadLocalRandom.current().nextInt(names.size()));
        }

        double roll = ThreadLocalRandom.current().nextDouble() * total;
        double accumulated = 0.0D;
        String last = null;
        for (String name : styles.keySet()) {
            last = name;
            accumulated += chanceOf(name);
            if (roll < accumulated) {
                return name;
            }
        }
        return last;
    }

    public synchronized void addStyle(String name, List<FireworkEffect> effects) {
        double average = averageChance();
        styles.put(name, new ArrayList<>(effects));
        chances.put(name, average <= 0.0D ? 1.0D : average);
        rebalance();
    }

    public synchronized boolean removeStyle(String name) {
        String key = findKey(name);
        if (key == null) {
            return false;
        }
        styles.remove(key);
        chances.remove(key);
        rebalance();
        return true;
    }

    /**
     * Sets a style chance and redistributes the remaining percentage across the
     * other styles so the total always stays at 100%.
     */
    public synchronized boolean setChance(String name, double value) {
        String key = findKey(name);
        if (key == null) {
            return false;
        }

        double target = round(clamp(value, 0.0D, 100.0D));
        chances.put(key, target);
        distributeRemainder(key, round(100.0D - target));
        fixRounding();
        return true;
    }

    /**
     * Resets every style chance to an equal distribution.
     */
    public synchronized void resetChances() {
        chances.clear();
        rebalance();
    }

    /**
     * Suggests an unused automatic style name (style1, style2, ...).
     */
    public synchronized String nextAutoName() {
        int index = 1;
        while (hasStyle("style" + index)) {
            index++;
        }
        return "style" + index;
    }

    private double chanceOf(String name) {
        Double value = chances.get(name);
        return value == null ? 0.0D : value;
    }

    private double averageChance() {
        double total = 0.0D;
        int count = 0;
        for (double value : chances.values()) {
            total += value;
            count++;
        }
        return count == 0 ? 100.0D : total / count;
    }

    /**
     * Scales every chance proportionally so they sum up to 100%.
     */
    private void rebalance() {
        if (styles.isEmpty()) {
            chances.clear();
            return;
        }

        double total = 0.0D;
        for (String name : styles.keySet()) {
            total += chanceOf(name);
        }

        Map<String, Double> normalized = new LinkedHashMap<>();
        if (total <= 0.0D) {
            double equal = round(100.0D / styles.size());
            for (String name : styles.keySet()) {
                normalized.put(name, equal);
            }
        } else {
            for (String name : styles.keySet()) {
                normalized.put(name, round(chanceOf(name) * 100.0D / total));
            }
        }

        chances.clear();
        chances.putAll(normalized);
        fixRounding();
    }

    /**
     * Distributes {@code remainder} across every style except {@code fixedKey}.
     */
    private void distributeRemainder(String fixedKey, double remainder) {
        double others = 0.0D;
        for (String name : styles.keySet()) {
            if (!name.equals(fixedKey)) {
                others += chanceOf(name);
            }
        }

        int count = Math.max(1, styles.size() - 1);
        for (String name : styles.keySet()) {
            if (name.equals(fixedKey)) {
                continue;
            }
            double value = others <= 0.0D ? remainder / count : chanceOf(name) / others * remainder;
            chances.put(name, round(value));
        }
    }

    /**
     * Makes sure the rounded chances add up to exactly 100%.
     */
    private void fixRounding() {
        if (chances.isEmpty()) {
            return;
        }

        double sum = 0.0D;
        for (double value : chances.values()) {
            sum += value;
        }

        double diff = round(100.0D - sum);
        if (Math.abs(diff) < 0.01D) {
            return;
        }

        String largest = null;
        double max = -1.0D;
        for (Map.Entry<String, Double> entry : chances.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                largest = entry.getKey();
            }
        }

        if (largest != null) {
            chances.put(largest, round(Math.max(0.0D, chances.get(largest) + diff)));
        }
    }

    private String findKey(String name) {
        if (name == null) {
            return null;
        }
        for (String key : styles.keySet()) {
            if (key.equalsIgnoreCase(name)) {
                return key;
            }
        }
        return null;
    }

    private static double round(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private Map<String, List<FireworkEffect>> builtInDefaults() {
        Map<String, List<FireworkEffect>> defaults = new LinkedHashMap<>();
        defaults.put("classic", Collections.singletonList(
                FireworkEffect.builder().with(FireworkEffect.Type.BALL)
                        .withColor(Color.RED, Color.YELLOW, Color.WHITE)
                        .withFade(Color.ORANGE)
                        .flicker(true)
                        .build()));
        defaults.put("starburst", Collections.singletonList(
                FireworkEffect.builder().with(FireworkEffect.Type.STAR)
                        .withColor(Color.AQUA, Color.WHITE, Color.BLUE)
                        .withFade(Color.PURPLE)
                        .flicker(true)
                        .build()));
        defaults.put("rainbow", Collections.singletonList(
                FireworkEffect.builder().with(FireworkEffect.Type.BURST)
                        .withColor(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.AQUA)
                        .withFade(Color.WHITE)
                        .trail(true)
                        .build()));
        defaults.put("creeper", Collections.singletonList(
                FireworkEffect.builder().with(FireworkEffect.Type.CREEPER)
                        .withColor(Color.GREEN, Color.LIME)
                        .withFade(Color.GREEN)
                        .flicker(true)
                        .trail(true)
                        .build()));
        return defaults;
    }
}
