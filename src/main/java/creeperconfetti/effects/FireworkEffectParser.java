package creeperconfetti.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Parses firework effects from a wide range of config representations:
 * serialized {@link FireworkEffect} objects, nested configuration sections and
 * plain YAML maps. Colors accept named constants (RED, LIME...), hex strings
 * (#RRGGBB) and RGB maps.
 */
public final class FireworkEffectParser {

    private FireworkEffectParser() {
    }

    /**
     * Parses a single effect or a list of effects.
     */
    public static List<FireworkEffect> parseEffects(Object raw) {
        List<FireworkEffect> effects = new ArrayList<>();
        if (raw == null) {
            return effects;
        }
        if (raw instanceof List<?>) {
            for (Object entry : (List<?>) raw) {
                FireworkEffect effect = parseEffect(entry);
                if (effect != null) {
                    effects.add(effect);
                }
            }
        } else {
            FireworkEffect effect = parseEffect(raw);
            if (effect != null) {
                effects.add(effect);
            }
        }
        return effects;
    }

    /**
     * Parses a single firework effect, returning {@code null} if it cannot be built.
     */
    public static FireworkEffect parseEffect(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof FireworkEffect) {
            return (FireworkEffect) raw;
        }
        if (raw instanceof ConfigurationSection) {
            return parseEffectSection((ConfigurationSection) raw);
        }
        if (raw instanceof Map<?, ?>) {
            return parseEffectMap((Map<?, ?>) raw);
        }
        return null;
    }

    private static FireworkEffect parseEffectSection(ConfigurationSection section) {
        return buildEffect(
                section.getString("type", "BALL"),
                section.getBoolean("flicker", false),
                section.getBoolean("trail", false),
                firstNonNull(section.get("colors"), section.get("color")),
                firstNonNull(section.get("fade-colors"), section.get("fade_colors"), section.get("fade"))
        );
    }

    private static FireworkEffect parseEffectMap(Map<?, ?> map) {
        return buildEffect(
                stringValue(map, "type", "BALL"),
                booleanValue(map, "flicker", false),
                booleanValue(map, "trail", false),
                firstNonNull(getIgnoreCase(map, "colors"), getIgnoreCase(map, "color")),
                firstNonNull(getIgnoreCase(map, "fade-colors"), getIgnoreCase(map, "fade_colors"), getIgnoreCase(map, "fade"))
        );
    }

    private static FireworkEffect buildEffect(String typeName, boolean flicker, boolean trail, Object colorsRaw, Object fadeRaw) {
        List<Color> colors = parseColors(colorsRaw);
        if (colors.isEmpty()) {
            return null;
        }

        FireworkEffect.Type type;
        try {
            type = FireworkEffect.Type.valueOf(typeName == null ? "BALL" : typeName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            type = FireworkEffect.Type.BALL;
        }

        try {
            return FireworkEffect.builder()
                    .with(type)
                    .flicker(flicker)
                    .trail(trail)
                    .withColor(colors)
                    .withFade(parseColors(fadeRaw))
                    .build();
        } catch (Exception exception) {
            return null;
        }
    }

    private static List<Color> parseColors(Object raw) {
        List<Color> colors = new ArrayList<>();
        if (raw == null) {
            return colors;
        }
        if (raw instanceof List<?>) {
            for (Object entry : (List<?>) raw) {
                Color color = parseColor(entry);
                if (color != null) {
                    colors.add(color);
                }
            }
        } else {
            Color color = parseColor(raw);
            if (color != null) {
                colors.add(color);
            }
        }
        return colors;
    }

    private static Color parseColor(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Color) {
            return (Color) raw;
        }
        if (raw instanceof Number) {
            return Color.fromRGB(((Number) raw).intValue() & 0xFFFFFF);
        }
        if (raw instanceof String) {
            return parseColorString((String) raw);
        }
        if (raw instanceof ConfigurationSection) {
            return parseColorMap(((ConfigurationSection) raw).getValues(false));
        }
        if (raw instanceof Map<?, ?>) {
            return parseColorMap((Map<?, ?>) raw);
        }
        return null;
    }

    private static Color parseColorMap(Map<?, ?> map) {
        Integer red = intValue(map, "RED", "red");
        Integer green = intValue(map, "GREEN", "green");
        Integer blue = intValue(map, "BLUE", "blue");
        if (isValidChannel(red) && isValidChannel(green) && isValidChannel(blue)) {
            return Color.fromRGB(red, green, blue);
        }

        Object rgb = firstNonNull(getIgnoreCase(map, "rgb"), getIgnoreCase(map, "hex"), getIgnoreCase(map, "value"));
        if (rgb instanceof Number) {
            return Color.fromRGB(((Number) rgb).intValue() & 0xFFFFFF);
        }
        if (rgb instanceof String) {
            return parseColorString((String) rgb);
        }
        return null;
    }

    private static Color parseColorString(String value) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.isEmpty()) {
            return null;
        }

        if (text.startsWith("#")) {
            text = text.substring(1);
        }

        if (text.matches("(?i)[0-9a-f]{6}")) {
            return Color.fromRGB(Integer.parseInt(text, 16));
        }
        if (text.matches("(?i)[0-9a-f]{3}")) {
            StringBuilder expanded = new StringBuilder();
            for (char character : text.toCharArray()) {
                expanded.append(character).append(character);
            }
            return Color.fromRGB(Integer.parseInt(expanded.toString(), 16));
        }

        switch (text.toUpperCase(Locale.ROOT).replace(' ', '_')) {
            case "AQUA": return Color.AQUA;
            case "BLACK": return Color.BLACK;
            case "BLUE": return Color.BLUE;
            case "FUCHSIA": return Color.FUCHSIA;
            case "GRAY":
            case "GREY": return Color.GRAY;
            case "GREEN": return Color.GREEN;
            case "LIME": return Color.LIME;
            case "MAROON": return Color.MAROON;
            case "NAVY": return Color.NAVY;
            case "OLIVE": return Color.OLIVE;
            case "ORANGE": return Color.ORANGE;
            case "PURPLE": return Color.PURPLE;
            case "RED": return Color.RED;
            case "SILVER": return Color.SILVER;
            case "TEAL": return Color.TEAL;
            case "WHITE": return Color.WHITE;
            case "YELLOW": return Color.YELLOW;
            default: return null;
        }
    }

    private static boolean isValidChannel(Integer value) {
        return value != null && value >= 0 && value <= 255;
    }

    private static Object getIgnoreCase(Map<?, ?> map, String key) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (String.valueOf(entry.getKey()).equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String stringValue(Map<?, ?> map, String key, String fallback) {
        Object value = getIgnoreCase(map, key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static boolean booleanValue(Map<?, ?> map, String key, boolean fallback) {
        Object value = getIgnoreCase(map, key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return fallback;
    }

    private static Integer intValue(Map<?, ?> map, String key, String alternative) {
        Object value = firstNonNull(getIgnoreCase(map, key), getIgnoreCase(map, alternative));
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException exception) {
                return null;
            }
        }
        return null;
    }

    private static Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
