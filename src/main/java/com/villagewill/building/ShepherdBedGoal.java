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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 牧羊人放床 AI：在石匠建造的小屋内放置床（每屋上限 config，默认2张）
 */
public class ShepherdBedGoal extends Goal {
    public static final String ACTION_ID = "shepherd_bed";

    private final Villager villager;
    private BlockPos villageKey;
    private BlockPos targetHouse;
    private boolean done;
    private int cooldown;

    public ShepherdBedGoal(Villager villager) {
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
        if (villager.isBaby() || !Config.SHEPHERD_ENABLED.get()) return false;
        if (villager.getVillagerData().getProfession() != VillagerProfession.SHEPHERD) return false;
        VillagerJobMemory mem = CapabilityRegistry.jobOf(villager).orElse(null);
        if (mem == null || mem.usesFor(ACTION_ID, Config.SHEPHERD_BEDS_PER_DAY.get()) <= 0) return false;

        this.villageKey = VillageContext.villageCenter(level, villager.blockPosition());
        if (villageKey == null) return false; // 无村庄（核心/钟）不干活
        VillageState state = VillageState.get(level, villageKey);
        BlockPos house = state.findHouseNeedingBed(villager.blockPosition());
        if (house == null) return false;
        this.targetHouse = house;
        this.done = false;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !done && targetHouse != null && villager.isAlive();
    }

    @Override
    public void start() {
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        pathToHouse();
    }

    @Override
    public void tick() {
        if (done || !(villager.level() instanceof ServerLevel level)) return;
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        if (villager.blockPosition().distSqr(targetHouse) <= 16.0D) {
            placeBed(level, VillageState.get(level, villageKey));
            done = true;
        } else {
            pathToHouse();
        }
    }

    @Override
    public void stop() {
        this.targetHouse = null;
        this.done = false;
        this.cooldown = 300 + villager.getRandom().nextInt(400);
    }

    private void placeBed(ServerLevel level, VillageState state) {
        int beds = state.bedsInHouse(targetHouse);
        // 床放在站立层（地板层 = 房子地基 gy-1，与地板同层，不嵌入/不悬浮）
        int y = targetHouse.getY() - 1;
        // 两张床并排朝南（头在北侧），避开职业方块 (px+1, pz±1) 与火把 (px, pz-(half-1))
        BlockPos foot = beds == 0
                ? new BlockPos(targetHouse.getX() - 1, y, targetHouse.getZ())
                : new BlockPos(targetHouse.getX(), y, targetHouse.getZ());
        // setBlock 直放不会自动补床头（setPlacedBy 仅玩家放置触发），手动放置头格
        BlockState bed = Blocks.RED_BED.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        level.setBlock(foot, bed, 3);
        level.setBlock(foot.relative(Direction.NORTH),
                bed.setValue(BlockStateProperties.BED_PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD), 3);
        state.addBed(targetHouse);
        VillagerJobMemory mem = CapabilityRegistry.jobOf(villager).orElse(null);
        if (mem != null) {
            mem.consumeUse(ACTION_ID, Config.SHEPHERD_BEDS_PER_DAY.get());
        }
        ActionEffects.playTradeComplete(level, Vec3.atCenterOf(foot));
        ActionEffects.grantVillagerXp(villager, Config.SHEPHERD_XP_PER_ACTION.get());
    }

    private void pathToHouse() {
        if (targetHouse != null) {
            villager.getNavigation().moveTo(targetHouse.getX() + 0.5D, targetHouse.getY() - 1, targetHouse.getZ() + 0.5D, 0.5D);
        }
    }
}
