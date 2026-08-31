package com.villagewill.village;

import com.villagewill.Config;
import com.villagewill.compat.GuardCompat;
import com.villagewill.entity.StoneGolem;
import com.villagewill.util.VillageContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * 村庄核心信标光环：
 * - 初始：50×50（半径25）内 村民/警卫/傀儡/高声望玩家 获得 生命恢复I
 * - 科技解锁：效果等级0=再生I；1-5依次解锁 速度/急迫/抗性/跳跃/力量（I）；6+=全部效果II（再生保持I）
 * - 范围升级：每级 +radiusPerLevel
 * - 仅核心激活且未损坏时生效
 */
public final class BeaconAura {
    private BeaconAura() {
    }

    public static void tick(ServerLevel level, BlockPos corePos, VillageState state) {
        if (!state.isCoreActive() || state.isCoreDamaged()) return;
        List<MobEffect> effects = activeEffects(state.beaconEffectLevel());
        if (effects.isEmpty()) return;

        int range = Config.BEACON_RADIUS_BASE.get()
                + Config.BEACON_RADIUS_PER_LEVEL.get() * state.beaconRangeLevel();
        AABB box = new AABB(corePos).inflate(range);

        for (Villager v : level.getEntitiesOfClass(Villager.class, box)) {
            apply(v, effects, state.beaconEffectLevel());
        }
        for (tallestegg.guardvillagers.entities.Guard g : GuardCompat.guardsNear(level, corePos, range)) {
            apply(g, effects, state.beaconEffectLevel());
        }
        for (IronGolem g : level.getEntitiesOfClass(IronGolem.class, box)) {
            apply(g, effects, state.beaconEffectLevel());
        }
        for (StoneGolem g : level.getEntitiesOfClass(StoneGolem.class, box)) {
            apply(g, effects, state.beaconEffectLevel());
        }
        for (Player p : level.getEntitiesOfClass(Player.class, box)) {
            if (playerReputation(level, corePos, p) >= Config.BEACON_REPUTATION_THRESHOLD.get()) {
                apply(p, effects, state.beaconEffectLevel());
            }
        }
    }

    /** 当前效果列表（按解锁等级） */
    private static List<MobEffect> activeEffects(int level) {
        List<MobEffect> list = new ArrayList<>();
        list.add(MobEffects.REGENERATION);
        if (level >= 1) list.add(MobEffects.MOVEMENT_SPEED);
        if (level >= 2) list.add(MobEffects.DIG_SPEED);
        if (level >= 3) list.add(MobEffects.DAMAGE_RESISTANCE);
        if (level >= 4) list.add(MobEffects.JUMP);
        if (level >= 5) list.add(MobEffects.DAMAGE_BOOST);
        return list;
    }

    private static void apply(LivingEntity entity, List<MobEffect> effects, int level) {
        for (MobEffect effect : effects) {
            int amp = (level >= 6 && effect != MobEffects.REGENERATION) ? 1 : 0;
            entity.addEffect(new MobEffectInstance(effect, 220, amp, true, true));
        }
    }

    private static int playerReputation(ServerLevel level, BlockPos corePos, Player player) {
        int rep = 0;
        for (Villager v : VillageContext.villagersInVillage(level, corePos)) {
            rep += v.getPlayerReputation(player);
        }
        return rep;
    }
}
