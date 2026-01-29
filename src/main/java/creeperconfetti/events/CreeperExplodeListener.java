
package creeperconfetti.events;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import creeperconfetti.CreeperConfetti;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class CreeperExplodeListener implements Listener {
    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        double random = ThreadLocalRandom.current().nextDouble() * 100.0D;

        if (random >= CreeperConfetti.getInstance().getConfig().getDouble("confetti_chance")) {
            return;
        }
        if (event.getEntityType().equals(EntityType.CREEPER)) {
            event.setCancelled(true);

            Creeper creeper = (Creeper)event.getEntity();
            Location location = creeper.getLocation();
            location = location.add(new Vector(0, 1, 0));

            Firework firework = (Firework)creeper.getWorld().spawnEntity(location, EntityType.FIREWORK_ROCKET);

            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.addEffects((List)CreeperConfetti.getInstance().getConfig().get("confetti_effect"));
            fireworkMeta.setPower(0);
            firework.setFireworkMeta(fireworkMeta);

            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 1.0F);

            Objects.requireNonNull(firework); Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)CreeperConfetti.getInstance(), firework::detonate, 1L);
        }
    }
}
