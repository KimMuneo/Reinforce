package org.example.kimmuneo.reinforce.Anvil;


import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.example.kimmuneo.reinforce.AllTool;
import org.example.kimmuneo.reinforce.SoundUtils;

import java.util.*;

public class ReinforceLogic {

    private final Plugin plugin;
    private final StarAttributeApplier attributeApplier;

    // 강화 확률표 [성공, 실패, 하락, 파괴]
    private final double[][] rates = {
            {1, 0.00, 0.00, 0.00}, // 0 -> 1
            {0.9, 0.09, 0.01, 0},
            {0.8, 0.16, 0.04, 0},
            {0.7, 0.20, 0.1, 0},
            {0.6, 0.23, 0.17, 0},
            {0.5, 0.23, 0.26, 0.01},
            {0.4, 0.20, 0.38, 0.02},
            {0.3, 0.16, 0.51, 0.03},
            {0.2, 0.09, 0.67, 0.04},
            {0.1, 0, 0.85, 0.05}
    };

    private static final int INPUT_SLOT = 4;
    private static final int ACTION_SLOT = 8;

    public ReinforceLogic(Plugin plugin) {
        this.plugin = plugin;
        this.attributeApplier = new StarAttributeApplier(plugin);
    }

    public void handleReinforce(Player player, Inventory inv) {
        ItemStack item = inv.getItem(INPUT_SLOT);
        if (item == null || item.getType() == Material.AIR) return;

        if (!AllTool.isTool(item.getType())) {
            player.sendMessage("§c인챈트가 가능한 아이템이 아닙니다!");
            return;
        }

        int currentStars = ItemUtils.getStarsFromItem(item);
        if (currentStars >= 10) {
            player.sendMessage("§e이미 최대 강화(10성)입니다!");
            return;
        }

        int spellSlot = ItemUtils.findSpellSlot(player);
        if (spellSlot == -1) {
            player.sendMessage("§c주문의 흔적이 필요합니다!");
            return;
        }

        ItemUtils.consumeSpell(player, spellSlot);
        double[] rate = rates[currentStars];
        double success = rate[0], fail = rate[1], down = rate[2], destroy = rate[3];

        double rand = Math.random();

        if (rand <= success) {
            currentStars++;
            player.sendMessage("§a★ 강화 성공! 현재 " + currentStars + "성!");
            SoundUtils.playForceSound(player);

            // ★ 10성 달성 시 내구도 무한(Unbreakable) 적용
            if (currentStars >= 10) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setUnbreakable(true);
                    meta.setLore(Arrays.asList("§b이 아이템은 §610성§b으로 완성되었습니다!", "§7(내구도 무한)"));
                    item.setItemMeta(meta);
                }
                player.sendMessage("§b✨ 아이템이 완성되었습니다! 이제 부서지지 않습니다!");
            }
        } else if (rand <= success + fail) {
            player.sendMessage("§7강화 실패... 변화 없음.");
        } else if (rand <= success + fail + down) {
            if (currentStars > 0) currentStars--;
            player.sendMessage("§e☆ 강화 하락! 현재 " + currentStars + "성!");
        } else {
            inv.setItem(INPUT_SLOT, new ItemStack(Material.AIR));
            player.sendMessage("§c💥 아이템이 파괴되었습니다!");
            SoundUtils.playDestorySound(player);
            updateChanceDisplay(inv);
            return;
        }

        ItemUtils.updateLore(item, currentStars);
        attributeApplier.applyStarDamageModifier(item, currentStars);
        updateChanceDisplay(inv);
    }

    public void updateChanceDisplay(Inventory inv) {
        ItemStack item = inv.getItem(INPUT_SLOT);
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta meta = info.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName("§e⭐ 강화 확률 정보");

        List<String> lore = new ArrayList<>();
        int stars = (item == null) ? 0 : ItemUtils.getStarsFromItem(item);

        if (stars >= 10) lore.add("§a최대 강화 단계입니다!");
        else {
            double[] rate = rates[stars];
            lore.add("§6" + stars + "강");
            lore.add("§a성공: §f" + (int) (rate[0] * 100) + "%");
            lore.add("§7실패: §f" + (int) (rate[1] * 100) + "%");
            lore.add("§e하락: §f" + (int) (rate[2] * 100) + "%");
            lore.add("§c파괴: §f" + (int) (rate[3] * 100) + "%");
        }

        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(ACTION_SLOT, info);
    }
}
