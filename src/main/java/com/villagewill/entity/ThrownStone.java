package com.villagewill.entity;

import com.villagewill.registry.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 石头球：石傀儡投掷物
 * - 命中伤害 = 发射者攻击属性（随石匠等级）
 * - 命中附带缓慢（等级=石傀儡 tier）
 */
public class ThrownStone extends ThrowableItemProjectile {

    public ThrownStone(EntityType<? extends ThrownStone> type, Level level) {
        super(type, level);
    }

    public ThrownStone(Level level, LivingEntity shooter) {
        super(ModEntities.THROWN_STONE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return com.villagewill.VillageWill.STONE_BALL.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        float damage = 3.0F;
        if (owner instanceof LivingEntity living) {
            AttributeInstance atk = living.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atk != null) {
                damage = (float) atk.getValue();
            }
            if (owner instanceof StoneGolem golem && target instanceof LivingEntity le) {
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, golem.getSlowLevel() - 1));
            }
        }
        target.hurt(this.damageSources().thrown(this, owner), damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }
}
