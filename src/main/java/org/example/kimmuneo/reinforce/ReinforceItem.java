package org.example.kimmuneo.reinforce;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ReinforceItem {
    /**
     * 마법 부여 주문서 아이템 생성
     */
    public static ItemStack createEnchantBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6마법 부여 주문서");
            book.setItemMeta(meta);
        }
        return book;
    }

    /**
     * 주문의 흔적 아이템 생성
     */
    public static ItemStack createSpellTrace() {
        ItemStack glowStone = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = glowStone.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6주문의 흔적");
            glowStone.setItemMeta(meta);
        }
        return glowStone;
    }
}