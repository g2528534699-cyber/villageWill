package com.villagewill.entity;

import com.villagewill.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.level.Level;

/**
 * 石傀儡：形态/攻击模式与雪傀儡一致（远程投掷），属性随石匠职业等级提升
 * 1级攻2 15血10甲1韧 → 5级攻7 30血20甲3韧（全部可配置公式见 Config）
 * 命中附带缓慢（等级=石匠职业等级）
 */
public class StoneGolem extends SnowGolem {
    /** 石匠职业等级 1-5 */
    private int tier = 1;

    public StoneGolem(EntityType<? extends StoneGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = Math.max(1, Math.min(5, tier));
        applyTierAttributes();
    }

    /** 缓慢效果等级（命中附带） */
    public int getSlowLevel() {
        return tier;
    }

    private void applyTierAttributes() {
        if (getAttribute(Attributes.ATTACK_DAMAGE) == null) return;
        int t = tier - 1;
        // 用户表：1级攻2 15血10甲1韧；2级3/18/12/1.5；3级4/21/14/2；4级5/24/16/2.5；5级7/30/20/3
        double[] dmg = {2, 3, 4, 5, 7};
        double[] hp = {15, 18, 21, 24, 30};
        double[] armor = {10, 12, 14, 16, 20};
        double[] toughness = {1.0, 1.5, 2.0, 2.5, 3.0};
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(dmg[t]);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp[t]);
        getAttribute(Attributes.ARMOR).setBaseValue(armor[t]);
        getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(toughness[t]);
        if (getHealth() > getMaxHealth() || getHealth() <= 0) {
            setHealth((float) hp[t]);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ThrownStone stone = new ThrownStone(this.level(), this);
        double d0 = target.getEyeY() - 1.1D;
        double d1 = target.getX() - this.getX();
        double d2 = d0 - stone.getY();
        double d3 = target.getZ() - this.getZ();
        float f = Mth.sqrt((float) (d1 * d1 + d3 * d3)) * 0.2F;
        stone.shoot(d1, d2 + f, d3, 1.6F, 1.0F);
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(stone);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("StoneTier", tier);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTier(tag.getInt("StoneTier"));
    }
}
