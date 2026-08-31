package com.villagewill.behavior.actions;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 警卫物品栏（guardInventory）通用操作
 * 槽位：0=头 1=胸 2=腿 3=脚 4=副手 5=主手
 */
public final class ActionUtil {
    private ActionUtil() {
    }

    public static int findItemSlot(SimpleContainer inv, Item item) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(item)) return i;
        }
        return -1;
    }

    public static boolean hasItem(Guard guard, Item item) {
        return findItemSlot(guard.guardInventory, item) >= 0;
    }

    /** 警卫是否持有食物（面包/牛排） */
    public static boolean hasAnyFood(Guard guard) {
        SimpleContainer inv = guard.guardInventory;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && (s.is(Items.BREAD) || s.is(Items.COOKED_BEEF))) return true;
        }
        return false;
    }

    /** 第一个空槽（[from, toExclusive)），无则 -1 */
    public static int firstEmptySlot(SimpleContainer inv, int from, int toExclusive) {
        for (int i = from; i < Math.min(toExclusive, inv.getContainerSize()); i++) {
            if (inv.getItem(i).isEmpty()) return i;
        }
        return -1;
    }

    public static boolean canAdd(Guard guard, int from, int toExclusive) {
        return firstEmptySlot(guard.guardInventory, from, toExclusive) >= 0;
    }
}
