package com.villagewill.behavior;

import com.villagewill.Config;
import com.villagewill.behavior.actions.ClericAction;
import com.villagewill.behavior.actions.ProfessionAction;
import com.villagewill.behavior.actions.ProfessionActions;
import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.VillagerJobMemory;
import com.villagewill.compat.GuardCompat;
import com.villagewill.util.VillageContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.phys.Vec3;
import tallestegg.guardvillagers.entities.Guard;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * 功能9：村民走向警卫执行强化（交互感）
 * - 注入村民 goalSelector（优先级2，EntityJoinLevelEvent 时）
 * - 目标：村庄内可强化的警卫（牧师特殊：日出时走向自己家/村中心复活警卫）
 * - 到达后执行动作 → 交易完成特效（绿星粒子+村民音效，服务端广播）→ 给村民经验
 * - 与村民 brain 竞争对策：开始前清除 WALK_TARGET 记忆，执行中若 brain 重新设置则放弃本次
 */
public class EnhanceGoal extends Goal {
    private final Villager villager;
    private Guard targetGuard;
    private BlockPos clericTarget;
    private boolean executed;
    private int cooldown;

    public EnhanceGoal(Villager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(villager.level() instanceof ServerLevel level)) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (villager.isBaby() || !Config.ENHANCE_INTERACTION_ENABLED.get()) return false;

        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (villager.tickCount % 100 == 0) {
            com.mojang.logging.LogUtils.getLogger().info("[VW] EnhanceGoal tick: prof={} tick={}",
                    profession, villager.tickCount);
        }
        if (profession == VillagerProfession.NONE) return false;
        Optional<VillagerJobMemory> mem = CapabilityRegistry.jobOf(villager);
        if (mem.isEmpty()) {
            if (villager.tickCount % 100 == 0) {
                com.mojang.logging.LogUtils.getLogger().info("[VW] {} 无能力", profession);
            }
            return false;
        }
        VillagerJobMemory memory = mem.get();

        // 牧师：日出时走向村庄中心复活警卫
        if (profession == VillagerProfession.CLERIC) {
            if (!Config.CLERIC_ENABLED.get()) return false;
            int clericDaily = Config.levelValue(Config.CLERIC_RESURRECT_PER_LEVEL.get(),
                    villager.getVillagerData().getLevel());
            if (memory.usesFor(ClericAction.ACTION_ID, clericDaily) <= 0) return false;
            if (!ClericAction.isDawn(level)) return false;
            BlockPos center = VillageContext.homeOf(villager).orElse(villager.blockPosition());
            if (!ClericAction.canResurrect(level, center)) return false;
            this.clericTarget = center;
            this.targetGuard = null;
            this.executed = false;
            return true;
        }

        ProfessionAction action = ProfessionActions.forProfession(profession);
        if (action == null) return false;
        int daily = action.dailyUses(villager.getVillagerData().getLevel());
        if (memory.usesFor(action.id(), daily) <= 0) {
            if (villager.tickCount % 100 == 0) {
                com.mojang.logging.LogUtils.getLogger().info("[VW] {} 次数不足: {}<=0 (daily={})", profession, action.id(), daily);
            }
            return false;
        }

        Guard guard = findTarget(level, action);
        if (guard == null) {
            if (villager.tickCount % 200 == 0) {
                com.mojang.logging.LogUtils.getLogger().info("[VW] {} 未找到可强化目标（无警卫或无需求）", profession);
            }
            return false;
        }
        this.targetGuard = guard;
        this.clericTarget = null;
        this.executed = false;
        com.mojang.logging.LogUtils.getLogger().info("[VW] {} 选定目标警卫 {} 距离={}", profession,
                guard.blockPosition(), villager.distanceTo(guard));
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (executed) return false;
        if (targetGuard != null && !targetGuard.isAlive()) return false;
        return targetGuard != null || clericTarget != null;
    }

    @Override
    public void start() {
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        rePath();
    }

    @Override
    public void tick() {
        if (executed) return;
        if (!(villager.level() instanceof ServerLevel level)) return;
        // 压制村民 brain 的行走目标，避免被日程打断（goal 激活期间以强化动作为准）
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        if (clericTarget != null) {
            if (villager.blockPosition().distSqr(clericTarget) <= 6.25D) {
                executeCleric(level);
            } else {
                rePath();
            }
            return;
        }
        if (targetGuard == null || !targetGuard.isAlive()) return;
        // 3.5 格执行（近距离寻路器在 ~2.8 格时不再生成路径，留出余量）
        if (villager.distanceToSqr(targetGuard) <= 12.25D) {
            executeAction(level, targetGuard);
        } else {
            rePath();
        }
    }

    @Override
    public void stop() {
        this.executed = false;
        this.targetGuard = null;
        this.clericTarget = null;
        this.cooldown = 200 + villager.getRandom().nextInt(400); // 10~30 秒后再评估
    }

    // ---------------- 内部 ----------------

    /** 在村庄范围内寻找最近的可强化警卫 */
    @Nullable
    private Guard findTarget(ServerLevel level, ProfessionAction action) {
        double maxDist = Config.ENHANCE_MAX_DISTANCE.get();
        int villagerLevel = villager.getVillagerData().getLevel();
        List<Guard> guards = VillageContext.guardsInVillage(level, villager.blockPosition());
        Guard best = null;
        double bestDist = maxDist * maxDist;
        for (Guard guard : guards) {
            if (!action.canApplyTo(guard, villagerLevel)) continue;
            double dist = villager.distanceToSqr(guard);
            if (dist < bestDist) {
                bestDist = dist;
                best = guard;
            }
        }
        return best;
    }

    private void rePath() {
        if (clericTarget != null) {
            villager.getNavigation().moveTo(clericTarget.getX() + 0.5D, clericTarget.getY(), clericTarget.getZ() + 0.5D, 0.6D);
        } else if (targetGuard != null) {
            villager.getNavigation().moveTo(targetGuard, 0.6D);
        }
    }

    private void executeAction(ServerLevel level, Guard guard) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        ProfessionAction action = ProfessionActions.forProfession(profession);
        if (action == null) return;
        VillagerJobMemory memory = CapabilityRegistry.jobOf(villager).orElse(null);
        if (memory == null) return;
        if (action.execute(level, villager, guard, memory)) {
            ActionEffects.playTradeComplete(level, guard.position());
            ActionEffects.grantVillagerXp(villager, action.xpPerAction());
            this.executed = true;
        } else {
            // 条件变化（如物品栏已满），放弃本次
            this.executed = true;
        }
    }

    private void executeCleric(ServerLevel level) {
        VillagerJobMemory memory = CapabilityRegistry.jobOf(villager).orElse(null);
        if (memory == null) return;
        if (ClericAction.execute(level, villager, clericTarget, memory)) {
            ActionEffects.playTradeComplete(level, Vec3.atCenterOf(clericTarget));
            ActionEffects.grantVillagerXp(villager, Config.CLERIC_XP_PER_ACTION.get());
            this.executed = true;
        } else {
            this.executed = true;
        }
    }
}
