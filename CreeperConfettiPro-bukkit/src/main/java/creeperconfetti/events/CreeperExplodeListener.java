package creeperconfetti.events;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import creeperconfetti.CreeperConfettiPro;
import creeperconfetti.effects.FireworkUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class CreeperExplodeListener implements Listener {

    private static final String CONFETTI_CHANCE_CONFIG = "confetti_chance";

    private final CreeperConfettiPro plugin;

    public CreeperExplodeListener(CreeperConfettiPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (!event.getEntity().getType().equals(EntityType.CREEPER)) {
            return;
        }

        double random = ThreadLocalRandom.current().nextDouble() * 100.0D;
        double chance = plugin.getConfig().getDouble(CONFETTI_CHANCE_CONFIG);

        if (random >= chance) {
            return;
        }

        event.setCancelled(true);

        Creeper creeper = (Creeper) event.getEntity();
        Location location = creeper.getLocation().add(new Vector(0, 1, 0));

        // Pick one random style out of the configured firework styles.
        List<FireworkEffect> effects = plugin.getFireworkStyleManager().getRandomEffects();

        Firework firework = FireworkUtil.spawnFirework(location, effects, 0);

        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 1.0F);

        creeper.remove();

        plugin.getServer().getScheduler().runTaskLater(plugin, firework::detonate, 1L);
    }
}
