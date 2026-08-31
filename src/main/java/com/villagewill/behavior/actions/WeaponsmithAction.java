package com.villagewill.behavior.actions;

import com.villagewill.Config;
import com.villagewill.capability.VillagerJobMemory;
import com.villagewill.util.TierUpgrade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 功能5：武器匠强化警卫
 * - 升级主手剑/斧（石→铁→钻石→下界合金，层级上限=职业等级），附魔不保留
 * - 弩只修不升；弓只修不升（警卫默认无弓，玩家主动给弓时仅维修）
 * - 维修一律无消耗，不限次，保留附魔
 * - 每日升级 1 次
 */
public final class WeaponsmithAction implements ProfessionAction {
    public static final String ACTION_ID = "weaponsmith";

    @Override
    public String id() {
        return ACTION_ID;
    }

    @Override
    public int xpPerAction() {
        return Config.WEAPONSMITH_XP_PER_ACTION.get();
    }

    @Override
    public int dailyUses(int villagerLevel) {
        return Config.WEAPONSMITH_USES_PER_DAY.get();
    }

    @Override
    public boolean canApplyTo(Guard guard, int villagerLevel) {
        ItemStack weapon = guard.guardInventory.getItem(5);
        if (weapon.isEmpty()) return false;
        if (TierUpgrade.isUpgradeableWeapon(weapon)) {
            int tier = TierUpgrade.weaponTierOf(weapon);
            if (tier >= 0 && tier < TierUpgrade.maxWeaponTier(villagerLevel)) return true;
        } else if (!isRepairableWeapon(weapon)) {
            return false; // 其他物品不处理
        }
        return isWorn(weapon);
    }

    @Override
    public boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory) {
        ItemStack weapon = guard.guardInventory.getItem(5);
        if (weapon.isEmpty()) return false;

        // 1) 剑/斧升级（每日限次，附魔不保留）
        if (TierUpgrade.isUpgradeableWeapon(weapon)) {
            int tier = TierUpgrade.weaponTierOf(weapon);
            int maxTier = TierUpgrade.maxWeaponTier(villager.getVillagerData().getLevel());
            int daily = dailyUses(villager.getVillagerData().getLevel());
            if (tier >= 0 && tier < maxTier && memory.usesFor(ACTION_ID, daily) > 0) {
                ItemStack next = new ItemStack(TierUpgrade.nextWeaponTier(weapon));
                if (!next.isEmpty()) {
                    guard.guardInventory.setItem(5, next);
                    memory.consumeUse(ACTION_ID, daily);
                    return true;
                }
            }
        }
        // 2) 维修（弩/弓/剑/斧均可，无消耗、不限次、保留附魔）
        if (isRepairableWeapon(weapon) && isWorn(weapon)) {
            weapon.setDamageValue(0);
            return true;
        }
        return false;
    }

    private static boolean isRepairableWeapon(ItemStack stack) {
        return TierUpgrade.isUpgradeableWeapon(stack)
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem;
    }

    private static boolean isWorn(ItemStack stack) {
        int threshold = Config.WEAPONSMITH_REPAIR_THRESHOLD.get();
        return stack.isDamaged() && stack.getDamageValue() * 100 >= stack.getMaxDamage() * threshold;
    }
}
