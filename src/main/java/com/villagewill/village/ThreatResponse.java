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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.villagewill.VillageWill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 村庄核心：威胁召唤（§4.3）
 * - 村民受敌对生物攻击时触发；仅核心激活且未损坏时生效
 * - 综合威胁评估：威胁值 = 类型基础权重 × 属性因子（生命/护甲/攻击，兼容其他 mod 生物）
 * - 绿宝石预算权衡：可用余额 = 余额×spendRatio（其余留科技）；按威胁值组队（铁/石傀儡战力）
 *   小威胁只召少量石傀儡，大威胁按预算上限满编铁+石傀儡；总成本不超过可用余额
 * - 召唤物带临时标记（VillageWillTempUntil），到期自动消失
 * - 同村冷却防刷
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

        double threat = evaluateThreat(mob);
        if (threat <= 0) return;

        // 村庄绿宝石结算与扣费（仅核心激活且未损坏时生效）
        BlockPos center = VillageContext.villageCenter(level, hurt.blockPosition());
        if (center == null) return; // 无核心/钟的野区不响应
        VillageState state = VillageState.get(level, center);
        if (!state.isCoreActive() || state.isCoreDamaged()) return;
        state.settleEmeraldIncome(level);

        // 冷却：同一村庄短时间内不重复触发（防止连续攻击刷傀儡）
        long now = level.getGameTime();
        if (now - state.lastThreatTime() < Config.THREAT_COOLDOWN_TICKS.get()) return;
        state.setLastThreatTime(now);

        // 预算：召唤可用余额（其余留给科技升级）
        long balance = state.emeraldBalance();
        int spendable = (int) Math.floor(balance * Config.THREAT_SPEND_RATIO.get());
        if (spendable < 1) return;

        // 按威胁值组队：铁傀儡战力高/成本高，石傀儡补足
        int ironCost = Config.THREAT_IRON_COST.get();
        int stoneCost = Config.THREAT_STONE_COST.get();
        int ironPower = Config.THREAT_IRON_POWER.get();
        int stonePower = Config.THREAT_STONE_POWER.get();
        int neededPower = (int) Math.ceil(threat);

        int iron = 0;
        int stone = 0;
        if (threat >= Config.THREAT_IRON_THRESHOLD.get() && spendable >= ironCost) {
            int maxIronByBudget = spendable / ironCost;
            int maxIronByPower = (int) Math.ceil(neededPower / (double) ironPower);
            iron = Math.min(Math.min(maxIronByBudget, maxIronByPower), 2); // 铁傀儡最多2只
        }
        int remainingPower = Math.max(0, neededPower - iron * ironPower);
        int remainingBudget = spendable - iron * ironCost;
        if (remainingBudget >= stoneCost) {
            stone = Math.min(remainingBudget / stoneCost,
                    (int) Math.ceil(remainingPower / (double) stonePower));
        }
        // 总数上限裁剪
        int maxGolems = Config.THREAT_MAX_GOLEMS.get();
        if (iron + stone > maxGolems) {
            int excess = iron + stone - maxGolems;
            int cutStone = Math.min(stone, excess);
            stone -= cutStone;
            excess -= cutStone;
            if (excess > 0) iron -= excess;
        }
        int cost = iron * ironCost + stone * stoneCost;
        if (cost <= 0) return;
        if (!state.spendEmeralds(cost)) return;

        long until = now + Config.THREAT_DURATION_TICKS.get();
        Vec3 at = attacker.position();
        for (int i = 0; i < iron; i++) {
            spawnGolem(level, center, at, true, until);
        }
        for (int i = 0; i < stone; i++) {
            spawnGolem(level, center, at, false, until);
        }
        ActionEffects.playTradeComplete(level, at);
        com.mojang.logging.LogUtils.getLogger().info(
                "[VW] 威胁召唤: {} 威胁值={} 消耗={}颗(铁{}石{}) 余额={}",
                mob.getType().getDescriptionId(), String.format("%.1f", threat),
                cost, iron, stone, state.emeraldBalance());
    }

    /**
     * 综合威胁评估（§4.3）：基础权重 × 属性因子
     * 属性因子 = 0.4 + 0.3×(生命/20) + 0.3×(攻击/3) + 0.2×((护甲+韧性)/15)，钳制 0.4~3.0
     */
    /** 威胁综合评估：类型权重 × 属性因子（攻击/血量/速度），兼容其他 mod 生物（Part 3 队长派遣复用） */
    public static double evaluateThreat(Mob mob) {
        int base = threatWeight(mob);
        if (!Config.THREAT_ATTR_EVALUATION.get()) return base;
        double health = attrValue(mob, Attributes.MAX_HEALTH, 20.0);
        double attack = attrValue(mob, Attributes.ATTACK_DAMAGE, 3.0);
        double armor = (attrValue(mob, Attributes.ARMOR, 10.0)
                + attrValue(mob, Attributes.ARMOR_TOUGHNESS, 5.0)) / 15.0;
        double factor = 0.4 + 0.3 * (health / 20.0) + 0.3 * (attack / 3.0) + 0.2 * armor;
        factor = Math.max(0.4, Math.min(3.0, factor));
        return base * factor;
    }

    private static double attrValue(Mob mob, net.minecraft.world.entity.ai.attributes.Attribute attr, double def) {
        AttributeInstance inst = mob.getAttribute(attr);
        return inst != null ? inst.getValue() : def;
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
            if (v.getVillagerData().getProfession() == VillagerProfession.MASON) {
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
