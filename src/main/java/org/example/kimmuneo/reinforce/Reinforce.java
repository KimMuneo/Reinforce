package org.example.kimmuneo.reinforce;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.example.kimmuneo.reinforce.Anvil.AnvilListener;

import java.util.Objects;

public class Reinforce extends JavaPlugin {

    private static final String CMD_REINFORCEBOOK = "reinforcebook";
    private static final String CMD_SPELLTRACE = "spelltrace";
    private static final String CMD_SCARECROW = "scarecrow";

    @Override
    public void onEnable() {
        Plugin plugin = this;

        // 리스너 등록 (Anvil 최적화 버전은 생성자에 Plugin을 요구하므로 this 전달)
        Bukkit.getPluginManager().registerEvents(new EnchantTable(), plugin);
        Bukkit.getPluginManager().registerEvents(new AnvilListener(plugin), plugin);

        // Scarecrow를 커맨드 실행자와 리스너로 등록
        Scarecrow scarecrow = new Scarecrow(this);
        if (getCommand(CMD_SCARECROW) != null) {
            getCommand(CMD_SCARECROW).setExecutor(scarecrow);
            getCommand(CMD_SCARECROW).setTabCompleter(scarecrow);
            Bukkit.getPluginManager().registerEvents(scarecrow, plugin);
        } else {
            getLogger().warning("plugin.yml에 scarecrow 커맨드가 등록되어 있지 않습니다.");
        }

        getLogger().info("ReinforcePlugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ReinforcePlugin disabled.");
    }

    /**
     * /reinforcebook 와 /spelltrace 명령어 처리
     * command.getName()을 사용해 alias에 관계없이 정확히 처리합니다.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName();

        if (name.equalsIgnoreCase(CMD_REINFORCEBOOK)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("콘솔에서는 사용할 수 없습니다.");
                return true;
            }
            Player player = (Player) sender;
            giveItemToPlayer(player, ReinforceItem.createEnchantBook(), "§b마법 부여 주문서를 지급했습니다!");
            return true;
        }

        if (name.equalsIgnoreCase(CMD_SPELLTRACE)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("콘솔에서는 사용할 수 없습니다.");
                return true;
            }
            Player player = (Player) sender;
            giveItemToPlayer(player, ReinforceItem.createSpellTrace(), "§b주문의 흔적을 지급했습니다!");
            return true;
        }

        return false;
    }

    /**
     * 아이템을 플레이어에게 안전하게 지급합니다.
     * 인벤토리가 가득 찼을 경우 월드에 드롭합니다.
     */
    private void giveItemToPlayer(Player player, org.bukkit.inventory.ItemStack item, String message) {
        Objects.requireNonNull(player, "player");
        if (item == null || item.getType().isAir()) {
            getLogger().warning("지급할 아이템이 비어있습니다.");
            return;
        }

        var leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(it -> player.getWorld().dropItemNaturally(player.getLocation(), it));
        }
        player.sendMessage(message);
    }
}