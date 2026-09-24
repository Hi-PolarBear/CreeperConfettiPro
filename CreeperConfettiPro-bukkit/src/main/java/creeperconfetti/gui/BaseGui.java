package creeperconfetti.gui;

import java.util.ArrayList;
import java.util.List;

import creeperconfetti.CreeperConfettiPro;
import creeperconfetti.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Base class for the plugin's inventories. Every GUI is its own
 * {@link InventoryHolder} so click events can be routed safely.
 */
public abstract class BaseGui implements InventoryHolder {

    protected final CreeperConfettiPro plugin;
    protected final Player viewer;
    protected final Inventory inventory;

    protected BaseGui(CreeperConfettiPro plugin, Player viewer, int size, String title) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getViewer() {
        return viewer;
    }

    protected LanguageManager lang() {
        return plugin.getLanguageManager();
    }

    protected String msg(String key) {
        return lang().getMessage(key);
    }

    protected String msg(String key, Object... args) {
        return creeperconfetti.util.MessageUtil.format(lang().getMessage(key), args);
    }

    /**
     * Rebuilds the contents and shows the inventory.
     */
    public void open() {
        refresh();
        viewer.openInventory(inventory);
    }

    /**
     * Opens the inventory on the next tick - use this from inside a click event.
     */
    public void openLater() {
        Bukkit.getScheduler().runTask(plugin, this::open);
    }

    public abstract void refresh();

    public abstract void onClick(InventoryClickEvent event);

    protected static ItemStack item(Material material, int amount, String name, String... lore) {
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(colorize(name));
            }
            if (lore != null && lore.length > 0) {
                List<String> lines = new ArrayList<>();
                for (String line : lore) {
                    lines.add(colorize(line));
                }
                meta.setLore(lines);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /**
     * Translates {@code &} color codes into real chat colors. Language messages
     * are already translated, so this only affects hardcoded GUI strings and
     * style names.
     */
    protected static String colorize(String text) {
        return text == null ? null : ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Returns the first material that exists on this server version.
     */
    protected static Material material(String... names) {
        for (String name : names) {
            Material resolved = Material.matchMaterial(name);
            if (resolved != null) {
                return resolved;
            }
        }
        return Material.STONE;
    }

    protected static String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
