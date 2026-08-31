package com.villagewill.behavior.actions;

import com.villagewill.capability.VillagerJobMemory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 村民职业动作接口：每个职业一个实现，由 EnhanceGoal 驱动执行
 */
public interface ProfessionAction {
    /** 每日次数计数的动作键 */
    String id();

    /** 单次动作经验（§4.1：约2天升2级折算，按职业配置） */
    int xpPerAction();

    /** 该职业等级对应的每日动作次数 */
    int dailyUses(int villagerLevel);

    /** 该警卫是否值得村民走过去强化 */
    boolean canApplyTo(Guard guard, int villagerLevel);

    /**
     * 执行强化动作（此时村民已在警卫身边）
     *
     * @return 是否成功执行（成功才消耗次数/给经验/播特效）
     */
    boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory);
}
