package com.villagewill.behavior.actions;

import com.villagewill.Config;
import com.villagewill.behavior.ActionEffects;
import com.villagewill.capability.VillagerJobMemory;
import com.villagewill.compat.GuardCompat;
import com.villagewill.util.VillageContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import tallestegg.guardvillagers.entities.Guard;

import java.util.List;

/**
 * 功能4：牧师日出时复活（生成）警卫村民
 * - 每天日出时段可执行一次（EnhanceGoal 内做时间判定）
 * - 数量：1级1、3级2、5级3（2级1、4级2）
 * - 上限：guardvillagers 配置的每村庄警卫生成数（guardSpawnInVillage）
 * - 复活=全新生成警卫（默认装备，不保留任何装备/附魔）
 */
public final class ClericAction {
    public static final String ACTION_ID = "cleric";

    private ClericAction() {
    }

    /** 日出时段（23000-24000 tick） */
    public static boolean isDawn(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return time >= 23000L;
    }

    /** 村庄警卫数是否低于复活上限 */
    public static boolean canResurrect(ServerLevel level, BlockPos center) {
        int limit = GuardCompat.villageGuardLimit();
        if (limit <= 0) return false;
        int count = VillageContext.guardsInVillage(level, center).size();
        return count < limit;
    }

    /** 在村庄中心执行复活 */
    public static boolean execute(ServerLevel level, Villager villager, BlockPos center, VillagerJobMemory memory) {
        int limit = GuardCompat.villageGuardLimit();
        if (limit <= 0) return false;
        List<Guard> guards = VillageContext.guardsInVillage(level, center);
        int count = guards.size();
        if (count >= limit) return false;
        int want = Config.levelValue(Config.CLERIC_RESURRECT_PER_LEVEL.get(), villager.getVillagerData().getLevel());
        int toSpawn = Math.min(want, limit - count);
        if (toSpawn <= 0) return false;
        for (int i = 0; i < toSpawn; i++) {
            BlockPos pos = center.offset(level.random.nextInt(5) - 2, 0, level.random.nextInt(5) - 2);
            GuardCompat.spawnFreshGuard(level, pos);
        }
        ActionEffects.playTradeComplete(level, center.getCenter());
        memory.consumeUse(ACTION_ID);
        return true;
    }
}
