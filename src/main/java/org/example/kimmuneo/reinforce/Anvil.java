package org.example.kimmuneo.reinforce;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.plugin.Plugin;

import java.util.*;


import static org.example.kimmuneo.reinforce.AllTool.*;

public class Anvil implements Listener {

    private final Plugin plugin;

    private final String anvilName = "스타 포스";
    private final ChatColor anvilColor = ChatColor.GOLD;

    // 슬롯 / 크기 상수
    private static final int INV_SIZE = 9;
    private static final int INPUT_SLOT = 4;
    private static final int ACTION_SLOT = 8;

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

    // Cached keys for attribute modifiers
    private final NamespacedKey keyFixedAttackSpeed;
    private final NamespacedKey keyFixedArmorTough;
    private final NamespacedKey keyFixedArmor;
    private final NamespacedKey keyFixedKnockback;
    private final NamespacedKey keyStarBonus;

    public Anvil(Plugin plugin) {
        this.plugin = plugin;
        this.keyFixedAttackSpeed = new NamespacedKey(plugin, "fixed_attack_speed");
        this.keyFixedArmorTough = new NamespacedKey(plugin, "fixed_armorTough");
        this.keyFixedArmor = new NamespacedKey(plugin, "fixed_armor");
        this.keyFixedKnockback = new NamespacedKey(plugin, "fixed_knockback");
        this.keyStarBonus = new NamespacedKey(plugin, "star_bonus");
    }

    private double getBaseDamage(Material type) {
        return switch (type) {
            case WOODEN_SWORD, GOLDEN_SWORD -> 4.0;
            case STONE_SWORD, COPPER_SWORD -> 5.0;
            case IRON_SWORD -> 6.0;
            case DIAMOND_SWORD -> 7.0;
            case NETHERITE_SWORD -> 8.0;
            default -> 1.0;
        };
    }

    private double getBaseArmor(Material type) {
        return switch (type) {
            // 가죽 갑옷
            case LEATHER_HELMET -> 1.0;
            case LEATHER_CHESTPLATE -> 3.0;
            case LEATHER_LEGGINGS -> 2.0;
            case LEATHER_BOOTS -> 1.0;

            case COPPER_HELMET -> 2.0;
            case COPPER_CHESTPLATE -> 4.0;
            case COPPER_LEGGINGS -> 3.0;
            case COPPER_BOOTS -> 1.0;

            // 철 갑옷
            case IRON_HELMET -> 2.0;
            case IRON_CHESTPLATE -> 6.0;
            case IRON_LEGGINGS -> 5.0;
            case IRON_BOOTS -> 2.0;

            // 다이아몬드 갑옷
            case DIAMOND_HELMET -> 3.0;
            case DIAMOND_CHESTPLATE -> 8.0;
            case DIAMOND_LEGGINGS -> 6.0;
            case DIAMOND_BOOTS -> 3.0;

            // 네더라이트 갑옷
            case NETHERITE_HELMET -> 3.0;
            case NETHERITE_CHESTPLATE -> 8.0;
            case NETHERITE_LEGGINGS -> 6.0;
            case NETHERITE_BOOTS -> 3.0;

            // 골드 갑옷
            case GOLDEN_HELMET -> 2.0;
            case GOLDEN_CHESTPLATE -> 5.0;
            case GOLDEN_LEGGINGS -> 3.0;
            case GOLDEN_BOOTS -> 1.0;

            case CHAINMAIL_HELMET ->  2.0;
            case CHAINMAIL_CHESTPLATE ->  5.0;
            case CHAINMAIL_LEGGINGS ->  4.0;
            case CHAINMAIL_BOOTS ->  1.0;

            // 튜토리얼용 기본값
            default -> 0.0;
        };
    }

    private double getBaseArmorToughness(Material type) {
        return switch (type) {
            // 다이아몬드 갑옷
            case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS  -> 2.0;

            // 네더라이트 갑옷
            case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> 3.0;

            // 기본값
            default -> 0.0;
        };
    }

    private double getBaseKnockbackResistance(Material type) {
        return switch (type) {

            // 네더라이트 갑옷
            case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> 0.1;

            // 기본값
            default -> 0.0;
        };
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();

        if (block.getType() == Material.ANVIL || block.getType() == Material.CHIPPED_ANVIL || block.getType() == Material.DAMAGED_ANVIL) {
            event.setCancelled(true);
            Player player = event.getPlayer();

            Inventory inv = Bukkit.createInventory(null, INV_SIZE, anvilColor + anvilName);

            ItemStack filler = new ItemStack(Material.ANVIL);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(" ");
                filler.setItemMeta(fillerMeta);
            }

            // set filler except input slot and action slot
            for (int i = 0; i < INV_SIZE; i++) {
                if (i == INPUT_SLOT || i == ACTION_SLOT) continue;
                inv.setItem(i, filler);
            }

            // initial info paper in action slot
            updateChanceDisplay(inv);

            player.openInventory(inv);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(anvilColor + anvilName)) return;

        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();

        // 슬롯 4의 아이템이 변경될 때 확률 정보 갱신 (delay 한 tick으로 메타 반영)
        if (slot == INPUT_SLOT) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateChanceDisplay(inv), 1L);
            return;
        }

        // 강화 실행 (좌클릭)
        if (slot == ACTION_SLOT && event.isLeftClick()) {
            ItemStack item = inv.getItem(INPUT_SLOT);
            if (item == null || item.getType() == Material.AIR) {
                event.setCancelled(true);
                return;
            }

            if (!isTool(item.getType())) {
                player.sendMessage("§c인챈트가 가능한 아이템이 아닙니다!");
                event.setCancelled(true);
                return;
            }

            int currentStars = getStarsFromItem(item);
            if (currentStars >= 10) {
                player.sendMessage("§e이미 최대 강화(10성)입니다!");
                event.setCancelled(true);
                return;
            }

            int spellSlot = findSpellSlot(player);
            if (spellSlot == -1) {
                player.sendMessage("§c주문의 흔적이 필요합니다!");
                event.setCancelled(true);
                return;
            }

            // Consume spell (glowstone dust with custom name)
            consumeSpell(player, spellSlot);

            double[] rate = rates[currentStars];
            double success = rate[0], fail = rate[1], down = rate[2], destroy = rate[3];

            double rand = Math.random();

            if (rand <= success) {
                currentStars++;
                player.sendMessage("§a★ 강화 성공! 현재 " + currentStars + "성!");
                playForceSound(player);
            } else if (rand <= success + fail) {
                player.sendMessage("§7강화 실패... 변화가 없습니다.");
            } else if (rand <= success + fail + down) {
                if (currentStars > 0) currentStars--;
                player.sendMessage("§e☆ 강화 하락! 현재 " + currentStars + "성!");
            } else {
                // 파괴
                inv.setItem(INPUT_SLOT, new ItemStack(Material.AIR));
                player.sendMessage("§c💥 아이템이 파괴되었습니다!");
                playDestorySound(player);
                event.setCancelled(true);
                // update info display (item removed)
                updateChanceDisplay(inv);
                return;
            }

            // 별 갱신
            setStarsOnItem(item, currentStars);

            // 별 수치에 따른 보정 적용
            applyStarDamageModifier(item, currentStars);

            updateChanceDisplay(inv);
            event.setCancelled(true);
            return;
        }

        // GUI 다른 슬롯 클릭 방지 (상호작용 가능한 슬롯만 제외)
        if (slot >= 0 && slot < INV_SIZE && slot != INPUT_SLOT) event.setCancelled(true);
    }

    /**
     * 인벤토리의 slot4 아이템을 읽어 강화별 데미지 보정과 확률 표시를 갱신합니다.
     */
    private void updateChanceDisplay(Inventory inv) {
        ItemStack item = inv.getItem(INPUT_SLOT);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta meta = info.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName("§e⭐ 강화 확률 정보");

        List<String> lore = new ArrayList<>();

        int currentStars = 0;
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            currentStars = getStarsFromItem(item);
            // 아이템이 있을 때는 별 수치에 따른 공격력/방어력 보정 적용
            applyStarDamageModifier(item, currentStars);
        }

        if (currentStars >= 10) {
            lore.add("§a최대 강화 단계입니다!");
        } else {
            double[] rate = rates[currentStars];
            lore.add("§6" + (currentStars) +  "강");
            lore.add("§a성공 확률: §f" + (int) (rate[0] * 100) + "%");
            lore.add("§7실패 확률: §f" + (int) (rate[1] * 100) + "%");
            lore.add("§e하락 확률: §f" + (int) (rate[2] * 100) + "%");
            lore.add("§c파괴 확률: §f" + (int) (rate[3] * 100) + "%");
        }

        meta.setLore(lore);
        info.setItemMeta(meta);

        inv.setItem(ACTION_SLOT, info);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(anvilColor + anvilName)) {
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

    private void playForceSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.85f, 1.5f);
    }
    private void playDestorySound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 0.85f, 1.5f);
    }

    /**
     * 아이템의 lore에서 별 개수를 반환합니다.
     */
    private int getStarsFromItem(ItemStack item) {
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

    /**
     * 아이템의 lore에 별 줄을 설정하거나 갱신합니다.
     */
    private void setStarsOnItem(ItemStack item, int stars) {
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

    /**
     * 플레이어 인벤토리에서 "§6주문의 흔적" 이름을 가진 glowstone dust의 슬롯을 찾습니다.
     * 찾지 못하면 -1 반환.
     */
    private int findSpellSlot(Player player) {
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

    private void consumeSpell(Player player, int slot) {
        ItemStack spell = player.getInventory().getItem(slot);
        if (spell == null) return;
        if (spell.getAmount() > 1) spell.setAmount(spell.getAmount() - 1);
        else player.getInventory().setItem(slot, null);
    }

    /**
     * 공격력/방어력 관련 AttributeModifier를 적용합니다.
     * 기존 modifier를 키(keyStarBonus 등)로 찾아 안전하게 제거한 뒤 추가합니다.
     */
    private void applyStarDamageModifier(ItemStack item, int currentStars) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 공격무기 처리 (검류)
        if (isSword(item.getType())) {
            // 제거: 기존 star_bonus 공격력 modifier
            removeModifiersByKey(meta, Attribute.ATTACK_DAMAGE, keyStarBonus);

            // 제거: 기존 고정 공격속도 modifier
            removeModifiersByKey(meta, Attribute.ATTACK_SPEED, keyFixedAttackSpeed);

            // 고정 공격속도 추가 (항상 적용)
            try {
                AttributeModifier speedMod = new AttributeModifier(
                        keyFixedAttackSpeed,
                        -2.4,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND
                );
                meta.addAttributeModifier(Attribute.ATTACK_SPEED, speedMod);
            } catch (UnsupportedOperationException ignored) {}

            // 별 보정 (별 하나당 +1 공격력, baseDamage used to align original logic)
            removeModifiersByKey(meta, Attribute.ATTACK_DAMAGE, keyStarBonus);
            if (currentStars > 0) {
                double bonus = getBaseDamage(item.getType()) + currentStars * 1.0 - 1;
                try {
                    AttributeModifier starMod = new AttributeModifier(
                            keyStarBonus,
                            bonus,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.MAINHAND
                    );
                    meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, starMod);
                } catch (UnsupportedOperationException ignored) {}
            }

            item.setItemMeta(meta);
        }


        if (isHelmet(item.getType())) {
            // ARMOR_TOUGHNESS star modifier 제거 (if present)
            removeModifiersByKey(meta, Attribute.ARMOR_TOUGHNESS, keyStarBonus);

            // ARMOR 고정 값 제거 및 추가
            removeModifiersByKey(meta, Attribute.ARMOR, keyFixedArmor);
            try {
                double baseArmor = getBaseArmor(item.getType());
                AttributeModifier armorMod = new AttributeModifier(
                        keyFixedArmor,
                        baseArmor,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.HEAD
                );
                meta.addAttributeModifier(Attribute.ARMOR, armorMod);
            } catch (UnsupportedOperationException ignored) {}

            // KNOCKBACK_RESISTANCE 고정 제거 및 추가
            removeModifiersByKey(meta, Attribute.KNOCKBACK_RESISTANCE, keyFixedKnockback);
            try {
                double baseKnockback = getBaseKnockbackResistance(item.getType());
                AttributeModifier knockbackMod = new AttributeModifier(
                        keyFixedKnockback,
                        baseKnockback,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.HEAD
                );
                meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, knockbackMod);
            } catch (UnsupportedOperationException ignored) {}

            // 별 보정 추가 (ARMOR_TOUGHNESS)
            if (currentStars > 0) {
                try {
                    double bonus = getBaseArmorToughness(item.getType()) + currentStars * 1.0 - 1;
                    AttributeModifier starMod = new AttributeModifier(
                            keyStarBonus,
                            bonus,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.HEAD
                    );
                    meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, starMod);
                } catch (UnsupportedOperationException ignored) {}
            }

            item.setItemMeta(meta);
        }

        if(isChestplate(item.getType())){
            removeModifiersByKey(meta, Attribute.MAX_HEALTH, keyStarBonus);

            // ARMOR_TOUGHNESS 고정 값 제거 및 추가
            removeModifiersByKey(meta, Attribute.ARMOR_TOUGHNESS, keyFixedArmorTough);
            try {
                double baseArmorTough = getBaseArmorToughness(item.getType());
                AttributeModifier armorToughMod = new AttributeModifier(
                        keyFixedArmorTough,
                        baseArmorTough,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.CHEST
                );
                meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, armorToughMod);
            } catch (UnsupportedOperationException ignored) {}

            // ARMOR 고정 값 제거 및 추가
            removeModifiersByKey(meta, Attribute.ARMOR, keyFixedArmor);
            try {
                double baseArmor = getBaseArmor(item.getType());
                AttributeModifier armorMod = new AttributeModifier(
                        keyFixedArmor,
                        baseArmor,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.CHEST
                );
                meta.addAttributeModifier(Attribute.ARMOR, armorMod);
            } catch (UnsupportedOperationException ignored) {}

            // KNOCKBACK_RESISTANCE 고정 제거 및 추가
            removeModifiersByKey(meta, Attribute.KNOCKBACK_RESISTANCE, keyFixedKnockback);
            try {
                double baseKnockback = getBaseKnockbackResistance(item.getType());
                AttributeModifier knockbackMod = new AttributeModifier(
                        keyFixedKnockback,
                        baseKnockback,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.CHEST
                );
                meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, knockbackMod);
            } catch (UnsupportedOperationException ignored) {}

            // 별 보정 추가 (MAX_HEALTH)
            if (currentStars > 0) {
                try {
                    double bonus =  currentStars * 2.0 - 1;
                    AttributeModifier starMod = new AttributeModifier(
                            keyStarBonus,
                            bonus,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.CHEST
                    );
                    meta.addAttributeModifier(Attribute.MAX_HEALTH, starMod);
                } catch (UnsupportedOperationException ignored) {}
            }

            item.setItemMeta(meta);

        }
    }

    /**
     * meta에서 특정 Attribute의 modifier들 중 namespaced key가 일치하는 것들을 제거합니다.
     */
    private void removeModifiersByKey(ItemMeta meta, Attribute attribute, NamespacedKey key) {
        Collection<AttributeModifier> existing = meta.getAttributeModifiers(attribute);
        if (existing == null || existing.isEmpty()) return;
        for (AttributeModifier mod : new ArrayList<>(existing)) {
            NamespacedKey mk = mod.getKey();
            if (mk != null && mk.equals(key)) {
                try { meta.removeAttributeModifier(attribute, mod); }
                catch (UnsupportedOperationException ignored) {}
            }
        }
    }
}