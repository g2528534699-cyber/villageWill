package com.villagewill.behavior;

import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.GuardBuffState;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 功能1/8：警卫食物逻辑（全部服务端）
 * - 不遇敌且受伤时：把物品栏的面包切到副手进食（原版只吃副手食物）
 * - 副手被顶替的盾牌在吃完后归还
 * - 食物优先级：遇敌触发回血→牛排优先；和平回血→面包优先
 * 注：GuardEatFoodGoal 会自动吃掉副手食物，这里只负责切换。
 */
public final class GuardFoodLogic {
    private GuardFoodLogic() {
    }

    public static void tick(Guard guard) {
        if (guard.level().isClientSide || guard.isDeadOrDying()) return;
        GuardBuffState state = CapabilityRegistry.guardStateOf(guard).orElse(null);
        if (state == null) return;

        SimpleContainer inv = guard.guardInventory;
        ItemStack offhand = inv.getItem(4);
        boolean combat = guard.getTarget() != null || guard.isAggressive();
        boolean eating = guard.isEating();
        boolean usingItem = guard.isUsingItem();

        // 举盾/使用物品期间不干预
        if (usingItem && !eating) return;

        // 1) 归还被顶替的盾牌（吃完面包/牛排后）
        if (!eating && offhand.isEmpty() && !state.displacedShield.isEmpty()) {
            boolean willPlaceFood = guard.getHealth() < guard.getMaxHealth() && hasAnyFood(inv);
            if (!willPlaceFood) {
                inv.setItem(4, state.displacedShield.copy());
                state.displacedShield = ItemStack.EMPTY;
                return;
            }
        }

        if (guard.getHealth() >= guard.getMaxHealth()) return;

        // 2) 按优先级选食物
        Item desired = combat ? Items.COOKED_BEEF : Items.BREAD;
        int slot = findItemSlot(inv, desired);
        if (slot < 0) slot = findItemSlot(inv, combat ? Items.BREAD : Items.COOKED_BEEF);
        if (slot < 0 || slot == 4) return; // 已在副手或没有食物

        // 3) 切换食物到副手
        if (combat) {
            // 遇敌：只有血量低于一半（触发跑开回血）且副手是盾牌时才换牛排
            if (guard.getHealth() >= guard.getMaxHealth() / 2.0F) return;
            if (!offhand.isEmpty() && !offhand.is(Items.SHIELD)) return;
        } else {
            // 和平：副手有非盾牌物品时不打扰
            if (!offhand.isEmpty() && !offhand.is(Items.SHIELD)) return;
        }
        if (!offhand.isEmpty() && offhand.is(Items.SHIELD)) {
            state.displacedShield = offhand.copy();
        }
        ItemStack food = inv.getItem(slot);
        inv.setItem(4, food.copy());
        inv.setItem(slot, ItemStack.EMPTY);
    }

    public static boolean hasAnyFood(SimpleContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && (s.is(Items.BREAD) || s.is(Items.COOKED_BEEF))) return true;
        }
        return false;
    }

    /** 在物品栏找食物（跳过主手槽5；副手槽4也算，若已在副手则由进食AI处理） */
    public static int findItemSlot(SimpleContainer inv, Item item) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(item)) return i;
        }
        return -1;
    }
}
