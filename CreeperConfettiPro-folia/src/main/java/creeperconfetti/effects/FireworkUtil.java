package creeperconfetti.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import creeperconfetti.util.Schedulers;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/**
 * Small helpers for spawning fireworks and reading effects from items.
 */
public final class FireworkUtil {

    private FireworkUtil() {
    }

    public static boolean isFireworkRocket(ItemStack item) {
        Material material = fireworkMaterial();
        return item != null && material != null && item.getType() == material;
    }

    /**
     * Reads the firework effects stored on the given item.
     */
    public static List<FireworkEffect> getEffectsFromItem(ItemStack item) {
        if (!isFireworkRocket(item)) {
            return Collections.emptyList();
        }
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof FireworkMeta)) {
            return Collections.emptyList();
        }
        FireworkMeta fireworkMeta = (FireworkMeta) meta;
        if (!fireworkMeta.hasEffects()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(fireworkMeta.getEffects());
    }

    /**
     * Spawns a firework at the given location, applying the supplied effects.
     */
    public static Firework spawnFirework(Location location, List<FireworkEffect> effects, int power) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        if (effects != null && !effects.isEmpty()) {
            meta.addEffects(effects);
        }
        meta.setPower(power);
        firework.setFireworkMeta(meta);
        return firework;
    }

    /**
     * Spawns a firework and detonates it on the next tick, on the region that
     * owns the firework entity (Folia-safe).
     */
    public static void spawnAndDetonate(Plugin plugin, Location location, List<FireworkEffect> effects, int power) {
        Firework firework = spawnFirework(location, effects, power);
        Schedulers.runOnEntityLater(plugin, firework, firework::detonate, 1L);
    }

    public static Material fireworkMaterial() {
        Material material = Material.matchMaterial("FIREWORK_ROCKET");
        if (material == null) {
            material = Material.matchMaterial("FIREWORK");
        }
        return material;
    }
}
