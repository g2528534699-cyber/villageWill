package com.villagewill.behavior.actions;

import com.villagewill.Config;
import com.villagewill.behavior.GuardFoodLogic;
import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.GuardBuffState;
import com.villagewill.capability.VillagerJobMemory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 功能8：屠夫给警卫牛排
 * - 1级1块、2级2块、3级3块、4级4块、5级5块
 * - 放入专用食物槽（§4.2，不占装备槽）；每日2次
 */
public final class ButcherAction implements ProfessionAction {
    public static final String ACTION_ID = "butcher";

    @Override
    public String id() {
        return ACTION_ID;
    }

    @Override
    public int xpPerAction() {
        return Config.BUTCHER_XP_PER_ACTION.get();
    }

    @Override
    public int dailyUses(int villagerLevel) {
        return Config.BUTCHER_USES_PER_DAY.get();
    }

    @Override
    public boolean canApplyTo(Guard guard, int villagerLevel) {
        GuardBuffState state = CapabilityRegistry.guardStateOf(guard).orElse(null);
        return state != null && !GuardFoodLogic.hasFood(state, Items.COOKED_BEEF);
    }

    @Override
    public boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory) {
        GuardBuffState state = CapabilityRegistry.guardStateOf(guard).orElse(null);
        if (state == null) return false;
        int count = Config.levelValue(Config.BUTCHER_STEAK_PER_LEVEL.get(), villager.getVillagerData().getLevel());
        if (!GuardFoodLogic.addFood(state, new ItemStack(Items.COOKED_BEEF, count))) return false;
        memory.consumeUse(ACTION_ID, dailyUses(villager.getVillagerData().getLevel()));
        return true;
    }
}
