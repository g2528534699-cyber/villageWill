package com.villagewill.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * 警卫强化状态与专用储物槽（挂在 guardvillagers 的 Guard 上，随实体 NBT 持久化）
 *
 * 储物槽设计（§4.2，不占 guardvillagers 装备槽 0-3）：
 * - foodSlots[0..1]：食物槽（面包/牛排，同类可合并）
 * - arrowSlot：药水箭槽（弹药库，战斗时上膛到副手作弹匣）
 * - displacedOffhand：被顶替的副手物品（盾牌等），使用完毕后归还
 *
 * 其他：
 * - 各盔甲槽的升级层级（槽 0-3 对应 头/胸/腿/脚）
 * - 武器升级层级
 */
public class GuardBuffState {
    /** 盔甲槽升级层级（与 guardInventory 槽 0-3 对应） */
    public final int[] armorTiers = new int[4];
    /** 武器升级层级（主手） */
    public int weaponTier = 0;
    /** 被食物/药水箭顶替的副手物品，用完后归还 */
    public ItemStack displacedOffhand = ItemStack.EMPTY;
    /** 专用食物槽（面包/牛排） */
    public final ItemStack[] foodSlots = {ItemStack.EMPTY, ItemStack.EMPTY};
    /** 专用药水箭槽（弹药库） */
    public ItemStack arrowSlot = ItemStack.EMPTY;

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("ArmorTiers", armorTiers);
        tag.putInt("WeaponTier", weaponTier);
        if (!displacedOffhand.isEmpty()) {
            tag.put("DisplacedOffhand", displacedOffhand.save(new CompoundTag()));
        }
        ListTag foods = new ListTag();
        for (ItemStack stack : foodSlots) {
            if (!stack.isEmpty()) {
                foods.add(stack.save(new CompoundTag()));
            }
        }
        tag.put("FoodSlots", foods);
        if (!arrowSlot.isEmpty()) {
            tag.put("ArrowSlot", arrowSlot.save(new CompoundTag()));
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("ArmorTiers", Tag.TAG_INT_ARRAY)) {
            int[] tiers = tag.getIntArray("ArmorTiers");
            System.arraycopy(tiers, 0, armorTiers, 0, Math.min(tiers.length, armorTiers.length));
        }
        weaponTier = tag.getInt("WeaponTier");
        // 旧版字段兼容
        if (tag.contains("DisplacedShield", Tag.TAG_COMPOUND)) {
            displacedOffhand = ItemStack.of(tag.getCompound("DisplacedShield"));
        } else {
            displacedOffhand = tag.contains("DisplacedOffhand", Tag.TAG_COMPOUND)
                    ? ItemStack.of(tag.getCompound("DisplacedOffhand"))
                    : ItemStack.EMPTY;
        }
        foodSlots[0] = ItemStack.EMPTY;
        foodSlots[1] = ItemStack.EMPTY;
        if (tag.contains("FoodSlots", Tag.TAG_LIST)) {
            ListTag foods = tag.getList("FoodSlots", Tag.TAG_COMPOUND);
            for (int i = 0; i < foods.size() && i < foodSlots.length; i++) {
                foodSlots[i] = ItemStack.of(foods.getCompound(i));
            }
        }
        arrowSlot = tag.contains("ArrowSlot", Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound("ArrowSlot"))
                : ItemStack.EMPTY;
    }
}
