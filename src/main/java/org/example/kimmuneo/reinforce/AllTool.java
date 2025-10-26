package org.example.kimmuneo.reinforce;

import org.bukkit.Material;

import java.util.Set;

public class AllTool {
    public static final Set<Material> ALL_TOOL = Set.of(
            //검 (Swords)
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.IRON_SWORD,
            Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD,

            //곡괭이 (Pickaxes)
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE,

            //도끼 (Axes)
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE,

            //삽 (Shovels)
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL,

            //괭이 (Hoes)
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_HOE,

            //활/삼지창/방패/낚싯대 등 기타 도구
            Material.BOW,
            Material.CROSSBOW,
            Material.TRIDENT,
            Material.SHIELD,
            Material.FISHING_ROD,
            Material.SHEARS,
            Material.FLINT_AND_STEEL

    );

    public static boolean isTool(Material material){
        return ALL_TOOL.contains(material);
    }
}
