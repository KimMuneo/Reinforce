package org.example.kimmuneo.reinforce;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class Scarecrow implements Listener, TabExecutor {

    // 메타키 이름
    private static final String META_KEY = "scarecrow_owner";

    private final JavaPlugin plugin;

    public Scarecrow(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // /scarecrow [distance(optional, 기본 2.0)]
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        double distance = 2.0;
        if (args.length >= 1) {
            try {
                distance = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage("거리 인자는 숫자여야 합니다. 예: /scarecrow 3");
                return true;
            }
        }

        // 플레이어 앞에 소환 (플레이어 눈높이 기준)
        Location spawnLoc = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(distance));
        spawnLoc.setY(player.getLocation().getY() - 1.5); // 바닥에 세우고 싶으면 높이 보정

        Entity ent = player.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        if (!(ent instanceof ArmorStand)) {
            player.sendMessage("허수아비 소환 실패.");
            return true;
        }

        ArmorStand stand = (ArmorStand) ent;
        stand.setVisible(true);
        stand.setGravity(false);
        stand.setCustomName("§6Scarecrow");
        stand.setCustomNameVisible(true);
        stand.setSmall(false);
        stand.setMarker(false);
        stand.setArms(true);
        stand.setBasePlate(false);
        // 머리에 호박 씌우기
        stand.getEquipment().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
        // invulnerable로 하면 데미지 이벤트가 발생하지 않으니 false로 둡니다.
        stand.setInvulnerable(false);

        // 메타데이터로 소유자 저장 (플러그인 인스턴스와 함께)
        stand.setMetadata(META_KEY, new FixedMetadataValue(this.plugin, player.getUniqueId().toString()));

        player.sendMessage("허수아비를 소환했습니다. 때려서 데미지를 확인하세요!");
        return true;
    }

    // 탭 완성(간단)
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    // 데미지 이벤트 리스너
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        Entity damager = event.getDamager();

        // 때린 사람이 플레이어인지 확인
        if (!(damager instanceof Player)) return;

        Player player = (Player) damager;

        // 타겟에 우리 메타데이터가 있는지 확인
        if (!target.hasMetadata(META_KEY)) return;

        List<MetadataValue> meta = target.getMetadata(META_KEY);
        if (meta.isEmpty()) return;

        String ownerUuid = meta.get(0).asString();
        // (선택) 소환자만 피해를 확인하게 하려면 아래 체크를 사용:
        // if (!player.getUniqueId().toString().equals(ownerUuid)) return;

        // 입힌 데미지 수치 얻기
        double damage = event.getFinalDamage(); // 최종 적용될 데미지
        // 채팅에 소수점 둘째 자리까지 포맷
        String damageStr = String.format("%.2f", damage);

        // 원하면 액션바로 표시 (서버 버전에 따라 지원 다름)
        try {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent("데미지: " + damageStr));
        } catch (NoClassDefFoundError ignored) {
            // spigot API가 없거나 지원 안하면 무시
        }

        // 허수아비가 파괴되지 않도록 데미지를 취소
        event.setCancelled(true);
    }

    // 우클릭으로 허수아비 제거 기능 추가
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof ArmorStand)) return;

        // 메타데이터로 우리가 만든 허수아비인지 확인
        if (!clicked.hasMetadata(META_KEY)) return;

        Player player = event.getPlayer();
        List<MetadataValue> meta = clicked.getMetadata(META_KEY);
        if (meta.isEmpty()) return;

        String ownerUuid = meta.get(0).asString();

        // 소환자이거나 적절한 권한을 가진 경우에만 제거 가능하게 함
        if (!player.getUniqueId().toString().equals(ownerUuid) && !player.hasPermission("scarecrow.use")) {
            player.sendMessage("§c허수아비를 제거할 권한이 없습니다.");
            event.setCancelled(true);
            return;
        }

        // 허수아비 제거
        clicked.remove();
        player.sendMessage("§a허수아비를 제거했습니다.");
        event.setCancelled(true);
    }
}