package com.villagewill.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

/**
 * 附魔台规则随机附魔封装（图书管理员使用）
 * 等价于附魔台 N 级经验：EnchantmentHelper.enchantItem(random, stack, level, allowTreasure=false)
 */
public final class EnchantRoll {
    private EnchantRoll() {
    }

    /** 指定附魔台等级随机附魔，返回是否成功附上 */
    public static boolean enchant(ItemStack stack, int enchantTableLevel) {
        if (stack.isEmpty() || !stack.isEnchantable()) return false;
        if (hasEnchantments(stack)) return false;
        EnchantmentHelper.enchantItem(RandomSource.create(), stack, enchantTableLevel, false);
        return hasEnchantments(stack);
    }

    public static boolean hasEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
        return map != null && !map.isEmpty();
    }

    public static int enchantTableLevelFor(int villagerLevel) {
        return 10 * Math.max(1, villagerLevel);
    }
}
