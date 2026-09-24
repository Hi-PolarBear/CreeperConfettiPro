package creeperconfetti.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Folia-safe scheduling helpers.
 *
 * <p>Folia has no main thread: every task has to run on the region that owns the
 * affected location / entity, on the global region thread, or asynchronously.
 * {@code Bukkit.getScheduler()} does not exist on Folia at all, so all plugin
 * scheduling goes through this class.
 */
public final class Schedulers {

    private Schedulers() {
    }

    /** Runs on the region owning {@code entity}, on the next tick. */
    public static void runOnEntity(Plugin plugin, Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, scheduled -> task.run(), () -> { });
    }

    /**
     * Runs on the region owning {@code entity} after {@code delayTicks} ticks
     * (Folia requires a delay of at least one tick).
     */
    public static void runOnEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        entity.getScheduler().runDelayed(plugin, scheduled -> task.run(), () -> { }, Math.max(1L, delayTicks));
    }

    /** Runs on the region owning {@code location}, on the next tick. */
    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        Bukkit.getRegionScheduler().run(plugin, location, scheduled -> task.run());
    }

    /** Runs on the region owning {@code location} after {@code delayTicks} ticks. */
    public static void runAtLocationLater(Plugin plugin, Location location, Runnable task, long delayTicks) {
        Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduled -> task.run(), Math.max(1L, delayTicks));
    }

    /** Runs on the global region thread (the closest thing Folia has to a main thread). */
    public static void runGlobal(Plugin plugin, Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, scheduled -> task.run());
    }

    /** Runs asynchronously, outside of any region. */
    public static void runAsync(Plugin plugin, Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduled -> task.run());
    }
}
