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
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
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
