package com.villagewill.behavior.actions;

import com.villagewill.Config;
import com.villagewill.capability.VillagerJobMemory;
import com.villagewill.util.EnchantRoll;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.AxeItem;
import tallestegg.guardvillagers.entities.Guard;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能6：图书管理员附魔警卫的武器/盔甲
 * - 附魔台规则随机附魔（EnchantmentHelper.enchantItem，等级=10×职业等级）
 * - 只附魔没有附魔的装备（升级后新装备无附魔→可附；维修保留附魔→不再附）
 * - 每日次数=(等级+1)/2（1级1、3级2、5级3）
 */
public final class LibrarianAction implements ProfessionAction {
    public static final String ACTION_ID = "librarian";

    @Override
    public String id() {
        return ACTION_ID;
    }

    @Override
    public int xpPerAction() {
        return Config.LIBRARIAN_XP_PER_ACTION.get();
    }

    @Override
    public boolean canApplyTo(Guard guard, int villagerLevel) {
        return !candidates(guard).isEmpty();
    }

    @Override
    public boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory) {
        List<ItemStack> candidates = candidates(guard);
        if (candidates.isEmpty()) return false;
        int enchantLevel = EnchantRoll.enchantTableLevelFor(villager.getVillagerData().getLevel());
        // 随机顺序尝试附魔（某些装备可能没有可用的附魔）
        java.util.Collections.shuffle(candidates, new java.util.Random(level.random.nextLong()));
        for (ItemStack stack : candidates) {
            if (EnchantRoll.enchant(stack, enchantLevel)) {
                memory.consumeUse(ACTION_ID);
                return true;
            }
        }
        return false;
    }

    /** 收集可附魔且未附魔的装备（武器优先：主手，其次盔甲槽0-3） */
    private static List<ItemStack> candidates(Guard guard) {
        List<ItemStack> result = new ArrayList<>();
        ItemStack mainhand = guard.guardInventory.getItem(5);
        if (isEnchantableWeapon(mainhand) && !EnchantRoll.hasEnchantments(mainhand)) {
            result.add(mainhand);
        }
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = guard.guardInventory.getItem(slot);
            if (stack.getItem() instanceof ArmorItem && !EnchantRoll.hasEnchantments(stack)) {
                result.add(stack);
            }
        }
        return result;
    }

    private static boolean isEnchantableWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem;
    }
}
