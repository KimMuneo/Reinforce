package org.example.kimmuneo.reinforce.Anvil;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class AnvilListener implements Listener {
    private final Plugin plugin;
    private final ReinforceLogic reinforceLogic;
    private static final int INV_SIZE = 9;
    private static final int INPUT_SLOT = 4;
    private static final int ACTION_SLOT = 8;
    private final String title = ChatColor.GOLD + "스타 포스";

    public AnvilListener(Plugin plugin) {
        this.plugin = plugin;
        this.reinforceLogic = new ReinforceLogic(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getType() == Material.ANVIL || block.getType() == Material.CHIPPED_ANVIL || block.getType() == Material.DAMAGED_ANVIL) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            Inventory inv = Bukkit.createInventory(null, INV_SIZE, title);

            ItemStack filler = new ItemStack(Material.ANVIL);
            ItemMeta meta = filler.getItemMeta();
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);

            for (int i = 0; i < INV_SIZE; i++) {
                if (i == INPUT_SLOT || i == ACTION_SLOT) continue;
                inv.setItem(i, filler);
            }

            reinforceLogic.updateChanceDisplay(inv);
            player.openInventory(inv);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();

        if (slot == INPUT_SLOT) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> reinforceLogic.updateChanceDisplay(inv), 1L);
            return;
        }

        if (slot == ACTION_SLOT && event.isLeftClick()) {
            reinforceLogic.handleReinforce(player, inv);
            event.setCancelled(true);
            return;
        }

        if (slot >= 0 && slot < INV_SIZE && slot != INPUT_SLOT) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(title)) return;

        Inventory inv = event.getInventory();
        ItemStack item = inv.getItem(INPUT_SLOT);
        if (item != null && item.getType() != Material.AIR) {
            Player player = (Player) event.getPlayer();
            Map<Integer, ItemStack> notStored = player.getInventory().addItem(item);
            for (ItemStack leftover : notStored.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }
}
