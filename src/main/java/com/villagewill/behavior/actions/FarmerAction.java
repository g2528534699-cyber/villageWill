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
 * 功能3：农民给警卫补充面包
 * - 凭空生成（不消耗农民种田所得），放入专用食物槽（§4.2，不占装备槽）
 * - 优先给无食物警卫
 * - 数量/次数：1级6×2、3级10×3、5级14×4（2/4级插值，可配置）
 */
public final class FarmerAction implements ProfessionAction {
    public static final String ACTION_ID = "farmer";

    @Override
    public String id() {
        return ACTION_ID;
    }

    @Override
    public int xpPerAction() {
        return Config.FARMER_XP_PER_ACTION.get();
    }

    @Override
    public int dailyUses(int villagerLevel) {
        return Config.levelValue(Config.FARMER_USES_PER_LEVEL.get(), villagerLevel);
    }

    @Override
    public boolean canApplyTo(Guard guard, int villagerLevel) {
        GuardBuffState state = CapabilityRegistry.guardStateOf(guard).orElse(null);
        return state != null && GuardFoodLogic.hasNoFood(state); // 无食物警卫优先
    }

    @Override
    public boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory) {
        GuardBuffState state = CapabilityRegistry.guardStateOf(guard).orElse(null);
        if (state == null) return false;
        int count = Config.levelValue(Config.FARMER_BREAD_PER_LEVEL.get(), villager.getVillagerData().getLevel());
        if (!GuardFoodLogic.addFood(state, new ItemStack(Items.BREAD, count))) return false;
        memory.consumeUse(ACTION_ID, dailyUses(villager.getVillagerData().getLevel()));
        return true;
    }
}
