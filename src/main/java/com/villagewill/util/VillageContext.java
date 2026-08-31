package com.villagewill.util;

import com.villagewill.Config;
import com.villagewill.compat.GuardCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
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

    /** 村庄代表点：最近的钟（MEETING POI），无钟则用给定位置 */
    public static BlockPos villageCenter(ServerLevel level, BlockPos pos) {
        return level.getPoiManager()
                .findClosest(holder -> holder.is(PoiTypes.MEETING), pos, 64, PoiManager.Occupancy.ANY)
                .orElse(pos.immutable());
    }

    /** 地表高度（从给定 y 向下找第一个非空气方块的上方） */
    public static int surfaceY(ServerLevel level, BlockPos pos) {
        int y = Math.min(pos.getY(), level.getHeight());
        for (; y > level.getMinBuildHeight() + 1; y--) {
            net.minecraft.world.level.block.state.BlockState s =
                    level.getBlockState(new BlockPos(pos.getX(), y, pos.getZ()));
            if (!s.isAir() && s.getFluidState().isEmpty()) return y + 1;
        }
        return pos.getY();
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
