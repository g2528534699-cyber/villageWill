package com.villagewill.village;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * 村庄核心区块强加载（原版 setChunkForced，持久化到 level 数据，服务器重启自动恢复）
 */
public final class CoreChunkLoader {
    private CoreChunkLoader() {
    }

    /** 强加载核心周围 (2r+1)×(2r+1) 区块 */
    public static void forceLoadCore(ServerLevel level, BlockPos corePos) {
        int radius = 1;
        int cx = corePos.getX() >> 4;
        int cz = corePos.getZ() >> 4;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                level.setChunkForced(cx + dx, cz + dz, true);
            }
        }
    }

    /** 释放核心强加载 */
    public static void releaseCore(ServerLevel level, BlockPos corePos) {
        int radius = 1;
        int cx = corePos.getX() >> 4;
        int cz = corePos.getZ() >> 4;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                level.setChunkForced(cx + dx, cz + dz, false);
            }
        }
    }
}
