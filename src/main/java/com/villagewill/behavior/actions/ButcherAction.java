package com.villagewill.behavior.actions;

import com.villagewill.Config;
import com.villagewill.capability.VillagerJobMemory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 功能8：屠夫给警卫牛排
 * - 1级1块、2级2块、3级3块、4级4块、5级5块
 * - 每日2次
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
    public boolean canApplyTo(Guard guard, int villagerLevel) {
        return !ActionUtil.hasItem(guard, Items.COOKED_BEEF);
    }

    @Override
    public boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory) {
        int count = Config.levelValue(Config.BUTCHER_STEAK_PER_LEVEL.get(), villager.getVillagerData().getLevel());
        int slot = ActionUtil.firstEmptySlot(guard.guardInventory, 0, 4); // 槽0-3，不占副手/主手
        if (slot < 0) return false;
        guard.guardInventory.setItem(slot, new ItemStack(Items.COOKED_BEEF, count));
        memory.consumeUse(ACTION_ID);
        return true;
    }
}
