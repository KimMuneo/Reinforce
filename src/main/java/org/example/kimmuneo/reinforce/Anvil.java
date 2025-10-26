    package org.example.kimmuneo.reinforce;

    import org.bukkit.*;
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

    import java.util.*;

    import static org.example.kimmuneo.reinforce.AllTool.*;

    public class Anvil implements Listener {

        String anvilName = "스타 포스";
        ChatColor anvilColor = ChatColor.GOLD;

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
                case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> 1.0;

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

                Inventory inv = Bukkit.createInventory(null, 9, anvilColor + anvilName);

                ItemStack filler = new ItemStack(Material.ANVIL);
                inv.setItem(8, new ItemStack(Material.PAPER));
                ItemMeta fillerMeta = filler.getItemMeta();
                fillerMeta.setDisplayName(" ");
                filler.setItemMeta(fillerMeta);

                for (int i = 0; i < 9; i++) {
                    if (i == 4) continue;
                    if (i == 8) continue;
                    inv.setItem(i, filler);
                }

                player.openInventory(inv);
            }
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals(anvilColor + anvilName)) return;

            int slot = event.getRawSlot();
            Player player = (Player) event.getWhoClicked();
            Inventory inv = event.getInventory();

            // 슬롯 4의 아이템이 변경될 때 확률 정보 갱신
            if (slot == 4) {
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("Reinforce"), () -> updateChanceDisplay(inv), 1L);
                return;
            }

            // 강화 실행 (좌클릭)
            if (slot == 8 && event.isLeftClick()) {
                ItemStack item = inv.getItem(4);
                if (item == null || item.getType() == Material.AIR) {
                    event.setCancelled(true);
                    return;
                }

                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta != null && meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

                int currentStars = 0;
                int starIndex = -1;
                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);
                    if (line.contains("★") || line.contains("☆")) {
                        currentStars = (int) line.chars().filter(ch -> ch == '★').count();
                        starIndex = i;
                        break;
                    }
                }

                if (!isTool(item.getType())){
                    player.sendMessage("§c인챈트가 가능한 아이템이 아닙니다!");
                    event.setCancelled(true);
                    return;
                }

                if (currentStars >= 10) {
                    player.sendMessage("§e이미 최대 강화(10성)입니다!");
                    event.setCancelled(true);
                    return;
                }

                // 주문의 흔적 확인
                ItemStack spell = null;
                int spellSlot = -1;
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack invItem = player.getInventory().getItem(i);
                    if (invItem != null && invItem.getType() == Material.GLOWSTONE_DUST) {
                        if (invItem.hasItemMeta() && invItem.getItemMeta().hasDisplayName()
                                && invItem.getItemMeta().getDisplayName().equals("§6주문의 흔적")) {
                            spell = invItem;
                            spellSlot = i;
                            break;
                        }
                    }
                }

                if (spell == null) {
                    player.sendMessage("§c주문의 흔적이 필요합니다!");
                    event.setCancelled(true);
                    return;
                }

                // 강화 재료 소모
                if (spell.getAmount() > 1) spell.setAmount(spell.getAmount() - 1);
                else player.getInventory().setItem(spellSlot, null);

                // 강화 확률
                double success = rates[currentStars][0];
                double fail = rates[currentStars][1];
                double down = rates[currentStars][2];
                double destroy = rates[currentStars][3];

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
                    // 모루 4번 슬롯 아이템 파괴
                    inv.setItem(4, new ItemStack(Material.AIR));
                    player.sendMessage("§c💥 아이템이 파괴되었습니다!");
                    playDestorySound(player);
                    event.setCancelled(true);
                    return;
                }

                // 별 갱신
                String newLine = "§6" + "★".repeat(currentStars) + "§7" + "☆".repeat(10 - currentStars);
                if (starIndex >= 0) lore.set(starIndex, newLine);
                else lore.add(newLine);
                if (meta == null) meta = item.getItemMeta();
                meta.setLore(lore);
                item.setItemMeta(meta);

                // 별 수치에 따른 무기 데미지 보정 적용 (별 1개당 공격력 +1)
                applyStarDamageModifier(item, currentStars);

                updateChanceDisplay(inv);
                event.setCancelled(true);
            }

            // GUI 다른 슬롯 클릭 방지
            if (slot >= 0 && slot < 9 && slot != 4) event.setCancelled(true);
        }

        /**
         * 인벤토리의 slot4 아이템을 읽어 강화별 데미지 보정과 확률 표시를 갱신합니다.
         */
        private void updateChanceDisplay(Inventory inv) {
            ItemStack item = inv.getItem(4);

            ItemStack info = new ItemStack(Material.PAPER);
            ItemMeta meta = info.getItemMeta();
            meta.setDisplayName("§e⭐ 강화 확률 정보");

            List<String> lore = new ArrayList<>();

            int currentStars = 0;
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                for (String line : item.getItemMeta().getLore()) {
                    if (line.contains("★") || line.contains("☆")) {
                        currentStars = (int) line.chars().filter(ch -> ch == '★').count();
                        break;
                    }
                }
                // 아이템이 있을 때는 별 수치에 따른 공격력 보정 적용
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

            inv.setItem(8, info);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (event.getView().getTitle().equals(anvilColor + anvilName)) {
                Inventory inv = event.getInventory();
                ItemStack item = inv.getItem(4);
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


        private void applyStarDamageModifier(ItemStack item, int currentStars) {
            if (item == null || item.getType() == Material.AIR) return;
            if (isSword(item.getType())){
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return;

                // 기존 "star_bonus" AttributeModifier 제거 (공격력)
                Collection<AttributeModifier> existing = meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE);
                if (existing != null && !existing.isEmpty()) {
                    for (AttributeModifier mod : new ArrayList<>(existing)) {
                        NamespacedKey key = mod.getKey();
                        if (key != null && key.getKey().equals("star_bonus")) {
                            try { meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE, mod); }
                            catch (UnsupportedOperationException ignored) {}
                        }
                    }
                }

                // 공격속도 고정 (1.6) 적용
                Collection<AttributeModifier> existingSpeed = meta.getAttributeModifiers(Attribute.ATTACK_SPEED);
                if (existingSpeed != null && !existingSpeed.isEmpty()) {
                    for (AttributeModifier mod : new ArrayList<>(existingSpeed)) {
                        NamespacedKey key = mod.getKey();
                        if (key != null && key.getKey().equals("fixed_attack_speed")) {
                            try { meta.removeAttributeModifier(Attribute.ATTACK_SPEED, mod); }
                            catch (UnsupportedOperationException ignored) {}
                        }
                    }
                }

                NamespacedKey speedKey = new NamespacedKey("reinforce", "fixed_attack_speed");
                AttributeModifier speedMod = new AttributeModifier(
                        speedKey,
                        -2.4,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND
                );
                meta.addAttributeModifier(Attribute.ATTACK_SPEED, speedMod);

                // 별이 0이면 공격력 보정 없음
                if (currentStars > 0) {
                    double bonus = getBaseDamage(item.getType()) + currentStars * 1.0 - 1;
                    NamespacedKey key = new NamespacedKey("reinforce", "star_bonus");
                    AttributeModifier starMod = new AttributeModifier(
                            key,
                            bonus,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.MAINHAND
                    );
                    meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, starMod);
                }

                item.setItemMeta(meta);
            }

            if(isHelmet(item.getType())){
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return;

                Collection<AttributeModifier> existing = meta.getAttributeModifiers(Attribute.ARMOR_TOUGHNESS);
                if (existing != null && !existing.isEmpty()) {
                    for (AttributeModifier mod : new ArrayList<>(existing)) {
                        NamespacedKey key = mod.getKey();
                        if (key != null && key.getKey().equals("star_bonus")) {
                            try { meta.removeAttributeModifier(Attribute.ARMOR_TOUGHNESS, mod); }
                            catch (UnsupportedOperationException ignored) {}
                        }
                    }
                }

                // 방어력 고정 적용
                Collection<AttributeModifier> existingArmor = meta.getAttributeModifiers(Attribute.ARMOR);
                if (existingArmor != null && !existingArmor.isEmpty()) {
                    for (AttributeModifier mod : new ArrayList<>(existingArmor)) {
                        NamespacedKey key = mod.getKey();
                        if (key != null && key.getKey().equals("fixed_armor")) {
                            try { meta.removeAttributeModifier(Attribute.ARMOR, mod); }
                            catch (UnsupportedOperationException ignored) {}
                        }
                    }
                }

                NamespacedKey armorKey = new NamespacedKey("reinforce", "fixed_armor");
                double baseArmor = getBaseArmor(item.getType());
                AttributeModifier armorMod = new AttributeModifier(
                        armorKey,
                        baseArmor,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND
                );
                meta.addAttributeModifier(Attribute.ARMOR, armorMod);

                //밀치기 저항 고정 적용
                Collection<AttributeModifier> existingKnockback = meta.getAttributeModifiers(Attribute.KNOCKBACK_RESISTANCE);
                if (existingKnockback != null && !existingKnockback.isEmpty()) {
                    for (AttributeModifier mod : new ArrayList<>(existingKnockback)) {
                        NamespacedKey key = mod.getKey();
                        if (key != null && key.getKey().equals("fixed_knockback")) {
                            try { meta.removeAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, mod); }
                            catch (UnsupportedOperationException ignored) {}
                        }
                    }
                }

                NamespacedKey knockbackKey = new NamespacedKey("reinforce", "fixed_knockback");
                double baseknockback = getBaseKnockbackResistance(item.getType());
                AttributeModifier kncokbackMod = new AttributeModifier(
                        knockbackKey,
                        baseknockback,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND
                );
                meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, kncokbackMod);

                // 별이 0이면  보정 없음
                if (currentStars > 0) {
                    double bonus = getBaseArmorToughness(item.getType()) + currentStars * 1.0 - 1;
                    NamespacedKey key = new NamespacedKey("reinforce", "star_bonus");
                    AttributeModifier starMod = new AttributeModifier(
                            key,
                            bonus,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.MAINHAND
                    );
                    meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, starMod);
                }

                item.setItemMeta(meta);
            }
        }
    }

