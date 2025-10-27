package org.example.kimmuneo.reinforce.Anvil;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static org.example.kimmuneo.reinforce.AllTool.*;

public class StarAttributeApplier {
    private final Plugin plugin;
    private final NamespacedKey keyFixedAttackSpeed;
    private final NamespacedKey keyFixedArmorTough;
    private final NamespacedKey keyFixedArmor;
    private final NamespacedKey keyFixedKnockback;
    private final NamespacedKey keyStarBonus;


    public StarAttributeApplier(Plugin plugin) {
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


    /**
     * 공격력/방어력 관련 AttributeModifier를 적용합니다.
     * 기존 modifier를 키(keyStarBonus 등)로 찾아 안전하게 제거한 뒤 추가합니다.
     */
    public void applyStarDamageModifier(ItemStack item, int currentStars) {
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
                double bonus = getBaseDamage(item.getType()) + currentStars * 1.0;
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
                    double bonus = getBaseArmorToughness(item.getType()) + currentStars * 1.0;
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
                    double bonus =  currentStars * 2.0;
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

        if(isLeggings(item.getType())){
            removeModifiersByKey(meta, Attribute.ARMOR, keyStarBonus);

            // ARMOR_TOUGHNESS 고정 값 제거 및 추가
            removeModifiersByKey(meta, Attribute.ARMOR_TOUGHNESS, keyFixedArmorTough);
            try {
                double baseArmorTough = getBaseArmorToughness(item.getType());
                AttributeModifier armorToughMod = new AttributeModifier(
                        keyFixedArmorTough,
                        baseArmorTough,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.LEGS
                );
                meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, armorToughMod);
            } catch (UnsupportedOperationException ignored) {}

            // KNOCKBACK_RESISTANCE 고정 제거 및 추가
            removeModifiersByKey(meta, Attribute.KNOCKBACK_RESISTANCE, keyFixedKnockback);
            try {
                double baseKnockback = getBaseKnockbackResistance(item.getType());
                AttributeModifier knockbackMod = new AttributeModifier(
                        keyFixedKnockback,
                        baseKnockback,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.LEGS
                );
                meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, knockbackMod);
            } catch (UnsupportedOperationException ignored) {}

            // 별 보정 추가 (ARMOR)
            if (currentStars > 0) {
                try {
                    double bonus =  currentStars * 1.0;
                    AttributeModifier starMod = new AttributeModifier(
                            keyStarBonus,
                            bonus,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.LEGS
                    );
                    meta.addAttributeModifier(Attribute.ARMOR, starMod);
                } catch (UnsupportedOperationException ignored) {}
            }

            item.setItemMeta(meta);

        }

        if(isBoots(item.getType())){
            removeModifiersByKey(meta, Attribute.MOVEMENT_SPEED, keyStarBonus);

            // ARMOR_TOUGHNESS 고정 값 제거 및 추가
            removeModifiersByKey(meta, Attribute.ARMOR_TOUGHNESS, keyFixedArmorTough);
            try {
                double baseArmorTough = getBaseArmorToughness(item.getType());
                AttributeModifier armorToughMod = new AttributeModifier(
                        keyFixedArmorTough,
                        baseArmorTough,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.FEET
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
                        EquipmentSlotGroup.FEET
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
                        EquipmentSlotGroup.FEET
                );
                meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, knockbackMod);
            } catch (UnsupportedOperationException ignored) {}

            // 별 보정 추가 (MOVEMENT_SPEED)
            if (currentStars > 0) {
                try {
                    double bonus =  currentStars * 0.025;
                    AttributeModifier starMod = new AttributeModifier(
                            keyStarBonus,
                            bonus,
                            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                            EquipmentSlotGroup.FEET
                    );
                    meta.addAttributeModifier(Attribute.MOVEMENT_SPEED, starMod);
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
