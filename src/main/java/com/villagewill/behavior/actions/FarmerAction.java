package com.villagewill.behavior.actions;

import com.villagewill.Config;
import com.villagewill.capability.VillagerJobMemory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 功能3：农民给警卫补充面包
 * - 凭空生成（不消耗农民种田所得）
 * - 优先给无食物警卫
 * - 数量/次数随等级：1级6×2、2级8×2、3级10×3、4级14×4、5级16×5
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
    public boolean canApplyTo(Guard guard, int villagerLevel) {
        return !ActionUtil.hasAnyFood(guard); // 无食物警卫优先
    }

    @Override
    public boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory) {
        int count = Config.levelValue(Config.FARMER_BREAD_PER_LEVEL.get(), villager.getVillagerData().getLevel());
        int slot = ActionUtil.firstEmptySlot(guard.guardInventory, 0, 4); // 槽0-3，不占副手/主手
        if (slot < 0) return false;
        guard.guardInventory.setItem(slot, new ItemStack(Items.BREAD, count));
        memory.consumeUse(ACTION_ID);
        return true;
    }
}
