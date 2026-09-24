package creeperconfetti.gui;

import java.util.List;

import creeperconfetti.CreeperConfettiPro;
import creeperconfetti.effects.FireworkStyleManager;
import creeperconfetti.effects.FireworkUtil;
import creeperconfetti.util.MessageUtil;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Per-style editor: preview, adjust the trigger chance and delete the style.
 */
public class StyleEditGui extends BaseGui {

    private static final int SIZE = 27;

    private static final int PREVIEW_SLOT = 4;
    private static final int MINUS_10_SLOT = 10;
    private static final int MINUS_5_SLOT = 11;
    private static final int MINUS_1_SLOT = 12;
    private static final int CHANCE_SLOT = 13;
    private static final int PLUS_1_SLOT = 14;
    private static final int PLUS_5_SLOT = 15;
    private static final int PLUS_10_SLOT = 16;
    private static final int SET_HAND_SLOT = 18;
    private static final int DELETE_SLOT = 22;
    private static final int BACK_SLOT = 26;

    private final String styleName;

    public StyleEditGui(CreeperConfettiPro plugin, Player viewer, String styleName) {
        super(plugin, viewer, SIZE,
                MessageUtil.format(plugin.getLanguageManager().getMessage("gui.edit_title"), styleName));
        this.styleName = styleName;
    }

    @Override
    public void refresh() {
        for (int slot = 0; slot < SIZE; slot++) {
            inventory.setItem(slot, null);
        }

        FireworkStyleManager manager = plugin.getFireworkStyleManager();
        List<FireworkEffect> effects = manager.getStyle(styleName);

        inventory.setItem(PREVIEW_SLOT, item(FireworkUtil.fireworkMaterial(), 1, "&f" + styleName,
                msg("gui.style_effects", effects.size()),
                msg("gui.preview_lore")));

        Material minus = material("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
        Material plus = material("LIME_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
        inventory.setItem(MINUS_10_SLOT, item(minus, 1, msg("gui.minus", 10)));
        inventory.setItem(MINUS_5_SLOT, item(minus, 1, msg("gui.minus", 5)));
        inventory.setItem(MINUS_1_SLOT, item(minus, 1, msg("gui.minus", 1)));
        inventory.setItem(CHANCE_SLOT, item(Material.PAPER, 1, msg("gui.chance_name", styleName),
                msg("gui.chance_lore", formatNumber(manager.getChance(styleName)))));
        inventory.setItem(PLUS_1_SLOT, item(plus, 1, msg("gui.plus", 1)));
        inventory.setItem(PLUS_5_SLOT, item(plus, 1, msg("gui.plus", 5)));
        inventory.setItem(PLUS_10_SLOT, item(plus, 1, msg("gui.plus", 10)));

        inventory.setItem(SET_HAND_SLOT, item(material("FIREWORK_STAR", "FIREWORK_CHARGE", "BLAZE_POWDER"), 1,
                msg("gui.set_from_hand"), msg("gui.set_from_hand_lore")));
        inventory.setItem(DELETE_SLOT, item(material("BARRIER", "TNT"), 1,
                msg("gui.delete"), msg("gui.delete_lore", styleName)));
        inventory.setItem(BACK_SLOT, item(Material.ARROW, 1, msg("gui.back")));

        ItemStack filler = item(material("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), 1, " ");
        for (int slot = 0; slot < SIZE; slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
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

        FireworkStyleManager manager = plugin.getFireworkStyleManager();
        int slot = event.getSlot();

        if (slot == PREVIEW_SLOT) {
            preview(manager);
        } else if (slot == MINUS_10_SLOT) {
            adjust(manager, -10.0D);
        } else if (slot == MINUS_5_SLOT) {
            adjust(manager, -5.0D);
        } else if (slot == MINUS_1_SLOT) {
            adjust(manager, -1.0D);
        } else if (slot == PLUS_1_SLOT) {
            adjust(manager, 1.0D);
        } else if (slot == PLUS_5_SLOT) {
            adjust(manager, 5.0D);
        } else if (slot == PLUS_10_SLOT) {
            adjust(manager, 10.0D);
        } else if (slot == SET_HAND_SLOT) {
            setFromHand(manager);
        } else if (slot == DELETE_SLOT) {
            delete(manager);
        } else if (slot == BACK_SLOT) {
            new StyleListGui(plugin, viewer, 1).openLater();
        }
    }

    private void preview(FireworkStyleManager manager) {
        FireworkUtil.spawnAndDetonate(plugin, viewer.getLocation().add(0, 1, 0), manager.getStyle(styleName), 0);
    }

    private void adjust(FireworkStyleManager manager, double delta) {
        if (!manager.hasStyle(styleName)) {
            viewer.sendMessage(msg("effect.not_found", styleName));
            new StyleListGui(plugin, viewer, 1).openLater();
            return;
        }
        manager.setChance(styleName, manager.getChance(styleName) + delta);
        manager.save();
        refresh();
        viewer.sendMessage(msg("gui.chance_set", styleName, formatNumber(manager.getChance(styleName))));
    }

    private void setFromHand(FireworkStyleManager manager) {
        List<FireworkEffect> effects = FireworkUtil.getEffectsFromItem(viewer.getInventory().getItemInMainHand());
        if (effects.isEmpty()) {
            viewer.sendMessage(msg("command.hold_firework"));
            return;
        }
        manager.addStyle(styleName, effects);
        manager.save();
        viewer.sendMessage(msg("gui.set_from_hand_success", styleName));
        refresh();
    }

    private void delete(FireworkStyleManager manager) {
        if (!manager.removeStyle(styleName)) {
            viewer.sendMessage(msg("effect.not_found", styleName));
        } else {
            manager.save();
            viewer.sendMessage(msg("gui.delete_success", styleName));
        }
        new StyleListGui(plugin, viewer, 1).openLater();
    }
}
