package com.villagewill.behavior;

import com.villagewill.Config;
import com.villagewill.VillageWill;
import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.VillagerJobMemory;
import com.villagewill.compat.GuardCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 事件总线：
 * - EntityJoinLevelEvent：给村民注入强化目标 AI（EnhanceGoal）
 * - ServerTickEvent：警卫食物逻辑 + 村民每日次数重置
 */
@Mod.EventBusSubscriber(modid = VillageWill.MODID)
public final class VillageWillEvents {
    private static int tickCounter;

    private VillageWillEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Villager villager && event.getLevel() instanceof ServerLevel) {
            villager.goalSelector.addGoal(2, new EnhanceGoal(villager));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (++tickCounter % Config.GUARD_FOOD_CHECK_TICKS.get() != 0) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            long day = level.getDayTime() / 24000L;
            for (Entity entity : level.getEntities().getAll()) {
                if (entity instanceof Guard guard) {
                    if (Config.GUARD_FOOD_LOGIC_ENABLED.get()) {
                        GuardFoodLogic.tick(guard);
                    }
                } else if (entity instanceof Villager villager) {
                    // 换天重置每日动作次数（顺带职业快照）
                    CapabilityRegistry.jobOf(villager)
                            .ifPresent(memory -> memory.advanceDay(day));
                }
            }
        }
    }
}
