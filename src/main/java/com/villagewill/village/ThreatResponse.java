package com.villagewill.village;

import com.villagewill.Config;
import com.villagewill.behavior.ActionEffects;
import com.villagewill.entity.StoneGolem;
import com.villagewill.registry.ModEntities;
import com.villagewill.util.VillageContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.villagewill.VillageWill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 村庄核心：威胁召唤（新功能）
 * - 村民受敌对生物攻击时，按威胁权重计算威胁值
 * - 消耗绿宝石（消耗=ceil(威胁值×costPerThreat)，最小1），绿宝石来自村庄每日收入
 * - 在威胁处召唤 1-3 只傀儡：威胁值<阈值→石傀儡（等级=村庄最高石匠等级），≥阈值→铁傀儡
 * - 召唤物带临时标记（VillageWillTempUntil），到期（默认600tick）自动消失
 */
@Mod.EventBusSubscriber(modid = VillageWill.MODID)
public final class ThreatResponse {
    /** 临时傀儡到期标记（持久化 NBT） */
    public static final String TEMP_UNTIL_TAG = "VillageWillTempUntil";

    private static final Map<String, Integer> WEIGHT_CACHE = new HashMap<>();

    private ThreatResponse() {
    }

    @SubscribeEvent
    public static void onVillagerHurt(LivingHurtEvent event) {
        if (!Config.THREAT_ENABLED.get()) return;
        LivingEntity hurt = event.getEntity();
        if (!(hurt instanceof Villager) || hurt.level().isClientSide) return;
        ServerLevel level = (ServerLevel) hurt.level();
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Mob mob) || !(mob instanceof Enemy)) return; // 仅敌对生物
        int weight = threatWeight(mob);
        if (weight <= 0) return;

        // 村庄绿宝石结算与扣费（仅核心激活后生效）
        BlockPos center = VillageContext.villageCenter(level, hurt.blockPosition());
        VillageState state = VillageState.get(level, center);
        if (!state.isCoreActive()) return; // 核心未激活（村庄未达条件）不触发
        state.settleEmeraldIncome(level);

        // 冷却：同一村庄短时间内不重复触发（防止连续攻击刷傀儡）
        long now = level.getGameTime();
        if (now - state.lastThreatTime() < Config.THREAT_COOLDOWN_TICKS.get()) return;
        state.setLastThreatTime(now);

        int cost = Math.max(1, (int) Math.ceil(weight * Config.THREAT_COST_PER_THREAT.get()));
        if (!state.spendEmeralds(cost)) return;

        // 召唤数量与类型
        int count = Math.min(Config.THREAT_MAX_GOLEMS.get(),
                Math.max(1, (int) Math.ceil(weight / 4.0)));
        boolean iron = weight >= Config.THREAT_IRON_THRESHOLD.get();
        long until = level.getGameTime() + Config.THREAT_DURATION_TICKS.get();
        Vec3 at = attacker.position();

        for (int i = 0; i < count; i++) {
            spawnGolem(level, center, at, iron, until);
        }
        ActionEffects.playTradeComplete(level, at);
        com.mojang.logging.LogUtils.getLogger().info("[VW] 威胁召唤: 攻击者={} 威胁值={} 消耗={}颗 召唤{}只{}",
                mob.getType().getDescriptionId(), weight, cost, count, iron ? "铁傀儡" : "石傀儡");
    }

    private static void spawnGolem(ServerLevel level, BlockPos villageCenter, Vec3 at, boolean iron, long until) {
        Mob golem;
        if (iron) {
            IronGolem ig = EntityType.IRON_GOLEM.create(level);
            if (ig == null) return;
            golem = ig;
        } else {
            StoneGolem sg = ModEntities.STONE_GOLEM.get().create(level);
            if (sg == null) return;
            sg.setTier(highestMasonLevel(level, villageCenter));
            sg.setHealth(sg.getMaxHealth());
            golem = sg;
        }
        double dx = level.random.nextDouble() * 4.0 - 2.0;
        double dz = level.random.nextDouble() * 4.0 - 2.0;
        golem.moveTo(at.x + dx, at.y, at.z + dz, level.random.nextFloat() * 360.0F, 0.0F);
        golem.getPersistentData().putLong(TEMP_UNTIL_TAG, until);
        golem.setPersistenceRequired();
        level.addFreshEntity(golem);
    }

    /** 村庄内最高等级石匠（石傀儡等级），无则 1 */
    private static int highestMasonLevel(ServerLevel level, BlockPos center) {
        int best = 1;
        List<Villager> villagers = VillageContext.villagersInVillage(level, center);
        for (Villager v : villagers) {
            if (v.getVillagerData().getProfession() == net.minecraft.world.entity.npc.VillagerProfession.MASON) {
                best = Math.max(best, v.getVillagerData().getLevel());
            }
        }
        return best;
    }

    /** 临时傀儡到期检查（每实体 tick 调用） */
    public static void tickTemporary(Entity entity) {
        if (entity.level().isClientSide) return;
        long until = entity.getPersistentData().getLong(TEMP_UNTIL_TAG);
        if (until > 0 && entity.level().getGameTime() >= until) {
            entity.discard();
        }
    }

    private static int threatWeight(Mob mob) {
        String key = EntityType.getKey(mob.getType()).toString();
        Integer cached = WEIGHT_CACHE.get(key);
        if (cached != null) return cached;
        int weight = 1;
        for (String entry : Config.THREAT_WEIGHTS.get()) {
            String[] parts = entry.split("=");
            if (parts.length == 2 && parts[0].trim().equals(key)) {
                try {
                    weight = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ignored) {
                }
                break;
            }
        }
        WEIGHT_CACHE.put(key, weight);
        return weight;
    }
}
