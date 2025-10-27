package org.example.kimmuneo.reinforce;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.example.kimmuneo.reinforce.AllTool.isTool;

public class EnchantTable implements Listener {

    private static final String TABLE_NAME = "마법 부여 재설정";
    private static final ChatColor TABLE_COLOR = ChatColor.BLACK;

    private static final int INV_SIZE = 9;
    private static final int INPUT_SLOT = 4;

    // GUI 배경용 빈 칸으로 사용할 ItemStack (단일 인스턴스 재사용)
    private final ItemStack filler;

    public EnchantTable() {
        filler = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta fm = filler.getItemMeta();
        if (fm != null) {
            // 시각적으로 눈에 거슬리지 않도록 공백 이름 설정
            fm.setDisplayName(" ");
            filler.setItemMeta(fm);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();

        if (block.getType() == Material.ENCHANTING_TABLE) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            Inventory inv = Bukkit.createInventory(null, INV_SIZE, TABLE_COLOR + TABLE_NAME);

            // 입력 슬롯을 제외한 모든 슬롯을 filler로 채움 (같은 ItemStack 재사용)
            for (int i = 0; i < INV_SIZE; i++) {
                if (i == INPUT_SLOT) continue;
                inv.setItem(i, filler);
            }

            player.openInventory(inv);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TABLE_COLOR + TABLE_NAME)) return;

        int slot = event.getRawSlot();


        // 입력 슬롯에서 우클릭했을 때만 리롤 실행 허용
        if (slot == INPUT_SLOT && event.isRightClick()) {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getInventory().getItem(INPUT_SLOT);

            if (hasLoreLine(item, "★") ) {
                player.sendMessage("§c이미 강화가 진행된 상태입니다!");
                event.setCancelled(true);
                return;
            }

            if (item == null || item.getType() == Material.AIR) {
                event.setCancelled(true);
                return;
            }

            if (!isTool(item.getType())) {
                player.sendMessage("§c인챈트가 가능한 아이템이 아닙니다!");
                event.setCancelled(true);
                return;
            }

            int bookSlot = findNamedBookSlot(player, "§6마법 부여 주문서");
            if (bookSlot == -1) {
                player.sendMessage("§c마법 부여 주문서가 필요합니다!");
                event.setCancelled(true);
                return;
            }

            // 책 1개 소모
            consumeOne(player, bookSlot);

            // 실제 리롤 수행
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                rerollEnchants(item, meta);
                // 메타를 명시적으로 다시 설정 (명확성을 위해)
                item.setItemMeta(meta);
                // 소리 재생
                SoundUtils.playEnchantSound(player);
            }

            event.setCancelled(true);
            return;
        }

        // 입력 슬롯을 제외한 GUI 내 다른 클릭 차단
        if (slot >= 0 && slot < INV_SIZE && slot != INPUT_SLOT) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(TABLE_COLOR + TABLE_NAME)) return;

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



    // --- 헬퍼 / 핵심 로직 ---

    /**
     * 주어진 정확한 표시 이름을 가진 BOOK의 첫 슬롯 인덱스를 찾습니다.
     * 찾지 못하면 -1을 반환합니다.
     */
    private int findNamedBookSlot(Player player, String displayName) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack it = player.getInventory().getItem(i);
            if (it == null || it.getType() != Material.BOOK) continue;
            ItemMeta m = it.getItemMeta();
            if (m != null && m.hasDisplayName() && m.getDisplayName().equals(displayName)) return i;
        }
        return -1;
    }

    // 플레이어 인벤토리의 해당 슬롯에서 아이템 1개만 소모합니다.
    private void consumeOne(Player player, int slot) {
        ItemStack it = player.getInventory().getItem(slot);
        if (it == null) return;
        if (it.getAmount() > 1) it.setAmount(it.getAmount() - 1);
        else player.getInventory().setItem(slot, null);
    }

    /**
     * 주어진 아이템(ItemStack)과 메타(ItemMeta)를 사용해 인챈트를 리롤합니다.
     * - 최소 1줄의 인챈트를 보장하며, 낮은 확률로 줄 수가 1만큼 증가할 수 있습니다.
     * - 아이템에 적용 가능한 인챈트 후보군에서 무작위로 선택합니다 (가능하면 Enchantment.canEnchantItem 사용).
     * - 레벨은 가중치 분포에 따라 선택되며, 인챈트의 최대 레벨에 따라 조정됩니다.
     */
    private void rerollEnchants(ItemStack item, ItemMeta meta) {
        // 아이템에 호환되는 인챈트 후보군을 준비
        List<Enchantment> candidates = Arrays.stream(Enchantment.values())
                .filter(Objects::nonNull)
                .filter(e -> {
                    try {
                        return e.canEnchantItem(item);
                    } catch (NoSuchMethodError | UnsupportedOperationException ex) {
                        // 일부 플랫폼/버전에서 canEnchantItem(ItemStack)를 지원하지 않을 수 있으므로,
                        // 예외 발생 시 안전하게 허용하도록 폴백 처리 (이전 동작 유지)
                        return true;
                    }
                })
                .collect(Collectors.toList());

        // 선택할 후보가 없으면 중단
        if (candidates.isEmpty()) return;

        // 현재 인챈트 "줄 수" (최소 1)
        int currentLines = Math.max(1, meta.getEnchants().size());
        final int MAX_LINES = 4;

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // 3% 확률로 줄 수가 증가 (최대 한도까지)
        if (currentLines < MAX_LINES && rnd.nextInt(100) < 3) {
            currentLines++;
        }

        // 기존 인챈트 모두 제거
        for (Enchantment ench : new HashSet<>(meta.getEnchants().keySet())) {
            meta.removeEnchant(ench);
        }

        // 중복 없이 currentLines 개수만큼 인챈트 선택 (안전장치 포함)
        Set<Enchantment> chosen = new HashSet<>();
        int safety = 0;
        final int SAFETY_LIMIT = 100;
        while (chosen.size() < currentLines && safety++ < SAFETY_LIMIT) {
            Enchantment picked = candidates.get(rnd.nextInt(candidates.size()));
            chosen.add(picked);
        }

        // 레벨 선택에 사용할 기본 확률 (레벨 1..5에 대한 가중치)
        double[] baseLevelChances = {0.50, 0.25, 0.15, 0.07, 0.03}; // 합계 1.0

        for (Enchantment ench : chosen) {
            int maxLevel = Math.max(1, ench.getMaxLevel());
            int level = pickLevel(rnd, baseLevelChances, maxLevel);
            try {
                // unsafe 허용(true)로 추가하여 원래 동작을 보존
                meta.addEnchant(ench, level, true);
            } catch (Exception ignored) {
                // 플랫폼별로 드물게 발생할 수 있는 예외 무시
            }
        }
    }

    /**
     * ThreadLocalRandom과 기본 레벨 확률 배열, 인챈트의 최대 레벨을 받아 레벨을 선택합니다.
     * - maxLevel이 baseLevelChances 길이 이하이면 해당 접두사 확률대로 선택합니다.
     * - maxLevel이 base 길이보다 크면, 남은 확률을 초과 레벨에 균등 배분합니다.
     */
    private int pickLevel(ThreadLocalRandom rnd, double[] baseLevelChances, int maxLevel) {
        // 레벨 1..maxLevel에 대한 유효한 확률 배열 생성
        double[] probs = new double[maxLevel];
        int baseLen = Math.min(baseLevelChances.length, maxLevel);

        // 사용 가능한 레벨에 대해 기본 확률 복사
        double sum = 0;
        for (int i = 0; i < baseLen; i++) {
            probs[i] = baseLevelChances[i];
            sum += probs[i];
        }

        // 만약 인챈트 최대레벨이 기본 배열 길이보다 크면 남은 확률을 균등 분배
        if (maxLevel > baseLevelChances.length) {
            double remainingPer = (1.0 - sum) / (maxLevel - baseLevelChances.length);
            for (int i = baseLevelChances.length; i < maxLevel; i++) {
                probs[i] = remainingPer;
            }
            sum = 1.0;
        } else if (sum < 1.0) {
            // baseLen < maxLevel 이거나 잘린 경우 정규화
            for (int i = 0; i < baseLen; i++) probs[i] /= sum;
            sum = 1.0;
        }

        // 누적 확률로 레벨 선택
        double r = rnd.nextDouble();
        double cumulative = 0.0;
        for (int i = 0; i < probs.length; i++) {
            cumulative += probs[i];
            if (r <= cumulative) return i + 1;
        }

        // 실패 시 최대 레벨 반환
        return maxLevel;
    }

    public boolean hasLoreLine(ItemStack item, String targetLine) {
        if (item == null || item.getType().isAir()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;

        List<String> lore = meta.getLore();
        if (lore == null) return false;

        // 한 줄이라도 targetLine을 포함하면 true
        for (String line : lore) {
            if (ChatColor.stripColor(line).contains(ChatColor.stripColor(targetLine))) {
                return true;
            }
        }

        return false;
    }

}