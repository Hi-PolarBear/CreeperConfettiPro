package creeperconfetti.gui;

import java.util.List;

import creeperconfetti.CreeperConfettiPro;
import creeperconfetti.effects.FireworkStyleManager;
import creeperconfetti.effects.FireworkUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Lists every firework style and allows adding new ones, deleting existing ones
 * (via the edit screen) and resetting the chances.
 */
public class StyleListGui extends BaseGui {

    private static final int SIZE = 54;
    private static final int CONTENT_SLOTS = 45;
    private static final int PAGE_INFO_SLOT = 46;
    private static final int PREV_SLOT = 45;
    private static final int RESET_SLOT = 48;
    private static final int ADD_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private int page;

    public StyleListGui(CreeperConfettiPro plugin, Player viewer, int page) {
        super(plugin, viewer, SIZE, plugin.getLanguageManager().getMessage("gui.title"));
        this.page = Math.max(1, page);
    }

    @Override
    public void refresh() {
        for (int slot = 0; slot < SIZE; slot++) {
            inventory.setItem(slot, null);
        }

        FireworkStyleManager manager = plugin.getFireworkStyleManager();
        List<String> names = manager.getStyleNames();
        int totalPages = Math.max(1, (int) Math.ceil(names.size() / (double) CONTENT_SLOTS));
        if (page > totalPages) {
            page = totalPages;
        }

        Material firework = FireworkUtil.fireworkMaterial();
        int start = (page - 1) * CONTENT_SLOTS;
        int end = Math.min(start + CONTENT_SLOTS, names.size());

        for (int index = start; index < end; index++) {
            String name = names.get(index);
            inventory.setItem(index - start, item(firework, 1, "&f" + name,
                    msg("gui.style_effects", manager.getStyle(name).size()),
                    msg("gui.style_chance", formatNumber(manager.getChance(name))),
                    msg("gui.style_click")));
        }

        ItemStack filler = item(material("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), 1, " ");
        for (int slot = CONTENT_SLOTS; slot < SIZE; slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(PAGE_INFO_SLOT, item(Material.PAPER, 1, msg("gui.page", page, totalPages)));
        inventory.setItem(RESET_SLOT, item(material("HOPPER"), 1, msg("gui.reset_chances"), msg("gui.reset_chances_lore")));
        inventory.setItem(ADD_SLOT, item(material("NETHER_STAR", "EMERALD"), 1, msg("gui.add"), msg("gui.add_lore")));

        if (page > 1) {
            inventory.setItem(PREV_SLOT, item(Material.ARROW, 1, msg("gui.prev")));
        }
        if (page < totalPages) {
            inventory.setItem(NEXT_SLOT, item(Material.ARROW, 1, msg("gui.next")));
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getClickedInventory() == null || event.getClickedInventory() != inventory) {
            return;
        }

        int slot = event.getSlot();
        FireworkStyleManager manager = plugin.getFireworkStyleManager();

        if (slot < CONTENT_SLOTS) {
            List<String> names = manager.getStyleNames();
            int index = (page - 1) * CONTENT_SLOTS + slot;
            if (index >= names.size()) {
                return;
            }
            String name = names.get(index);
            if (event.isRightClick()) {
                FireworkUtil.spawnAndDetonate(plugin, viewer.getLocation().add(0, 1, 0), manager.getStyle(name), 0);
            } else {
                new StyleEditGui(plugin, viewer, name).openLater();
            }
            return;
        }

        if (slot == PREV_SLOT) {
            page--;
            open();
        } else if (slot == NEXT_SLOT) {
            page++;
            open();
        } else if (slot == RESET_SLOT) {
            manager.resetChances();
            manager.save();
            viewer.sendMessage(msg("effect.chance_reset"));
            refresh();
        } else if (slot == ADD_SLOT) {
            addStyle(manager);
        }
    }

    private void addStyle(FireworkStyleManager manager) {
        List<FireworkEffect> effects = FireworkUtil.getEffectsFromItem(viewer.getInventory().getItemInMainHand());
        if (effects.isEmpty()) {
            viewer.sendMessage(msg("command.hold_firework"));
            return;
        }

        String name = manager.nextAutoName();
        manager.addStyle(name, effects);
        manager.save();
        viewer.sendMessage(msg("gui.add_success", name));
        FireworkUtil.spawnAndDetonate(plugin, viewer.getLocation().add(0, 1, 0), effects, 0);
        refresh();
    }
}
