package org.example.kimmuneo.reinforce;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reinforce extends JavaPlugin {
    @Override
    public void onEnable() {
        // 기존 기능 리스너 등록
        Bukkit.getPluginManager().registerEvents(new EnchantTable(), this);
        Bukkit.getPluginManager().registerEvents(new Anvil(), this);

        // Scarecrow를 커맨드 실행자와 리스너로 등록
        Scarecrow scarecrow = new Scarecrow(this);
        if (this.getCommand("scarecrow") != null) {
            this.getCommand("scarecrow").setExecutor(scarecrow);
            this.getCommand("scarecrow").setTabCompleter(scarecrow);
        } else {
            getLogger().warning("plugin.yml에 scarecrow 커맨드가 등록되어 있지 않습니다.");
        }
        Bukkit.getPluginManager().registerEvents(scarecrow, this);

        getLogger().info("ReinforcePlugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ReinforcePlugin disabled.");
    }

    // /reinforcebook, /spelltrace 명령어 처리
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("reinforcebook")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().addItem(ReinforceItem.createEnchantBook());
                player.sendMessage("§b마법 부여 주문서를 지급했습니다!");
            } else {
                sender.sendMessage("콘솔에서는 사용할 수 없습니다.");
            }
            return true;
        }

        if (label.equalsIgnoreCase("spelltrace")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().addItem(ReinforceItem.createSpellTrace());
                player.sendMessage("§b주문의 흔적을 지급했습니다!");
            } else {
                sender.sendMessage("콘솔에서는 사용할 수 없습니다.");
            }
            return true;
        }

        return false;
    }
}