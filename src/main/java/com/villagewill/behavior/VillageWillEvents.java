package com.villagewill.behavior;

import com.villagewill.Config;
import com.villagewill.VillageWill;
import com.villagewill.building.MasonBuildGoal;
import com.villagewill.building.MasonGolemGoal;
import com.villagewill.building.ShepherdBedGoal;
import com.villagewill.building.ShepherdDogs;
import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.compat.GuardCompat;
import com.villagewill.village.CoreConversion;
import com.villagewill.village.ThreatResponse;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 事件总线：
 * - EntityJoinLevelEvent：给村民注入行为 AI（强化/建房/放床/石傀儡）
 * - LivingTickEvent：警卫食物逻辑 + 村民每日次数重置 + 牧羊犬轮询 + 临时傀儡到期
 * - LivingHurtEvent：村庄威胁召唤（ThreatResponse）
 */
@Mod.EventBusSubscriber(modid = VillageWill.MODID)
public final class VillageWillEvents {

    private VillageWillEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Villager villager && event.getLevel() instanceof ServerLevel) {
            LogUtils.getLogger().info("[VW] 注入村民行为AI: prof={} pos={}",
                    villager.getVillagerData().getProfession(), villager.blockPosition());
            villager.goalSelector.addGoal(2, new EnhanceGoal(villager));
            villager.goalSelector.addGoal(2, new MasonBuildGoal(villager));
            villager.goalSelector.addGoal(2, new MasonGolemGoal(villager));
            villager.goalSelector.addGoal(2, new ShepherdBedGoal(villager));
        } else if (event.getEntity() instanceof Guard guard && event.getLevel() instanceof ServerLevel level) {
            // 新警卫加入：若属于某个村庄核心，应用警卫科技（血量/攻击）
            net.minecraft.core.BlockPos center =
                    com.villagewill.util.VillageContext.villageCenter(level, guard.blockPosition());
            if (center != null) {
                com.villagewill.village.VillageState state =
                        com.villagewill.village.VillageState.get(level, center);
                com.villagewill.village.CaptainManager.applyGuardBuff(guard, state.guardTechLevel());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        // 队长阵亡：记录装备待复活
        if (event.getEntity() instanceof com.villagewill.entity.GuardCaptain captain
                && event.getEntity().level() instanceof ServerLevel level) {
            com.villagewill.village.CaptainManager.onCaptainDeath(level, captain);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        // 警卫队长爆炸开路：爆炸只破坏方块，不伤害任何实体（含队长自己）
        net.minecraft.world.damagesource.DamageSource src = event.getSource();
        if (src.is(net.minecraft.world.damagesource.DamageTypes.EXPLOSION)
                && src.getEntity() instanceof com.villagewill.entity.GuardCaptain) {
            event.setCanceled(true);
            return;
        }
        // 威胁召唤（核心激活后按威胁值耗绿宝石召唤限时傀儡）
        ThreatResponse.onVillagerHurt(event);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        // 临时傀儡到期移除
        ThreatResponse.tickTemporary(entity);

        if (entity instanceof Guard guard) {
            if (Config.GUARD_FOOD_LOGIC_ENABLED.get()) {
                GuardFoodLogic.tick(guard);
            }
            // 护卫跟随队长（每 20 tick 刷新导航目标，无战斗目标时跟随）
            net.minecraft.nbt.CompoundTag data = guard.getPersistentData();
            if (data.getBoolean("VillageWillEscort") && guard.tickCount % 20 == 0) {
                try {
                    net.minecraft.world.entity.Entity captain = ((net.minecraft.server.level.ServerLevel) guard.level())
                            .getEntity(java.util.UUID.fromString(data.getString("CaptainUUID")));
                    if (captain instanceof com.villagewill.entity.GuardCaptain c && c.isAlive()
                            && guard.getTarget() == null && guard.distanceToSqr(c) > 9.0D) {
                        guard.getNavigation().moveTo(c, 1.0D);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        } else if (entity instanceof Villager villager) {
            long day = villager.level().getDayTime() / 24000L;
            CapabilityRegistry.jobOf(villager).ifPresent(memory -> {
                memory.advanceDay(day);
                if (villager.tickCount % 40 == 0) {
                    ShepherdDogs.tick((ServerLevel) villager.level(), villager, memory);
                }
                // 村庄核心转换检查（村民≥阈值 → 钟变核心）
                CoreConversion.tick((ServerLevel) villager.level(), villager);
            });
        }
    }
}
