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
            Material.IRON_SWORD,
            Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD
            );

    public static final Set<Material> ALL_ARMOR = Set.of(
            //방어구 (Armor)

            // 가죽 (Leather)
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS,

            // 철 (Iron)
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS,

            // 금 (Gold)
            Material.GOLDEN_HELMET,
            Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_BOOTS,

            // 다이아몬드 (Diamond)
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,

            // 네더라이트 (Netherite)
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS,

            //사슬 (Chainmail)
            Material.CHAINMAIL_HELMET,
            Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS,
            Material.CHAINMAIL_BOOTS
    );

    public static final Set<Material> ALL_PICKAXE = Set.of(
            //곡괭이 (Pickaxes)
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE
            );

    public static final Set<Material> ALL_AXE = Set.of(
            //도끼 (Axes)
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE
            );

    public static final Set<Material> ALL_SHOVEL = Set.of(
            //삽 (Shovels)
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL
            );

    public static final Set<Material> ALL_HOE = Set.of(
            //괭이 (Hoes)
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_HOE
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

    public static final Set<Material> ALL_ENCHANT=
            Stream.of(ALL_SWORD, ALL_ARMOR, ALL_AXE, ALL_PICKAXE, ALL_SHOVEL, ALL_HOE, ALL_TOOL)
                    .flatMap(Set::stream)
                    .collect(Collectors.toUnmodifiableSet());

    public static boolean isTool(Material material){
        return ALL_ENCHANT.contains(material);
    }
}
