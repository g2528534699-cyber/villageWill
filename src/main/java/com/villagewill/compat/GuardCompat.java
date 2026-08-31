package com.villagewill.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.phys.AABB;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

import javax.annotation.Nullable;
import java.util.List;

/**
 * guardvillagers 适配层：所有对前置 mod 的 API 访问集中于此，
 * 便于版本变更时单点修改。
 */
public final class GuardCompat {
    private GuardCompat() {
    }

    public static boolean isGuard(Entity entity) {
        return entity instanceof Guard;
    }

    public static boolean isGuardClass(Object o) {
        return o instanceof Guard;
    }

    /** 每村庄自然生成的警卫数（前置 mod 配置）——牧师复活上限数据源 */
    public static int villageGuardLimit() {
        try {
            return GuardConfig.COMMON.guardSpawnInVillage.get();
        } catch (Throwable t) {
            return 6;
        }
    }

    public static EntityType<Guard> guardType() {
        return GuardEntityType.GUARD.get();
    }

    /** 生成一个全新警卫（默认装备，无额外物品） */
    @Nullable
    public static Guard spawnFreshGuard(ServerLevel level, BlockPos pos) {
        Guard guard = GuardEntityType.GUARD.get().create(level);
        if (guard == null) return null;
        guard.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        guard.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
                MobSpawnType.MOB_SUMMONED, (SpawnGroupData) null, null);
        guard.setPersistenceRequired();
        level.addFreshEntity(guard);
        return guard;
    }

    /** 以村庄中心为中心的警卫列表 */
    public static List<Guard> guardsNear(ServerLevel level, BlockPos center, double radius) {
        return level.getEntitiesOfClass(Guard.class,
                new AABB(center).inflate(radius), GuardCompat::isValid);
    }

    private static boolean isValid(Guard guard) {
        return guard != null && guard.isAlive();
    }

    /** 守卫的声望容器 */
    public static GossipContainer gossips(Guard guard) {
        return guard.getGossips();
    }
}
