package org.example.kimmuneo.reinforce;

import org.bukkit.Material;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AllTool {


    public static final Set<Material> ALL_SWORD = Set.of(
            //검 (Swords)
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.COPPER_SWORD,
            Material.IRON_SWORD,
            Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD
    );

    public static final Set<Material> ALL_HELMET = Set.of(
            Material.LEATHER_HELMET,
            Material.COPPER_HELMET,
            Material.IRON_HELMET,
            Material.GOLDEN_HELMET,
            Material.DIAMOND_HELMET,
            Material.NETHERITE_HELMET,
            Material.CHAINMAIL_HELMET
    );

    public static final Set<Material> ALL_CHESTPLATE = Set.of(
            Material.LEATHER_CHESTPLATE,
            Material.COPPER_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.GOLDEN_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE,
            Material.NETHERITE_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE
    );

    public static final Set<Material> ALL_LEGGINGS = Set.of(
            Material.LEATHER_LEGGINGS,
            Material.COPPER_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.GOLDEN_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.NETHERITE_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS
    );

    public static final Set<Material> ALL_BOOTS = Set.of(
            Material.LEATHER_BOOTS,
            Material.COPPER_BOOTS,
            Material.IRON_BOOTS,
            Material.GOLDEN_BOOTS,
            Material.DIAMOND_BOOTS,
            Material.NETHERITE_BOOTS,
            Material.CHAINMAIL_BOOTS
    );

    public static final Set<Material> ALL_AXE = Set.of(
            //도끼 (Axes)
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.COPPER_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE
    );

    public static final Set<Material> ALL_TOOL = Set.of(
            //활/삼지창/방패/낚싯대 등 기타 도구
            Material.BOW,
            Material.CROSSBOW,
            Material.TRIDENT,
            Material.SHIELD,
            Material.FISHING_ROD,
            Material.SHEARS,
            Material.FLINT_AND_STEEL,
            Material.ELYTRA

    );

    public static final Set<Material> ALL_ENCHANT =
            Stream.of(ALL_SWORD, ALL_HELMET, ALL_CHESTPLATE, ALL_LEGGINGS, ALL_BOOTS, ALL_AXE, ALL_TOOL)
                    .flatMap(Set::stream)
                    .collect(Collectors.toUnmodifiableSet());

    public static final Set<Material> ALL_ARMOR =
            Stream.of(ALL_HELMET, ALL_CHESTPLATE, ALL_LEGGINGS, ALL_BOOTS)
                    .flatMap(Set::stream)
                    .collect(Collectors.toUnmodifiableSet());

    public static boolean isTool(Material material) {
        return ALL_ENCHANT.contains(material);
    }

    public static boolean isSword(Material material) {
        return ALL_SWORD.contains(material);
    }

    public static boolean isArmor(Material material) {
        return ALL_ARMOR.contains(material);
    }

    public static boolean isHelmet(Material material) {
        return ALL_HELMET.contains(material);
    }

    public static boolean isChestplate(Material material) {
        return ALL_CHESTPLATE.contains(material);
    }

    public static boolean isLeggings(Material material) {
        return ALL_LEGGINGS.contains(material);
    }

    public static boolean isBoots(Material material) {
        return ALL_BOOTS.contains(material);
    }

    public static boolean isAxe(Material material) {
        return ALL_AXE.contains(material);
    }
}
