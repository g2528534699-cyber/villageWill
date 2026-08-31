package com.villagewill.village;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

/**
 * 村庄核心注册表（服务端内存）：核心方块实体加载时注册，卸载时移除
 * 用于村庄中心查找（核心激活后 POI 钟消失，改由核心方块定位村庄）
 */
public final class CoreRegistry {
    private static final Set<CoreKey> ACTIVE = new HashSet<>();

    private record CoreKey(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private CoreRegistry() {
    }

    public static void register(ResourceKey<Level> dimension, BlockPos pos) {
        ACTIVE.add(new CoreKey(dimension, pos.immutable()));
        com.mojang.logging.LogUtils.getLogger().info("[VW] 核心注册: {} 数量={}", pos, ACTIVE.size());
    }

    public static void unregister(ResourceKey<Level> dimension, BlockPos pos) {
        ACTIVE.remove(new CoreKey(dimension, pos.immutable()));
    }

    /** 最近的核心位置（同维度，范围内），无则 null */
    public static BlockPos nearest(ResourceKey<Level> dimension, BlockPos from, double maxDist) {
        BlockPos best = null;
        double bestDist = maxDist * maxDist;
        for (CoreKey key : ACTIVE) {
            if (!key.dimension().equals(dimension)) continue;
            double d = from.distSqr(key.pos());
            if (d < bestDist) {
                bestDist = d;
                best = key.pos();
            }
        }
        return best;
    }
}
