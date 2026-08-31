package com.villagewill.entity;

import com.villagewill.Config;
import com.villagewill.village.CoreRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 警卫队长（阶段三 Part 3）：
 * - 村庄核心激活后生成（+4 名护卫），80 血/下界合金套/力量IV无限弓
 * - 独立战斗 AI（tick 状态机，不依赖原版目标选择器）：
 *   空闲 → 村庄中心附近巡逻；核心派遣 → 追击目标
 *   目标在地下 → 无伤爆炸（破坏方块、实体 0 伤害）开路
 *   目标在高处/飞行 → 脚下垫方块 + 弓射（力量IV无限）
 *   近身 → 近战攻击（攻击力随科技等级提升）
 * - 免疫全部负面效果；装备无限耐久；所在区块强加载（3×3）
 * - 属性随队长科技等级成长（每级 +血量/攻击）
 */
public class GuardCaptain extends PathfinderMob {
    /** 任务目标（核心派遣，NBT 持久化） */
    @Nullable
    private UUID taskTargetUUID;
    /** 科技等级（血量/攻击加成） */
    private int captainTier = 0;
    /** 村庄中心（巡逻锚点，NBT 持久化） */
    @Nullable
    private BlockPos villageAnchor;

    // 计时器
    private int patrolTimer;
    private int attackTimer;
    private int explodeTimer;
    private int bridgeTimer;
    private int shootTimer;
    private int chunkTimer;
    private int anchorTimer;
    private int stuckTicks;
    @Nullable
    private net.minecraft.world.phys.Vec3 lastPos;

    public GuardCaptain(EntityType<? extends GuardCaptain> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel level)) return;

        // 区块强加载（每 100 tick 刷新，死亡后由村庄核心替代保持）
        if (++chunkTimer >= 100) {
            chunkTimer = 0;
            forceLoadChunk(level);
        }
        // 巡逻锚点（每 100 tick 刷新：核心注册表 → 已加载扫描）
        if (++anchorTimer >= 100) {
            anchorTimer = 0;
            if (villageAnchor == null) {
                villageAnchor = CoreRegistry.nearest(level.dimension(), blockPosition(), 128);
            }
        }

        LivingEntity target = resolveTarget();
        if (target != null && target.isAlive() && !target.isSpectator()) {
            combatTick(level, target);
        } else {
            patrolTick(level);
        }

        // 卡住检测：导航进行中但 5 秒位移 < 0.01 → 头顶小型爆炸脱困
        if (getNavigation().isInProgress()) {
            double moved = lastPos == null ? Double.MAX_VALUE : position().distanceToSqr(lastPos);
            if (moved < 0.01D) {
                if (++stuckTicks > 100) {
                    unstuck(level);
                    stuckTicks = -200; // 脱困后 15 秒内不重复
                }
            } else {
                stuckTicks = 0;
            }
        }
        lastPos = position();
    }

    /** 卡住脱困：头顶 2 格无伤爆炸（只炸方块） */
    private void unstuck(ServerLevel level) {
        Explosion e = new Explosion(level, this,
                getX(), getY() + 2.0D, getZ(), 2.0F, false,
                Explosion.BlockInteraction.DESTROY);
        e.explode();
        e.finalizeExplosion(false);
        com.mojang.logging.LogUtils.getLogger().warn("[VW] 警卫队长脱困爆破: 位置={}", blockPosition());
    }

    // ---------------- 战斗状态机 ----------------

    private void combatTick(ServerLevel level, LivingEntity target) {
        double dy = target.getY() - getY();
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);

        // 1) 目标被地形阻挡（地下/墙后/洞穴）→ 无伤爆炸开路（只炸方块，不伤实体）
        if (isBlockedFrom(target)) {
            explodeTick(level, target);
            return;
        }
        // 2) 目标在高处但可垫脚 → 脚下垫方块（自动攀高）
        if (dy > 3.0D && horiz < 10.0D) {
            bridgeTick();
        }
        // 3) 飞行目标或过高（垫脚够不着）→ 弓射
        if (isAirborneTarget(target) || dy > 7.0D) {
            shootTick(level, target);
        }
        // 4) 近战
        if (distanceToSqr(target) <= 3.5D) {
            meleeTick(target);
        } else {
            getNavigation().moveTo(target, 1.0D);
        }
    }

    /** 队长眼睛到目标眼睛的射线被方块阻挡（且不是目标/自身脚下）→ 视为地形阻挡 */
    private boolean isBlockedFrom(LivingEntity target) {
        net.minecraft.world.phys.BlockHitResult hit = level().clip(new net.minecraft.world.level.ClipContext(
                new net.minecraft.world.phys.Vec3(getX(), getEyeY(), getZ()),
                new net.minecraft.world.phys.Vec3(target.getX(), target.getEyeY(), target.getZ()),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this));
        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.core.BlockPos hp = hit.getBlockPos();
            return !hp.equals(target.blockPosition()) && !hp.equals(blockPosition());
        }
        return false;
    }

    /** 无伤爆炸：破坏方块开路；实体伤害由 VillageWillEvents 拦截（队长爆炸取消） */
    private void explodeTick(ServerLevel level, LivingEntity target) {
        if (++explodeTimer < Config.CAPTAIN_EXPLOSION_INTERVAL.get()) return;
        explodeTimer = 0;
        BlockPos tp = target.blockPosition();
        Explosion explosion = new Explosion(level, this,
                tp.getX() + 0.5D, tp.getY() + 0.5D, tp.getZ() + 0.5D,
                Config.CAPTAIN_EXPLOSION_POWER.get().floatValue(), false,
                Explosion.BlockInteraction.DESTROY);
        explosion.explode();
        explosion.finalizeExplosion(false);
        playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 1.0F);
    }

    /** 脚下垫圆石（每 8 tick 一块，配合寻路器攀高） */
    private void bridgeTick() {
        if (++bridgeTimer < Config.CAPTAIN_BRIDGE_INTERVAL.get()) return;
        bridgeTimer = 0;
        BlockPos below = blockPosition().below();
        if (level().getBlockState(below).isAir()) {
            level().setBlock(below, Blocks.COBBLESTONE.defaultBlockState(), 3);
        }
    }

    /** 弓射（力量IV无限弓；箭伤害随科技等级提升） */
    private void shootTick(ServerLevel level, LivingEntity target) {
        if (++shootTimer < Config.CAPTAIN_SHOOT_INTERVAL.get()) return;
        shootTimer = 0;
        ItemStack bow = getMainHandItem();
        if (bow.isEmpty() || !bow.is(Items.BOW)) return;
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, bow, 1.6F);
        arrow.setBaseDamage(6.0D + captainTier * 1.5D);
        double d0 = target.getEyeY() - 1.1D;
        double d1 = target.getX() - getX();
        double d2 = d0 - arrow.getY();
        double d3 = target.getZ() - getZ();
        double dist = Math.sqrt(d1 * d1 + d3 * d3);
        arrow.shoot(d1, d2 + dist * 0.2D, d3, 1.6F, 2.0F);
        playSound(SoundEvents.ARROW_SHOOT, 1.0F, 0.8F / (random.nextFloat() * 0.4F + 0.8F));
        level.addFreshEntity(arrow);
    }

    /** 近战（16 tick 节流） */
    private void meleeTick(LivingEntity target) {
        if (++attackTimer < 16) return;
        attackTimer = 0;
        doHurtTarget(target);
        playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
    }

    /** 飞行/悬空目标判定 */
    private boolean isAirborneTarget(LivingEntity target) {
        if (target instanceof Phantom) return true;
        // 脚下无方块且离地 6 格以上视为飞行/悬空
        BlockPos below = target.blockPosition().below();
        return target.level().getBlockState(below).isAir()
                && target.getY() - target.level().getMinBuildHeight() > 6.0D
                && !target.onGround();
    }

    // ---------------- 巡逻 ----------------

    private void patrolTick(ServerLevel level) {
        if (++patrolTimer >= 100) {
            patrolTimer = 0;
            if (villageAnchor != null && getNavigation().isDone()) {
                // 村庄中心 20~40 格随机巡逻点
                float a = random.nextFloat() * (float) Math.PI * 2.0F;
                float r = 20.0F + random.nextFloat() * 20.0F;
                int px = villageAnchor.getX() + (int) (Math.cos(a) * r);
                int pz = villageAnchor.getZ() + (int) (Math.sin(a) * r);
                int py = villageAnchor.getY();
                getNavigation().moveTo(px + 0.5D, py, pz + 0.5D, 0.6D);
            }
        }
    }

    // ---------------- 区块强加载（队长活动区块 3×3） ----------------

    private void forceLoadChunk(ServerLevel level) {
        int cx = blockPosition().getX() >> 4;
        int cz = blockPosition().getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setChunkForced(cx + dx, cz + dz, true);
            }
        }
    }

    // ---------------- 任务派遣 API ----------------

    /** 核心派遣任务目标（null 清除） */
    public void setTaskTarget(@Nullable LivingEntity target) {
        this.taskTargetUUID = target != null ? target.getUUID() : null;
        if (target == null) {
            getNavigation().stop();
        }
    }

    @Nullable
    public LivingEntity resolveTarget() {
        if (taskTargetUUID == null) return null;
        if (!(level() instanceof ServerLevel level)) return null;
        Entity e = level.getEntity(taskTargetUUID);
        return (e instanceof LivingEntity le && le.isAlive() && !(le instanceof Player && ((Player) le).isSpectator()))
                ? le : null;
    }

    public boolean hasTask() {
        return resolveTarget() != null;
    }

    // ---------------- 科技等级（血量/攻击成长） ----------------

    public int getCaptainTier() {
        return captainTier;
    }

    public void setCaptainTier(int tier) {
        this.captainTier = Math.max(0, tier);
        applyTier();
    }

    /** 应用科技成长：每级 +HP_PER_LEVEL 血 +ATK_PER_LEVEL 攻（transient modifier，幂等） */
    public void applyTier() {
        net.minecraft.world.entity.ai.attributes.AttributeInstance hp =
                getAttribute(Attributes.MAX_HEALTH);
        net.minecraft.world.entity.ai.attributes.AttributeInstance atk =
                getAttribute(Attributes.ATTACK_DAMAGE);
        if (hp == null || atk == null) return;
        hp.removeModifier(TIER_HP_MOD);
        atk.removeModifier(TIER_ATK_MOD);
        if (captainTier > 0) {
            hp.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    TIER_HP_MOD, "vw_captain_tier_hp",
                    Config.TECH_CAPTAIN_HP_PER_LEVEL.get() * (double) captainTier,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
            atk.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    TIER_ATK_MOD, "vw_captain_tier_atk",
                    Config.TECH_CAPTAIN_ATK_PER_LEVEL.get() * (double) captainTier,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        }
    }

    private static final UUID TIER_HP_MOD = UUID.fromString("7f1d3a00-0a01-4c2e-9b3a-5f6a1d2c3e4f");
    private static final UUID TIER_ATK_MOD = UUID.fromString("8f2d4b11-1b02-4d3f-8c4a-6f7b2e3d4f5a");

    // ---------------- 队长装备（下界合金 + 力量IV无限弓，无限耐久） ----------------

    public void equipCaptain() {
        setItemSlot(EquipmentSlot.HEAD, unbreakable(new ItemStack(Items.NETHERITE_HELMET)));
        setItemSlot(EquipmentSlot.CHEST, unbreakable(new ItemStack(Items.NETHERITE_CHESTPLATE)));
        setItemSlot(EquipmentSlot.LEGS, unbreakable(new ItemStack(Items.NETHERITE_LEGGINGS)));
        setItemSlot(EquipmentSlot.FEET, unbreakable(new ItemStack(Items.NETHERITE_BOOTS)));
        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(net.minecraft.world.item.enchantment.Enchantments.POWER_ARROWS, 4);
        bow.enchant(net.minecraft.world.item.enchantment.Enchantments.INFINITY_ARROWS, 1);
        setItemSlot(EquipmentSlot.MAINHAND, unbreakable(bow));
    }

    private static ItemStack unbreakable(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        return stack;
    }

    // ---------------- 免疫负面效果 / 装备耐久 ----------------

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        return false;
    }

    @Override
    protected void hurtArmor(DamageSource source, float amount) {
        // 装备无限耐久：不扣
    }

    @Override
    public void hurtCurrentlyUsedShield(float amount) {
        // 不扣盾耐久
    }

    // ---------------- 持久化 ----------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (taskTargetUUID != null) tag.putString("TaskTarget", taskTargetUUID.toString());
        tag.putInt("CaptainTier", captainTier);
        if (villageAnchor != null) {
            tag.putInt("AnchorX", villageAnchor.getX());
            tag.putInt("AnchorY", villageAnchor.getY());
            tag.putInt("AnchorZ", villageAnchor.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TaskTarget")) {
            try {
                taskTargetUUID = UUID.fromString(tag.getString("TaskTarget"));
            } catch (IllegalArgumentException ignored) {
                taskTargetUUID = null;
            }
        }
        setCaptainTier(tag.getInt("CaptainTier"));
        if (tag.contains("AnchorX")) {
            villageAnchor = new BlockPos(tag.getInt("AnchorX"), tag.getInt("AnchorY"), tag.getInt("AnchorZ"));
        }
    }

    public void setVillageAnchor(BlockPos anchor) {
        this.villageAnchor = anchor.immutable();
    }

    @Nullable
    public BlockPos villageAnchor() {
        return villageAnchor;
    }
}
