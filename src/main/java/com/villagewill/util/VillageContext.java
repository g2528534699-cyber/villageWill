package com.villagewill.util;

import com.villagewill.Config;
import com.villagewill.compat.GuardCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import tallestegg.guardvillagers.entities.Guard;

import java.util.List;
import java.util.Optional;

/**
 * 村庄上下文：村庄范围判定与实体查询（阶段一按村民位置为中心，阶段三接入村庄核心）
 */
public final class VillageContext {
    private VillageContext() {
    }

    public static double radius() {
        return Config.VILLAGE_RADIUS.get();
    }

    /** 村民的家（床）位置 */
    public static Optional<BlockPos> homeOf(Villager villager) {
        return villager.getBrain().getMemory(MemoryModuleType.HOME).map(GlobalPos::pos);
    }

    /** 村庄内警卫列表 */
    public static List<Guard> guardsInVillage(ServerLevel level, BlockPos center) {
        return GuardCompat.guardsNear(level, center, radius());
    }

    /** 村庄内村民列表（不含警卫） */
    public static List<Villager> villagersInVillage(ServerLevel level, BlockPos center) {
        return level.getEntitiesOfClass(Villager.class,
                new net.minecraft.world.phys.AABB(center).inflate(radius()),
                v -> v != null && v.isAlive());
    }

    /** 村庄内村民数量（不含警卫） */
    public static int villagerCount(ServerLevel level, BlockPos center) {
        return villagersInVillage(level, center).size();
    }
}
