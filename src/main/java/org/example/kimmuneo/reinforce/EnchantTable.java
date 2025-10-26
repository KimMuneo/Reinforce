package org.example.kimmuneo.reinforce;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

import static org.example.kimmuneo.reinforce.AllTool.isTool;

public class EnchantTable implements Listener {

    String tableName = "마법 부여 재설정";
    ChatColor tableColor = ChatColor.BLACK;



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();

        if (block.getType() == Material.ENCHANTING_TABLE) {
            event.setCancelled(true);

            Player player = event.getPlayer();

            Inventory inv = Bukkit.createInventory(null, 9, tableColor + tableName);

            ItemStack enchantTable = new ItemStack(Material.ENCHANTING_TABLE);

            for (int i = 0; i < 9; i++) {
                if (i == 4) continue;
                inv.setItem(i, enchantTable);
            }

            player.openInventory(inv);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getView().getTitle().equals(tableColor + tableName)) {
            int slot = event.getRawSlot();

            if (slot == 4 && event.isRightClick()) {
                Player player = (Player) event.getWhoClicked();
                ItemStack item = event.getInventory().getItem(4);
                if (item != null && item.getType() != Material.AIR) {
                    // 주문서 찾기
                    ItemStack foundBook = null;
                    int bookSlot = -1;
                    for (int i = 0; i < player.getInventory().getSize(); i++) {
                        ItemStack invItem = player.getInventory().getItem(i);
                        if (invItem != null && invItem.getType() == Material.BOOK) {
                            if (invItem.hasItemMeta() && invItem.getItemMeta().hasDisplayName()
                                    && invItem.getItemMeta().getDisplayName().equals("§6마법 부여 주문서")) {
                                foundBook = invItem;
                                bookSlot = i;
                                break;
                            }
                        }
                    }

                    if (!isTool(item.getType())){
                        player.sendMessage("§c인챈트가 가능한 아이템이 아닙니다!");
                        event.setCancelled(true);
                        return;
                    }

                    if (foundBook == null) {
                        player.sendMessage("§c마법 부여 주문서가 필요합니다!");
                        event.setCancelled(true);
                        return;
                    }

                    // 주문서 소모
                    if (foundBook.getAmount() > 1) {
                        foundBook.setAmount(foundBook.getAmount() - 1);
                        player.getInventory().setItem(bookSlot, foundBook);
                    } else {
                        player.getInventory().setItem(bookSlot, null);
                    }

                    // --- 리롤 로직 ---
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        Enchantment[] allEnchants = Enchantment.values();
                        Random random = new Random();

                        // 현재 인첸트 줄 수: 없으면 1, 있으면 기존 줄 수
                        int currentLines = Math.max(1, meta.getEnchants().size());
                        int maxEnchant = 4; // 원하는 최대 줄 수

                        // 3% 확률로 줄 수 증가
                        if (currentLines < maxEnchant && random.nextInt(100) < 3) {
                            currentLines++;
                        }

                        // 기존 인첸트 모두 제거
                        for (Enchantment ench : meta.getEnchants().keySet()) {
                            meta.removeEnchant(ench);
                        }

                        // 줄 수만큼 랜덤 인첸트 + 랜덤 레벨
                        Set<Enchantment> used = new HashSet<>();
                        int safetyMax = 20;
                        for (int i = 0; i < currentLines; i++) {
                            int safety = 0;
                            Enchantment randomEnchant = allEnchants[random.nextInt(allEnchants.length)];
                            while (used.contains(randomEnchant) && safety < safetyMax) {
                                randomEnchant = allEnchants[random.nextInt(allEnchants.length)];
                                safety++;
                            }

                            int maxLevel = randomEnchant.getMaxLevel();
                            int minLevel = 1;
                            int level = minLevel;

                            double rand = random.nextDouble();
                            double[] chances = { 0.5, 0.25, 0.15, 0.07, 0.03 }; // 1~5레벨
                            double cumulative = 0;

                            for (int l = 0; l < maxLevel; l++){
                                cumulative += chances[l];
                                if(rand < cumulative){
                                    level = l + 1;
                                    break;
                                }
                            }

                            meta.addEnchant(randomEnchant, level, true);
                            used.add(randomEnchant);
                        }

                        item.setItemMeta(meta);
                        event.getInventory().setItem(4, item);

                        //인챈트 소리
                        playForceSound(player);
                    }
                }
                event.setCancelled(true);
                return;
            }

            // 마법부여대 막기
            if(slot >= 0 && slot < 9 && slot != 4){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(tableColor + tableName)) {
            Inventory inv = event.getInventory();
            ItemStack item = inv.getItem(4);
            if (item != null && item.getType() != Material.AIR) {
                Player player = (Player) event.getPlayer();
                Map<Integer, ItemStack> notStored = player.getInventory().addItem(item);
                for (ItemStack leftover : notStored.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
            }
        }
    }


    private void playForceSound(Player player){
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.85f , 1.5f);
    }
}