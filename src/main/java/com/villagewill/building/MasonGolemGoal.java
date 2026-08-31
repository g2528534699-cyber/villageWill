package com.villagewill.building;

import com.villagewill.Config;
import com.villagewill.behavior.ActionEffects;
import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.VillagerJobMemory;
import com.villagewill.entity.StoneGolem;
import com.villagewill.registry.ModEntities;
import com.villagewill.util.VillageContext;
import com.villagewill.village.VillageState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 石匠生成石傀儡 AI
 * - 每个石匠最多 2 只（VillagerJobMemory.golems 记录，死亡后补位）
 * - 石傀儡属性随石匠职业等级（tier=石匠等级）
 * - 走向村庄中心生成，附交易完成特效
 */
public class MasonGolemGoal extends Goal {
    public static final String ACTION_ID = "mason_golem";

    private final Villager villager;
    private BlockPos spawnPos;
    private boolean done;
    private int cooldown;

    public MasonGolemGoal(Villager villager) {
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
        if (mem == null || mem.usesFor(ACTION_ID, 1) <= 0) return false;

        // 清理已死石傀儡
        mem.golems.removeIf(uuid -> {
            Entity e = level.getEntity(uuid);
            return e == null || !e.isAlive();
        });
        if (mem.golems.size() >= Config.GOLEM_MAX_PER_MASON.get()) return false;

        BlockPos center = VillageContext.villageCenter(level, villager.blockPosition());
        if (center == null) return false; // 无村庄（核心/钟）不干活
        VillageState.get(level, center); // 确保村庄状态存在
        this.spawnPos = new BlockPos(center.getX(), VillageContext.surfaceY(level, center), center.getZ());
        this.done = false;
        com.mojang.logging.LogUtils.getLogger().info("[VW] 石匠生成石傀儡: 生成点={} golems={}", spawnPos, mem.golems.size());
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !done && spawnPos != null && villager.isAlive();
    }

    @Override
    public void start() {
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getNavigation().moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.6D);
    }

    @Override
    public void tick() {
        if (done || !(villager.level() instanceof ServerLevel level)) return;
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        if (villager.blockPosition().distSqr(spawnPos) <= 25.0D) {
            spawnGolem(level);
            done = true;
        } else if (villager.getNavigation().isDone()) {
            villager.getNavigation().moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.6D);
        }
    }

    @Override
    public void stop() {
        this.spawnPos = null;
        this.done = false;
        this.cooldown = 600 + villager.getRandom().nextInt(600);
    }

    private void spawnGolem(ServerLevel level) {
        StoneGolem golem = ModEntities.STONE_GOLEM.get().create(level);
        if (golem == null) return;
        int tier = villager.getVillagerData().getLevel();
        golem.setTier(tier);
        golem.setHealth(golem.getMaxHealth()); // 按 tier 属性满血生成
        double dx = level.random.nextDouble() * 4.0 - 2.0;
        double dz = level.random.nextDouble() * 4.0 - 2.0;
        golem.moveTo(spawnPos.getX() + 0.5D + dx, spawnPos.getY(), spawnPos.getZ() + 0.5D + dz,
                level.random.nextFloat() * 360.0F, 0.0F);
        golem.setPersistenceRequired();
        level.addFreshEntity(golem);

        VillagerJobMemory mem = CapabilityRegistry.jobOf(villager).orElse(null);
        if (mem != null) {
            mem.golems.add(golem.getUUID());
            mem.consumeUse(ACTION_ID, 1);
        }
        ActionEffects.playTradeComplete(level, Vec3.atCenterOf(spawnPos));
        ActionEffects.grantVillagerXp(villager, Config.MASON_XP_PER_ACTION.get());
    }
}
