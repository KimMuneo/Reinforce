package org.example.kimmuneo.reinforce.Anvil;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemUtils {

    private static final String STAR_SYMBOL = "★";

    /** 아이템에서 별 개수 추출 */
    public static int getStarsFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return 0;
        for (String line : meta.getLore()) {
            if (line.contains("★") || line.contains("☆")) {
                return (int) line.chars().filter(ch -> ch == '★').count();
            }
        }
        return 0;
    }

    /** Lore 갱신 (강화 수치 표시용) */
    public static void updateLore(ItemStack item, int stars) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        String newLine = "§6" + "★".repeat(Math.max(0, stars)) + "§7" + "☆".repeat(Math.max(0, 10 - stars));
        int starIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains("★") || line.contains("☆")) {
                starIndex = i;
                break;
            }
        }
        if (starIndex >= 0) lore.set(starIndex, newLine);
        else lore.add(newLine);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }



    /** 플레이어 인벤토리에서 주문의 흔적 슬롯 탐색 */
    public static int findSpellSlot(Player player) {
        int idx = player.getInventory().first(Material.GLOWSTONE_DUST);
        if (idx == -1) return -1;
        ItemStack found = player.getInventory().getItem(idx);
        if (found == null || !found.hasItemMeta()) return -1;
        ItemMeta m = found.getItemMeta();
        if (m.hasDisplayName() && m.getDisplayName().equals("§6주문의 흔적")) return idx;
        // if first glowstone isn't the named one, scan remaining quickly
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack invItem = player.getInventory().getItem(i);
            if (invItem != null && invItem.getType() == Material.GLOWSTONE_DUST && invItem.hasItemMeta()) {
                ItemMeta im = invItem.getItemMeta();
                if (im.hasDisplayName() && im.getDisplayName().equals("§6주문의 흔적")) return i;
            }
        }
        return -1;
    }

    /** 주문의 흔적 1개 사용 */
    public static void consumeSpell(Player player, int slot) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null) return;

        int amount = item.getAmount();
        if (amount > 1) item.setAmount(amount - 1);
        else player.getInventory().setItem(slot, new ItemStack(Material.AIR));
    }
}
