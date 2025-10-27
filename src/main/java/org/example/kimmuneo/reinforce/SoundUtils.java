package org.example.kimmuneo.reinforce;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtils {


    public static void playForceSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.85f, 1.5f);
    }
    public static void playDestorySound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 0.85f, 1.5f);
    }

    public static void playEnchantSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.85f, 1.5f);
    }

}
