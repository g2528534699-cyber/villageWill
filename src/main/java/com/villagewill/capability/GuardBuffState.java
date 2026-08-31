package com.villagewill.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * 警卫强化状态（挂在 guardvillagers 的 Guard 上，随实体 NBT 持久化）
 * - 各盔甲槽的升级层级（槽 0-3 对应 头/胸/腿/脚）
 * - 武器升级层级
 * - 被面包顶替的副手盾牌（吃完面包后归还）
 * - 每日进食计数
 */
public class GuardBuffState {
    /** 盔甲槽升级层级（与 guardInventory 槽 0-3 对应） */
    public final int[] armorTiers = new int[4];
    /** 武器升级层级（主手） */
    public int weaponTier = 0;
    /** 被面包顶替的盾牌，吃完后归还 */
    public ItemStack displacedShield = ItemStack.EMPTY;
    /** 今日已吃食物计数（供统计/调试） */
    public int eatenToday = 0;
    /** 上次进食的游戏日 */
    public long lastEatDay = -1;

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("ArmorTiers", armorTiers);
        tag.putInt("WeaponTier", weaponTier);
        if (!displacedShield.isEmpty()) {
            tag.put("DisplacedShield", displacedShield.save(new CompoundTag()));
        }
        tag.putInt("EatenToday", eatenToday);
        tag.putLong("LastEatDay", lastEatDay);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("ArmorTiers", Tag.TAG_INT_ARRAY)) {
            int[] tiers = tag.getIntArray("ArmorTiers");
            System.arraycopy(tiers, 0, armorTiers, 0, Math.min(tiers.length, armorTiers.length));
        }
        weaponTier = tag.getInt("WeaponTier");
        displacedShield = tag.contains("DisplacedShield", Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound("DisplacedShield"))
                : ItemStack.EMPTY;
        eatenToday = tag.getInt("EatenToday");
        lastEatDay = tag.getLong("LastEatDay");
    }
}
