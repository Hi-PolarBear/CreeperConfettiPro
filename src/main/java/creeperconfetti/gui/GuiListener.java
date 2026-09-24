package creeperconfetti.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Routes inventory clicks / drags to the owning {@link BaseGui} and blocks all
 * item movement inside plugin GUIs.
 */
public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BaseGui)) {
            return;
        }
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player) {
            ((BaseGui) holder).onClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BaseGui) {
            event.setCancelled(true);
        }
    }
}
