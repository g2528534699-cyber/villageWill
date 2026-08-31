package com.villagewill.behavior.actions;

import com.villagewill.Config;
import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.GuardBuffState;
import com.villagewill.capability.VillagerJobMemory;
import com.villagewill.util.TierUpgrade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 功能2：盔甲匠强化警卫
 * - 空甲位：发一件层级=职业等级上限的甲（无附魔）
 * - 已有甲：升级到下一层级（皮革→锁链→铁→钻石→下界合金），附魔不保留
 * - 维修：恢复耐久（保留附魔），无消耗
 * - 每日升级次数 = usesBase + usesPerLevel×(等级-1)（1级默认2次）；维修不限次
 */
public final class ArmorerAction implements ProfessionAction {
    public static final String ACTION_ID = "armorer";

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @Override
    public String id() {
        return ACTION_ID;
    }

    @Override
    public int xpPerAction() {
        return Config.ARMORER_XP_PER_ACTION.get();
    }

    @Override
    public int dailyUses(int villagerLevel) {
        return Config.armorerUsesPerDay(villagerLevel);
    }

    @Override
    public boolean canApplyTo(Guard guard, int villagerLevel) {
        int maxTier = TierUpgrade.maxArmorTier(villagerLevel);
        SimpleContainer inv = guard.guardInventory;
        int threshold = Config.ARMORER_REPAIR_THRESHOLD.get();
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (stack.isEmpty()) return true;
            if (stack.getItem() instanceof ArmorItem) {
                int tier = TierUpgrade.armorTierOf(stack);
                if (tier >= 0 && tier < maxTier) return true;
                if (isWorn(stack, threshold)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory) {
        int villagerLevel = villager.getVillagerData().getLevel();
        int maxTier = TierUpgrade.maxArmorTier(villagerLevel);
        int threshold = Config.ARMORER_REPAIR_THRESHOLD.get();
        int daily = dailyUses(villagerLevel);
        SimpleContainer inv = guard.guardInventory;
        GuardBuffState state = CapabilityRegistry.guardStateOf(guard).orElse(null);

        // 1) 空甲位发甲（消耗每日次数）
        if (memory.usesFor(ACTION_ID, daily) > 0) {
            for (int slot = 0; slot < 4; slot++) {
                if (inv.getItem(slot).isEmpty()) {
                    Item armor = TierUpgrade.armorItemForTier(maxTier, ARMOR_SLOTS[slot]);
                    if (armor != null) {
                        inv.setItem(slot, new ItemStack(armor));
                        markUpgraded(state, slot);
                        memory.consumeUse(ACTION_ID, daily);
                        return true;
                    }
                }
            }
            // 2) 已有甲升级到下一层级（消耗每日次数，附魔不保留=新装备）
            for (int slot = 0; slot < 4; slot++) {
                ItemStack stack = inv.getItem(slot);
                if (stack.getItem() instanceof ArmorItem) {
                    int tier = TierUpgrade.armorTierOf(stack);
                    if (tier >= 0 && tier < maxTier) {
                        Item next = TierUpgrade.armorItemForTier(tier + 1, ARMOR_SLOTS[slot]);
                        if (next != null) {
                            inv.setItem(slot, new ItemStack(next));
                            markUpgraded(state, slot);
                            memory.consumeUse(ACTION_ID, daily);
                            return true;
                        }
                    }
                }
            }
        }
        // 3) 维修最破的一件（不限次、无消耗，保留附魔）
        int worstSlot = -1;
        int worstDamage = -1;
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (stack.getItem() instanceof ArmorItem && stack.isDamaged()) {
                int damage = stack.getDamageValue();
                if (damage > worstDamage) {
                    worstDamage = damage;
                    worstSlot = slot;
                }
            }
        }
        if (worstSlot >= 0 && isWorn(inv.getItem(worstSlot), threshold)) {
            inv.getItem(worstSlot).setDamageValue(0);
            return true;
        }
        return false;
    }

    private static boolean isWorn(ItemStack stack, int thresholdPercent) {
        return stack.isDamaged() && stack.getDamageValue() * 100 >= stack.getMaxDamage() * thresholdPercent;
    }

    private static void markUpgraded(GuardBuffState state, int slot) {
        if (state != null) state.armorTiers[slot] = Math.max(state.armorTiers[slot] + 1, 0);
    }
}
