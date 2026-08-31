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

    /** 村庄代表点：最近的核心（注册表/扫描已加载区块）→ 钟（MEETING）。找不到返回 null（调用方跳过，避免以任意坐标创建村庄存档） */
    @javax.annotation.Nullable
    public static BlockPos villageCenter(ServerLevel level, BlockPos pos) {
        // 1. 内存注册表（快速路径）
        BlockPos core = com.villagewill.village.CoreRegistry.nearest(level.dimension(), pos, 128);
        if (core != null) return core;
        // 2. 扫描已加载区块中的核心方块实体（重启后兜底，找到即回填注册表）
        BlockPos found = scanLoadedCores(level, pos, 128);
        if (found != null) return found;
        // 3. 钟（未转换的村庄）
        return level.getPoiManager()
                .findClosest(holder -> holder.is(PoiTypes.MEETING), pos, 128, PoiManager.Occupancy.ANY)
                .orElse(null);
    }

    /** 扫描范围内已加载区块中的村庄核心，返回最近者 */
    private static BlockPos scanLoadedCores(ServerLevel level, BlockPos pos, int range) {
        int minX = (pos.getX() - range) >> 4;
        int maxX = (pos.getX() + range) >> 4;
        int minZ = (pos.getZ() - range) >> 4;
        int maxZ = (pos.getZ() + range) >> 4;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (net.minecraft.world.level.block.entity.BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof com.villagewill.block.VillageCoreBlockEntity) {
                        double d = pos.distSqr(be.getBlockPos());
                        if (d < bestDist) {
                            bestDist = d;
                            best = be.getBlockPos();
                        }
                    }
                }
            }
        }
        if (best != null) {
            com.villagewill.village.CoreRegistry.register(level.dimension(), best);
        }
        return best;
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
