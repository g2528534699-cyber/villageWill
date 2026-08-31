package com.villagewill.village;

import com.villagewill.Config;
import com.villagewill.compat.GuardCompat;
import com.villagewill.entity.GuardCaptain;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import tallestegg.guardvillagers.entities.Guard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 警卫队长管理（阶段三 Part 3）：
 * - 首次生成：村庄核心激活后生成队长 + N 名护卫（免费）
 * - 复活：队长死亡后，绿宝石余额足够时在核心旁自动复活（恢复存档装备）
 * - 威胁派遣：核心为中心扫描 300 格（仅已加载区块）敌对生物，按威胁值派遣队长
 * - 科技消费：队长血量/攻击（captainTechLevel）、护卫装备（escortTechLevel）、
 *   村庄全部警卫属性（guardTechLevel）——由核心 BE ticker 定期驱动
 */
public final class CaptainManager {
    private CaptainManager() {
    }

    /** 由 VillageCoreBlockEntity.tick 节流调用 */
    public static void tick(ServerLevel level, BlockPos corePos, VillageState state) {
        if (!state.isCoreActive() || state.isCoreDamaged()) return;

        GuardCaptain captain = captainInWorld(level, state);
        if (captain == null) {
            ensureCaptain(level, corePos, state);
            captain = captainInWorld(level, state);
        }
        if (captain == null) return;

        // 科技消费（幂等，每次刷新）
        applyCaptainTech(captain, state);
        applyEscortTech(level, state);
        applyGuardTech(level, state);

        // 威胁扫描派遣
        dispatchThreat(level, corePos, state, captain);
    }

    // ---------------- 生成 / 复活 ----------------

    private static void ensureCaptain(ServerLevel level, BlockPos corePos, VillageState state) {
        boolean free = state.captainUUID() == null || !state.isCaptainDead();
        if (!free) {
            int cost = Config.CAPTAIN_RESURRECT_COST.get();
            if (state.emeraldBalance() < cost) return; // 没钱不复活
            if (!state.spendEmeralds(cost)) return;
        }
        String what = state.captainUUID() == null ? "生成" : "复活";
        spawnCaptain(level, corePos, state);
        state.setCaptainDead(false);
        LogUtils.getLogger().info("[VW] 警卫队长{}: 位置={}", what, corePos);
    }

    /** 生成队长 + 护卫（首次免费，复活时恢复装备；存活护卫保留并重绑队长） */
    private static void spawnCaptain(ServerLevel level, BlockPos corePos, VillageState state) {
        GuardCaptain captain = com.villagewill.registry.ModEntities.GUARD_CAPTAIN.get().create(level);
        if (captain == null) return;
        captain.moveTo(corePos.getX() + 0.5D, corePos.getY() + 1.0D, corePos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        captain.setVillageAnchor(corePos);
        captain.setCaptainTier(state.captainTechLevel());
        captain.equipCaptain();
        // 复活：恢复存档装备（覆盖默认装备）
        CompoundTag saved = state.captainEquipment();
        if (saved != null && !saved.isEmpty()) {
            captain.readAdditionalSaveData(saved);
        }
        level.addFreshEntity(captain);
        state.setCaptainUUID(captain.getUUID());
        captain.setHealth(captain.getMaxHealth()); // 复活/重生后回满血

        // 护卫：复活时保留存活护卫（重绑队长），只补足缺失数量
        int escorts = Config.CAPTAIN_ESCORTS.get();
        List<Guard> existing = state.escortsSpawned() ? escortsInWorld(level, state) : new ArrayList<>();
        for (Guard g : existing) {
            g.getPersistentData().putString("CaptainUUID", captain.getUUID().toString());
        }
        int need = Math.max(0, escorts - existing.size());
        for (int i = 0; i < need; i++) {
            Guard guard = GuardCompat.spawnFreshGuard(level, corePos.offset(2 + i * 2, 1, 0));
            if (guard == null) continue;
            guard.getPersistentData().putBoolean("VillageWillEscort", true);
            guard.getPersistentData().putString("CaptainUUID", captain.getUUID().toString());
            equipEscortBase(guard);
            state.addEscortUUID(guard.getUUID());
        }
        state.setEscortsSpawned(true);
    }

    /** 护卫初始铁套 */
    private static void equipEscortBase(Guard guard) {
        guard.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        guard.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        guard.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        guard.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        guard.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }

    @Nullable
    private static GuardCaptain captainInWorld(ServerLevel level, VillageState state) {
        String uuid = state.captainUUID();
        if (uuid == null) return null;
        try {
            Entity e = level.getEntity(UUID.fromString(uuid));
            return e instanceof GuardCaptain c && c.isAlive() ? c : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // ---------------- 威胁扫描与派遣 ----------------

    /** 核心为中心 300 格（仅已加载区块会返回实体）内威胁最高的敌对生物 → 派遣队长 */
    private static void dispatchThreat(ServerLevel level, BlockPos corePos, VillageState state, GuardCaptain captain) {
        int range = Config.CAPTAIN_SCAN_RANGE.get();
        List<Mob> candidates = level.getEntitiesOfClass(Mob.class, new AABB(corePos).inflate(range),
                m -> m != null && m.isAlive() && m instanceof Enemy && !GuardCompat.isGuard(m));
        Mob best = null;
        double bestThreat = 0.0D;
        for (Mob mob : candidates) {
            double t = ThreatResponse.evaluateThreat(mob);
            if (t > bestThreat) {
                bestThreat = t;
                best = mob;
            }
        }
        if (best == null) {
            // 无威胁：若队长正在追击已消失目标则清空
            if (captain.hasTask()) {
                LivingEntity cur = captain.resolveTarget();
                if (cur == null || !cur.isAlive()) captain.setTaskTarget(null);
            }
            return;
        }
        LivingEntity current = captain.resolveTarget();
        if (current == null || (current instanceof Mob cm && ThreatResponse.evaluateThreat(cm) < bestThreat)) {
            captain.setTaskTarget(best);
            LogUtils.getLogger().info("[VW] 队长派遣: 目标={} 威胁={} 距离={}",
                    best.getType().getDescriptionId(), String.format("%.1f", bestThreat),
                    String.format("%.0f", captain.distanceTo(best)));
        }
    }

    // ---------------- 科技消费 ----------------

    /** 队长血量/攻击（每级 +hp +atk，modifier 幂等） */
    private static void applyCaptainTech(GuardCaptain captain, VillageState state) {
        if (captain.getCaptainTier() != state.captainTechLevel()) {
            captain.setCaptainTier(state.captainTechLevel());
        } else {
            captain.applyTier(); // 确保 modifier 存在（重载后）
        }
    }

    /** 护卫装备：escortTechLevel ≥3 换下界合金+附魔；≥1 钻石；0=铁 */
    private static void applyEscortTech(ServerLevel level, VillageState state) {
        int lv = state.escortTechLevel();
        boolean changed = state.appliedEscortTech() != lv;
        if (!changed) return;
        state.setAppliedEscortTech(lv);
        List<Guard> escorts = escortsInWorld(level, state);
        for (Guard g : escorts) {
            equipEscortByTech(g, lv);
        }
    }

    private static void equipEscortByTech(Guard guard, int lv) {
        if (lv <= 0) {
            equipEscortBase(guard);
            return;
        }
        boolean netherite = lv >= 3;
        ItemStack helm = new ItemStack(netherite ? Items.NETHERITE_HELMET : Items.DIAMOND_HELMET);
        ItemStack chest = new ItemStack(netherite ? Items.NETHERITE_CHESTPLATE : Items.DIAMOND_CHESTPLATE);
        ItemStack legs = new ItemStack(netherite ? Items.NETHERITE_LEGGINGS : Items.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(netherite ? Items.NETHERITE_BOOTS : Items.DIAMOND_BOOTS);
        if (lv >= 2) {
            helm.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION, 1 + lv);
            chest.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION, 1 + lv);
            legs.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION, 1 + lv);
            boots.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION, 1 + lv);
        }
        guard.setItemSlot(EquipmentSlot.HEAD, helm);
        guard.setItemSlot(EquipmentSlot.CHEST, chest);
        guard.setItemSlot(EquipmentSlot.LEGS, legs);
        guard.setItemSlot(EquipmentSlot.FEET, boots);
        ItemStack sword = new ItemStack(netherite ? Items.NETHERITE_SWORD : Items.DIAMOND_SWORD);
        if (lv >= 2) sword.enchant(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS, 1 + lv);
        guard.setItemSlot(EquipmentSlot.MAINHAND, sword);
    }

    /** 村庄全部警卫属性（guardTechLevel：每级 +hp 血 +atk 攻，transient modifier 幂等） */
    private static void applyGuardTech(ServerLevel level, VillageState state) {
        int lv = state.guardTechLevel();
        if (lv == state.appliedGuardTech()) return;
        state.setAppliedGuardTech(lv);
        for (Guard g : GuardCompat.guardsNear(level, state.key(), 256.0D)) {
            applyGuardBuff(g, lv);
        }
    }

    /** 新警卫加入村庄时应用（EntityJoinLevelEvent 调用） */
    public static void applyGuardBuff(Guard guard, int lv) {
        if (lv <= 0) return;
        AttributeInstance hp = guard.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance atk = guard.getAttribute(Attributes.ATTACK_DAMAGE);
        if (hp == null || atk == null) return;
        hp.removeModifier(GUARD_HP_MOD);
        atk.removeModifier(GUARD_ATK_MOD);
        if (lv > 0) {
            hp.addTransientModifier(new AttributeModifier(GUARD_HP_MOD, "vw_guard_tech_hp",
                    Config.TECH_GUARD_HP_PER_LEVEL.get() * (double) lv, AttributeModifier.Operation.ADDITION));
            atk.addTransientModifier(new AttributeModifier(GUARD_ATK_MOD, "vw_guard_tech_atk",
                    Config.TECH_GUARD_ATK_PER_LEVEL.get() * (double) lv, AttributeModifier.Operation.ADDITION));
        }
        if (guard.getHealth() > guard.getMaxHealth()) {
            guard.setHealth(guard.getMaxHealth());
        }
    }

    private static final UUID GUARD_HP_MOD = UUID.fromString("1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d");
    private static final UUID GUARD_ATK_MOD = UUID.fromString("2b3c4d5e-6f7a-4b8c-9d0e-1f2a3b4c5d6e");

    // ---------------- 护卫查询 ----------------

    /** 世界中的存活护卫（含新生成标记） */
    public static List<Guard> escortsInWorld(ServerLevel level, VillageState state) {
        List<Guard> result = new ArrayList<>();
        for (String s : state.escortUUIDs()) {
            try {
                Entity e = level.getEntity(UUID.fromString(s));
                if (e instanceof Guard g && g.isAlive()) result.add(g);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    /** 队长死亡：标记阵亡（复活收费）并保存装备（供复活恢复） */
    public static void onCaptainDeath(ServerLevel level, GuardCaptain captain) {
        BlockPos anchor = captain.villageAnchor();
        if (anchor == null) return;
        VillageState state = VillageState.get(level, anchor);
        if (state == null || !state.isCoreActive()) return;
        CompoundTag tag = new CompoundTag();
        captain.addAdditionalSaveData(tag);
        state.setCaptainEquipment(tag);
        state.setCaptainDead(true);
        LogUtils.getLogger().info("[VW] 警卫队长阵亡: 位置={} 已记录装备待复活", anchor);
    }
}
