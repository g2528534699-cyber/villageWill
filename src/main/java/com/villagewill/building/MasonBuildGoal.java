package com.villagewill.building;

import com.villagewill.Config;
import com.villagewill.behavior.ActionEffects;
import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.VillagerJobMemory;
import com.villagewill.util.VillageContext;
import com.villagewill.village.VillageState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 石匠建造小屋 AI（阶段二，v2 改进）
 * - 每天最多 1 栋；一个村庄 ≤15 栋（VillageState）
 * - 选址：以村庄中心为原点按正方形网格填充（side=ceil(sqrt(数量))）→ 村庄轮廓趋向正方形
 * - 地形：真实地表扫描（跳过树叶/原木）取区域最低值为地基 → 整平（填坑+挖高+门前平地）
 *   → 不再悬浮/埋地，门前与屋内同高，村民可正常进出
 * - 小屋：四角橡木原木柱 + 圆石墙 + 橡木门 + 木板地板 + 南北向斜坡屋顶（顶层台阶）+ 屋内火把
 *   + 2 个随机职业方块（全随机，不固定切石机）
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
        if (villageKey == null) return false; // 无村庄（核心/钟）不干活
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

    /** 正方形网格填充：side=ceil(sqrt(已建+1))，i 号地块在 (i%side, i/side)，中心对齐；
     *  自动避开村庄中心（核心/钟所在地），防止房子包住或整平挖掉钟/核心 */
    private BlockPos findPlot(ServerLevel level, VillageState state, BlockPos center) {
        int spacing = Config.MASON_HUT_SPACING.get();
        int plotRange = Config.MASON_HUT_SIZE.get() / 2 + 2; // 整平范围（含门前延伸）
        int count = state.houseCount();
        for (int attempt = 0; attempt < 12; attempt++) {
            int n = count + attempt;
            int side = (int) Math.ceil(Math.sqrt(n + 1));
            int ix = n % side;
            int iz = n / side;
            int x = center.getX() + (ix - (side - 1) / 2) * spacing;
            int z = center.getZ() + (iz - (side - 1) / 2) * spacing;
            if (Math.abs(x - center.getX()) <= plotRange + 1
                    && Math.abs(z - center.getZ()) <= plotRange + 1) {
                continue; // 与村庄中心重叠 → 换下一个网格位置
            }
            // 从高处向下找真实地表（不受村庄中心高度影响，跳过树）
            int y = groundY(level, x, z, Math.min(center.getY() + 48, level.getMaxBuildHeight()));
            return new BlockPos(x, y, z);
        }
        return null;
    }

    // ---------------- 建造 ----------------

    private void buildHouse(ServerLevel level, VillageState state) {
        int size = Config.MASON_HUT_SIZE.get();
        int half = size / 2;
        int px = plotCenter.getX();
        int pz = plotCenter.getZ();

        // 1) 基准地面：房子主体（±half）内真实地表最低值 → 墙脚永不悬浮
        //    （门前延伸区只整平不参与取高，避免门前低洼把整栋房子拖低）
        int fromY = Math.min(plotCenter.getY() + 48, level.getMaxBuildHeight());
        int gy = Integer.MAX_VALUE;
        int scanR = half + 2;
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                gy = Math.min(gy, groundY(level, px + dx, pz + dz, fromY));
            }
        }
        gy = Math.max(gy, level.getMinBuildHeight() + 4);
        int roofPeak = gy + 3 + (half + 1) / 2; // 坡顶最高层

        // 2) 整平（±(half+2)，含门前）：上空清理（树冠等）→ 地基填实（gy-1 向下至多 8 层）
        //    → 地面至墙顶四层清空（挖平高台/植被，屋内与门前同高）；村庄核心/钟一律保留
        for (int dx = -scanR; dx <= scanR; dx++) {
            for (int dz = -scanR; dz <= scanR; dz++) {
                int bx = px + dx;
                int bz = pz + dz;
                for (int y = roofPeak + 1; y <= roofPeak + 8; y++) {
                    BlockPos q = new BlockPos(bx, y, bz);
                    BlockState qs = level.getBlockState(q);
                    if (qs.isAir() || isCoreOrBell(qs)) continue;
                    level.setBlock(q, Blocks.AIR.defaultBlockState(), 3);
                }
                for (int d = 1; d <= 8; d++) {
                    BlockPos below = new BlockPos(bx, gy - d, bz);
                    BlockState bs = level.getBlockState(below);
                    if (bs.isAir() || !bs.getFluidState().isEmpty()) {
                        level.setBlock(below, Blocks.DIRT.defaultBlockState(), 3);
                    } else {
                        break; // 已实心（地表）停止
                    }
                }
                for (int dy = 0; dy <= 3; dy++) {
                    BlockPos q = new BlockPos(bx, gy + dy, bz);
                    BlockState qs = level.getBlockState(q);
                    if (qs.isAir() || isCoreOrBell(qs)) continue;
                    level.setBlock(q, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        // 3) 四角橡木原木柱（gy..gy+3，与墙同高）
        for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
                BlockPos corner = new BlockPos(px + sx * half, gy, pz + sz * half);
                for (int dy = 0; dy <= 3; dy++) {
                    level.setBlock(corner.above(dy), Blocks.OAK_LOG.defaultBlockState(), 3);
                }
            }
        }

        // 4) 墙（圆石 3 格高）+ 橡木门（南墙中间 1×2，朝向门外）
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                boolean wallX = Math.abs(dx) == half;
                boolean wallZ = Math.abs(dz) == half;
                boolean corner = wallX && wallZ;
                boolean door = dx == 0 && dz == half;
                if (!wallX && !wallZ) continue; // 内部
                for (int dy = 0; dy < 3; dy++) {
                    if (corner) continue; // 角柱已放
                    if (door) {
                        if (dy == 0) {
                            BlockState doorState = Blocks.OAK_DOOR.defaultBlockState()
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
                            level.setBlock(new BlockPos(px, gy, pz + half), doorState, 3);
                            level.setBlock(new BlockPos(px, gy + 1, pz + half),
                                    doorState.setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.UPPER), 3);
                        }
                        continue; // 门洞 1×2
                    }
                    level.setBlock(new BlockPos(px + dx, gy + dy, pz + dz), Blocks.COBBLESTONE.defaultBlockState(), 3);
                }
            }
        }

        // 5) 屋内木板地板（含门内第一格）
        for (int dx = -half + 1; dx <= half - 1; dx++) {
            for (int dz = -half + 1; dz <= half - 1; dz++) {
                level.setBlock(new BlockPos(px + dx, gy, pz + dz), Blocks.OAK_PLANKS.defaultBlockState(), 3);
            }
        }

        // 6) 屋顶：南北向斜坡（脊在 z 中间，顶层上半台阶坡面）
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                int h = (half - Math.abs(dz) + 1) / 2;
                for (int dy = 0; dy <= h; dy++) {
                    level.setBlock(new BlockPos(px + dx, gy + 3 + dy, pz + dz), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                }
                level.setBlock(new BlockPos(px + dx, gy + 3 + h, pz + dz),
                        Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP), 3);
            }
        }

        // 7) 屋内照明：北墙内侧地面火把
        level.setBlock(new BlockPos(px, gy, pz - (half - 1)), Blocks.TORCH.defaultBlockState(), 3);

        // 8) 内部 2 个随机职业方块（不重复）
        List<Block> pool = jobBlockPool();
        if (pool.size() >= 2) {
            Block b1 = pool.get(level.random.nextInt(pool.size()));
            Block b2;
            do {
                b2 = pool.get(level.random.nextInt(pool.size()));
            } while (b2 == b1 && pool.size() > 1);
            level.setBlock(new BlockPos(px + 1, gy, pz + 1), b1.defaultBlockState(), 3);
            level.setBlock(new BlockPos(px + 1, gy, pz - 1), b2.defaultBlockState(), 3);
        }

        // 9) 记录（实际地基高度，供牧羊人放床）并结算
        state.addHouse(new BlockPos(px, gy, pz));
        VillagerJobMemory mem = CapabilityRegistry.jobOf(villager).orElse(null);
        if (mem != null) {
            mem.consumeUse(ACTION_ID, Config.MASON_HOUSES_PER_DAY.get());
        }
        ActionEffects.playTradeComplete(level, plotCenter.getCenter());
        ActionEffects.grantVillagerXp(villager, Config.MASON_XP_PER_ACTION.get());
        com.mojang.logging.LogUtils.getLogger().info("[VW] 石匠建房完成: 地块={} 地基={} 屋顶={} 大小={}",
                plotCenter, gy, roofPeak, size);
    }

    /** 真实地表高度：从 fromY 向下找第一个非空气且非树叶/原木（树）的方块上方；水面也算地表 */
    private static int groundY(ServerLevel level, int x, int z, int fromY) {
        int y = Math.min(fromY, level.getMaxBuildHeight());
        for (; y > level.getMinBuildHeight() + 1; y--) {
            BlockState s = level.getBlockState(new BlockPos(x, y, z));
            if (s.isAir()) continue;
            if (!s.getFluidState().isEmpty()) return y + 1;
            Block b = s.getBlock();
            if (b instanceof LeavesBlock || b instanceof RotatedPillarBlock) {
                continue; // 树冠/树干：继续向下
            }
            return y + 1;
        }
        return Math.max(fromY, level.getMinBuildHeight() + 2);
    }

    /** 村庄核心或钟（整平/清空时保留，不可被替换成空气） */
    private static boolean isCoreOrBell(BlockState state) {
        Block b = state.getBlock();
        return b == com.villagewill.block.VillageCoreBlock.INSTANCE || b == Blocks.BELL;
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
