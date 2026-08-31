package com.villagewill.village;

import com.villagewill.Config;
import com.villagewill.util.VillageContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 村庄核心围墙：
 * - 建墙：以核心为中心方形环墙（半径 config，覆盖小屋网格+余量），4 个方向留通道
 * - 材质随等级：1=圆石 2=石砖 3=深板岩砖（升级时替换已有墙）
 * - 只在空气处放置（不覆盖已有建筑）
 */
public final class WallBuilder {
    private WallBuilder() {
    }

    public static Block wallBlockFor(int level) {
        return switch (level) {
            case 2 -> Blocks.STONE_BRICKS;
            case 3 -> Blocks.DEEPSLATE_BRICKS;
            default -> Blocks.COBBLESTONE;
        };
    }

    /** 建墙或升级材质（由 TechTree 在购买后调用） */
    public static void buildOrUpgrade(ServerLevel level, BlockPos corePos, VillageState state) {
        int wallLv = state.wallLevel();
        if (wallLv <= 0) return;
        Block wallBlock = wallBlockFor(wallLv);

        // 已有墙 → 升级材质（替换方块）
        List<BlockPos> walls = state.wallPositions();
        if (!walls.isEmpty()) {
            for (BlockPos p : walls) {
                BlockState bs = level.getBlockState(p);
                if (bs.getBlock() != wallBlock && !bs.isAir()) {
                    level.setBlock(p, wallBlock.defaultBlockState(), 3);
                }
            }
            return;
        }

        // 首次建造
        int radius = Config.WALL_RADIUS.get();
        int height = Config.WALL_HEIGHT.get();
        int gateHalf = Config.WALL_GATE_WIDTH.get() / 2;
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
        for (int y = 0; y < height; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    boolean edgeX = Math.abs(x) == radius;
                    boolean edgeZ = Math.abs(z) == radius;
                    if (!edgeX && !edgeZ) continue;
                    // 通道：4 个方向，宽 gateWidth（中心对齐）
                    boolean gate = (edgeZ && Math.abs(x) <= gateHalf)
                            || (edgeX && Math.abs(z) <= gateHalf);
                    if (gate) continue;
                    int groundY = VillageContext.surfaceY(level,
                            new BlockPos(corePos.getX() + x, corePos.getY(), corePos.getZ() + z));
                    mp.set(corePos.getX() + x, groundY + y, corePos.getZ() + z);
                    if (level.getBlockState(mp).isAir()) {
                        level.setBlock(mp, wallBlock.defaultBlockState(), 3);
                        state.addWallPos(mp.immutable());
                    }
                }
            }
        }
        com.mojang.logging.LogUtils.getLogger().info("[VW] 村庄围墙建成: 半径={} 高度={} 材质={}",
                radius, height, wallBlock.getDescriptionId());
    }
}
