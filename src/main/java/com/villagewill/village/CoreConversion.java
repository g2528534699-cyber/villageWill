package com.villagewill.village;

import com.villagewill.Config;
import com.villagewill.block.VillageCoreBlock;
import com.villagewill.util.VillageContext;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * 村庄核心转换（阶段三 Part 1）：
 * - 每 N tick 检查村庄（以村民为中心找最近钟）
 * - 村民数（不含警卫）≥ 阈值（默认20）且核心未激活 → 最近钟原地转换为村庄核心
 * - 激活 VillageState（coreActive=true，绿宝石收入/威胁召唤随之生效）
 * - 核心区块强加载（3×3 区块，setChunkForced 持久化）
 */
public final class CoreConversion {
    private CoreConversion() {
    }

    /** 由村民 tick 周期性调用（VillageWillEvents，每 CORE_CONVERT_CHECK_TICKS tick） */
    public static void tick(ServerLevel level, Villager villager) {
        if (villager.tickCount % Config.CORE_CONVERT_CHECK_TICKS.get() != 0) return;

        // 村庄中心：最近的核心 POI 或钟（128 格内），无则跳过
        BlockPos center = VillageContext.villageCenter(level, villager.blockPosition());
        if (!level.getBlockState(center).is(net.minecraft.world.level.block.Blocks.BELL)
                && !level.getBlockState(center).is(com.villagewill.block.VillageCoreBlock.INSTANCE)) {
            return; // 找不到钟/核心
        }

        VillageState state = VillageState.get(level, center);
        if (state.isCoreActive()) return; // 已激活

        int villagers = VillageContext.villagerCount(level, center);
        if (villagers < Config.CORE_VILLAGER_THRESHOLD.get()) return;

        convertBellToCore(level, center, state);
    }

    private static void convertBellToCore(ServerLevel level, BlockPos bellPos, VillageState state) {
        BlockState bellState = level.getBlockState(bellPos);
        if (bellState.getBlock() != Blocks.BELL) return;

        // 原地替换钟为村庄核心
        level.setBlock(bellPos, VillageCoreBlock.INSTANCE.defaultBlockState(), 3);
        CoreRegistry.register(level.dimension(), bellPos); // 已加载区块内 setBlock 不触发 BE.onLoad，手动注册
        state.setCoreActive(true);
        CoreChunkLoader.forceLoadCore(level, bellPos);
        LogUtils.getLogger().info("[VW] 村庄核心已激活: {} 村民数={}", bellPos, state.key());
        // TODO(阶段三 Part 3)：生成警卫队长与 4 名护卫
    }
}
