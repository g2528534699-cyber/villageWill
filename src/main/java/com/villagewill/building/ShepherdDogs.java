package com.villagewill.building;

import com.villagewill.Config;
import com.villagewill.capability.VillagerJobMemory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.UUID;

/**
 * 牧羊人驯服狗逻辑：
 * - 认领牧羊人职业 → 获得 2 只驯服狼（生命/攻击随职业等级缩放）
 * - 失业/转职 → 狗消失
 */
public final class ShepherdDogs {
    private ShepherdDogs() {
    }

    /** 周期轮询：认领牧羊人→生成狗；失业/转职→回收狗；狗死亡→补位 */
    public static void tick(ServerLevel level, Villager villager, VillagerJobMemory mem) {
        if (villager.tickCount % 100 == 0) {
            com.mojang.logging.LogUtils.getLogger().info("[VW] ShepherdDogs.tick: prof={} tick={} dogs={}",
                    villager.getVillagerData().getProfession(), villager.tickCount, mem.dogs.size());
        }
        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == VillagerProfession.SHEPHERD) {
            // 清理已死狗并补位
            mem.dogs.removeIf(uuid -> {
                Entity e = level.getEntity(uuid);
                return e == null || !e.isAlive();
            });
            int want = Config.SHEPHERD_DOGS.get();
            while (mem.dogs.size() < want) {
                if (!spawnDog(level, villager, mem)) break;
            }
        } else if (!mem.dogs.isEmpty()) {
            for (UUID uuid : mem.dogs) {
                Entity entity = level.getEntity(uuid);
                if (entity instanceof Wolf wolf && wolf.isAlive()) {
                    wolf.discard();
                }
            }
            mem.dogs.clear();
        }
    }

    private static boolean spawnDog(ServerLevel level, Villager villager, VillagerJobMemory mem) {
        int lvl = villager.getVillagerData().getLevel();
        double scale = 1.0 + Config.SHEPHERD_DOG_SCALE_PER_LEVEL.get() * (lvl - 1);
        Wolf wolf = EntityType.WOLF.create(level);
        if (wolf == null) return false;
        double dx = level.random.nextDouble() * 2.0 - 1.0;
        double dz = level.random.nextDouble() * 2.0 - 1.0;
        wolf.moveTo(villager.getX() + dx, villager.getY(), villager.getZ() + dz,
                level.random.nextFloat() * 360.0F, 0.0F);
        wolf.setTame(true);
        wolf.setOwnerUUID(villager.getUUID());
        wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Config.SHEPHERD_DOG_HP_BASE.get() * scale);
        wolf.setHealth((float) (Config.SHEPHERD_DOG_HP_BASE.get() * scale));
        wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Config.SHEPHERD_DOG_ATTACK_BASE.get() * scale);
        wolf.setPersistenceRequired();
        level.addFreshEntity(wolf);
        mem.dogs.add(wolf.getUUID());
        return true;
    }
}
