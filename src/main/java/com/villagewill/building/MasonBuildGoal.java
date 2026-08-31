package com.villagewill.building;

import com.villagewill.Config;
import com.villagewill.behavior.ActionEffects;
import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.VillagerJobMemory;
import com.villagewill.util.VillageContext;
import com.villagewill.village.VillageState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 石匠建造小屋 AI（阶段二）
 * - 每天最多 1 栋；一个村庄 ≤15 栋（VillageState）
 * - 选址：以村庄中心为原点按正方形网格填充（side=ceil(sqrt(数量))）→ 村庄轮廓趋向正方形
 * - 小屋：圆石墙+木板屋顶+门洞+2 个随机职业方块（全随机，不固定切石机），无床
 * - 到达后一次性整平并建造，记录到 VillageState
 */
public class MasonBuildGoal extends Goal {
    public static final String ACTION_ID = "mason";

    private final Villager villager;
    private BlockPos villageKey;
    private BlockPos plotCenter;
    private boolean built;
    private int cooldown;

    public MasonBuildGoal(Villager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(villager.level() instanceof ServerLevel level)) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (villager.isBaby() || !Config.MASON_ENABLED.get()) return false;
        if (villager.getVillagerData().getProfession() != VillagerProfession.MASON) return false;
        VillagerJobMemory mem = CapabilityRegistry.jobOf(villager).orElse(null);
        if (mem == null || mem.usesFor(ACTION_ID, Config.MASON_HOUSES_PER_DAY.get()) <= 0) return false;

        this.villageKey = VillageContext.villageCenter(level, villager.blockPosition());
        VillageState state = VillageState.get(level, villageKey);
        if (!state.canBuild()) return false;
        BlockPos plot = findPlot(level, state, villageKey);
        if (plot == null) return false;
        this.plotCenter = plot;
        this.built = false;
        com.mojang.logging.LogUtils.getLogger().info("[VW] 石匠开始建房: 村庄={} 地块={}", villageKey, plot);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !built && plotCenter != null && villager.isAlive();
    }

    @Override
    public void start() {
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        pathToPlot();
    }

    @Override
    public void tick() {
        if (built || !(villager.level() instanceof ServerLevel level)) return;
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        if (villager.blockPosition().distSqr(plotCenter) <= 16.0D) {
            buildHouse(level, VillageState.get(level, villageKey));
            built = true;
        } else {
            pathToPlot();
        }
    }

    @Override
    public void stop() {
        this.plotCenter = null;
        this.built = false;
        this.cooldown = 400 + villager.getRandom().nextInt(400);
    }

    // ---------------- 选址 ----------------

    /** 正方形网格填充：side=ceil(sqrt(已建+1))，i 号地块在 (i%side, i/side)，中心对齐 */
    private BlockPos findPlot(ServerLevel level, VillageState state, BlockPos center) {
        int count = state.houseCount();
        int side = (int) Math.ceil(Math.sqrt(count + 1));
        int ix = count % side;
        int iz = count / side;
        int spacing = Config.MASON_HUT_SPACING.get();
        int x = center.getX() + (ix - (side - 1) / 2) * spacing;
        int z = center.getZ() + (iz - (side - 1) / 2) * spacing;
        int y = VillageContext.surfaceY(level, new BlockPos(x, center.getY(), z));
        return new BlockPos(x, y, z);
    }

    // ---------------- 建造 ----------------

    private void buildHouse(ServerLevel level, VillageState state) {
        int size = Config.MASON_HUT_SIZE.get();
        int half = size / 2;
        int gy = plotCenter.getY();
        int roofY = gy + 3;

        // 1) 整平 size×size 地面并铺地板（地面层 gy-1）
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                BlockPos p = plotCenter.offset(dx, 0, dz);
                // 清理屋顶高度以上的方块
                for (int y = roofY + 1; y <= roofY + 6; y++) {
                    BlockPos q = new BlockPos(p.getX(), y, p.getZ());
                    if (!level.getBlockState(q).isAir()) {
                        level.setBlock(q, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
                // 地面层以下填土
                BlockPos below = new BlockPos(p.getX(), gy - 1, p.getZ());
                BlockState bs = level.getBlockState(below);
                if (bs.isAir() || !bs.getFluidState().isEmpty()) {
                    level.setBlock(below, Blocks.DIRT.defaultBlockState(), 3);
                }
                // 地面层清空（墙/内部分别放）
                BlockPos ground = new BlockPos(p.getX(), gy, p.getZ());
                if (!level.getBlockState(ground).isAir()) {
                    level.setBlock(ground, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        // 2) 墙壁（高3格）+ 屋顶（第4层）+ 门洞（南面中间 1×2）
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                boolean wallX = Math.abs(dx) == half;
                boolean wallZ = Math.abs(dz) == half;
                boolean door = dx == 0 && dz == half; // 南面门洞
                for (int dy = 0; dy < 3; dy++) {
                    if (!wallX && !wallZ) continue; // 内部
                    if (door && dy < 2) continue;   // 门洞 1×2
                    level.setBlock(plotCenter.offset(dx, dy, dz), Blocks.COBBLESTONE.defaultBlockState(), 3);
                }
                level.setBlock(plotCenter.offset(dx, 3, dz), Blocks.OAK_PLANKS.defaultBlockState(), 3);
            }
        }

        // 3) 内部 2 个随机职业方块（不重复）
        List<Block> pool = jobBlockPool();
        if (pool.size() >= 2) {
            Block b1 = pool.get(level.random.nextInt(pool.size()));
            Block b2;
            do {
                b2 = pool.get(level.random.nextInt(pool.size()));
            } while (b2 == b1 && pool.size() > 1);
            level.setBlock(plotCenter.offset(1, 0, 1), b1.defaultBlockState(), 3);
            level.setBlock(plotCenter.offset(1, 0, -1), b2.defaultBlockState(), 3);
        }

        // 4) 记录并结算
        state.addHouse(plotCenter);
        VillagerJobMemory mem = CapabilityRegistry.jobOf(villager).orElse(null);
        if (mem != null) {
            mem.consumeUse(ACTION_ID, Config.MASON_HOUSES_PER_DAY.get());
        }
        ActionEffects.playTradeComplete(level, plotCenter.getCenter());
        ActionEffects.grantVillagerXp(villager, Config.MASON_XP_PER_ACTION.get());
    }

    private static List<Block> jobBlockPool() {
        List<Block> result = new ArrayList<>();
        for (String id : Config.MASON_JOB_BLOCKS.get()) {
            Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(id));
            if (block != null && block != Blocks.AIR) result.add(block);
        }
        return result;
    }

    private void pathToPlot() {
        if (plotCenter != null) {
            villager.getNavigation().moveTo(plotCenter.getX() + 0.5D, plotCenter.getY(), plotCenter.getZ() + 0.5D, 0.5D);
        }
    }
}
