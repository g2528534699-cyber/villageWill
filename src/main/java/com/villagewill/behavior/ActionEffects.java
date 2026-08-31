package com.villagewill.behavior;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

/**
 * 动作特效与经验（服务端广播，所有玩家可见；不涉及客户端独立逻辑）
 */
public final class ActionEffects {
    private ActionEffects() {
    }

    /** 交易完成特效：绿星粒子 + 村民肯定音效（服务端广播） */
    public static void playTradeComplete(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.x, pos.y + 1.0D, pos.z,
                14, 0.5D, 0.6D, 0.5D, 0.06D);
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    /** 给村民增加职业经验（原版 XP 系统，等级随经验自然提升） */
    public static void grantVillagerXp(Villager villager, int amount) {
        if (amount <= 0 || villager.level().isClientSide) return;
        villager.setVillagerXp(villager.getVillagerXp() + amount);
    }
}
