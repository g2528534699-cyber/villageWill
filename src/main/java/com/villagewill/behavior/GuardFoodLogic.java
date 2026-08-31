package com.villagewill.behavior;

import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.GuardBuffState;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CrossbowItem;
import tallestegg.guardvillagers.entities.Guard;

/**
 * 功能1/8 + 弹药管理（§4.2 专用储物槽）：
 * - 食物槽 foodSlots 存放面包/牛排，进食时切到副手（副手原物暂存 displacedOffhand）
 * - 和平受伤→面包优先；战斗低血（<50%）→牛排优先
 * - 药水箭槽 arrowSlot 为弹药库：战斗持弩时上膛到副手（盾牌暂存），装填消耗后补弹，
 *   战斗结束收回箭槽并还原副手
 * - 装备槽（0-3）与主手（5）永远不被食物/箭占用
 * 注：GuardEatFoodGoal 会自动吃掉副手食物，这里只负责切换。
 */
public final class GuardFoodLogic {
    /** 副手药水箭弹匣容量 */
    private static final int MAGAZINE_SIZE = 8;

    private GuardFoodLogic() {
    }

    public static void tick(Guard guard) {
        if (guard.level().isClientSide || guard.isDeadOrDying()) return;
        GuardBuffState state = CapabilityRegistry.guardStateOf(guard).orElse(null);
        if (state == null) return;

        SimpleContainer inv = guard.guardInventory;
        ItemStack offhand = inv.getItem(4);
        ItemStack mainhand = inv.getItem(5);
        boolean combat = guard.getTarget() != null || guard.isAggressive();
        boolean eating = guard.isEating();
        boolean usingItem = guard.isUsingItem();

        // 举盾/使用物品期间不干预
        if (usingItem && !eating) return;

        // 1) 归还被顶替的副手物品（吃完/用完且暂存物还在）
        if (!eating && offhand.isEmpty() && !state.displacedOffhand.isEmpty()) {
            boolean stillNeedsOffhand = needsFood(guard, state, combat)
                    || (combat && isCrossbow(mainhand) && hasAmmo(state));
            if (!stillNeedsOffhand) {
                inv.setItem(4, state.displacedOffhand.copy());
                state.displacedOffhand = ItemStack.EMPTY;
                return;
            }
        }

        // 2) 低血进食（和平受伤 / 战斗低血<50%）
        if (needsFood(guard, state, combat)) {
            Item desired = combat ? Items.COOKED_BEEF : Items.BREAD;
            int foodSlot = findFoodSlot(state, desired);
            if (foodSlot < 0) foodSlot = findAnyFoodSlot(state);
            if (foodSlot >= 0 && !offhand.is(desired)) {
                // 副手药水箭先收回弹药库
                if (offhand.is(Items.TIPPED_ARROW)) {
                    mergeIntoArrowSlot(state, offhand);
                    offhand = ItemStack.EMPTY;
                }
                if (!offhand.isEmpty()) {
                    if (offhand.is(Items.SHIELD)) {
                        state.displacedOffhand = offhand.copy();
                    } else {
                        return; // 副手有其他物品，不打扰
                    }
                }
                ItemStack food = state.foodSlots[foodSlot];
                inv.setItem(4, food.copy());
                state.foodSlots[foodSlot] = ItemStack.EMPTY;
                return;
            }
        }

        // 3) 弹药弹匣管理（战斗持弩）
        if (combat && isCrossbow(mainhand)) {
            if (offhand.is(Items.TIPPED_ARROW)) {
                // 补弹：副手药水箭不足弹匣容量且弹药库有 → 补满
                if (!state.arrowSlot.isEmpty() && offhand.getCount() < MAGAZINE_SIZE) {
                    int need = Math.min(MAGAZINE_SIZE - offhand.getCount(), state.arrowSlot.getCount());
                    offhand.grow(need);
                    state.arrowSlot.shrink(need);
                }
            } else if (!state.arrowSlot.isEmpty() && (offhand.isEmpty() || offhand.is(Items.SHIELD))) {
                // 上膛：药水箭 → 副手（盾牌暂存）
                if (offhand.is(Items.SHIELD)) {
                    state.displacedOffhand = offhand.copy();
                }
                ItemStack ammo = state.arrowSlot.copy();
                int take = Math.min(MAGAZINE_SIZE, ammo.getCount());
                ammo.setCount(take);
                inv.setItem(4, ammo);
                state.arrowSlot.shrink(take);
            }
        } else if (!combat && offhand.is(Items.TIPPED_ARROW)) {
            // 收弹：战斗结束，副手药水箭归还弹药库
            mergeIntoArrowSlot(state, offhand);
            inv.setItem(4, ItemStack.EMPTY);
        }
    }

    // ---------------- 辅助 ----------------

    private static boolean needsFood(Guard guard, GuardBuffState state, boolean combat) {
        if (guard.getHealth() >= guard.getMaxHealth()) return false;
        if (combat && guard.getHealth() >= guard.getMaxHealth() / 2.0F) return false;
        return findAnyFoodSlot(state) >= 0;
    }

    private static boolean isCrossbow(ItemStack stack) {
        return stack.getItem() instanceof CrossbowItem;
    }

    /** 在食物槽找指定食物 */
    public static int findFoodSlot(GuardBuffState state, Item item) {
        for (int i = 0; i < state.foodSlots.length; i++) {
            ItemStack s = state.foodSlots[i];
            if (!s.isEmpty() && s.is(item)) return i;
        }
        return -1;
    }

    /** 在食物槽找任意食物（面包/牛排） */
    public static int findAnyFoodSlot(GuardBuffState state) {
        for (int i = 0; i < state.foodSlots.length; i++) {
            ItemStack s = state.foodSlots[i];
            if (!s.isEmpty() && (s.is(Items.BREAD) || s.is(Items.COOKED_BEEF))) return i;
        }
        return -1;
    }

    /** 警卫食物槽中是否有指定食物 */
    public static boolean hasFood(GuardBuffState state, Item item) {
        return findFoodSlot(state, item) >= 0;
    }

    /** 警卫是否完全无食物 */
    public static boolean hasNoFood(GuardBuffState state) {
        return findAnyFoodSlot(state) < 0;
    }

    /** 把食物放入食物槽（空槽或合并），失败返回 false */
    public static boolean addFood(GuardBuffState state, ItemStack stack) {
        for (int i = 0; i < state.foodSlots.length; i++) {
            ItemStack s = state.foodSlots[i];
            if (s.isEmpty()) {
                state.foodSlots[i] = stack.copy();
                return true;
            }
            if (ItemStack.isSameItemSameTags(s, stack) && s.getCount() + stack.getCount() <= s.getMaxStackSize()) {
                s.grow(stack.getCount());
                return true;
            }
        }
        return false;
    }

    /** 警卫药水箭槽是否非空 */
    public static boolean hasAmmo(GuardBuffState state) {
        return !state.arrowSlot.isEmpty();
    }

    /** 把药水箭放入箭槽（空槽或合并），失败返回 false */
    public static boolean addAmmo(GuardBuffState state, ItemStack stack) {
        if (state.arrowSlot.isEmpty()) {
            state.arrowSlot = stack.copy();
            return true;
        }
        if (ItemStack.isSameItemSameTags(state.arrowSlot, stack)
                && state.arrowSlot.getCount() + stack.getCount() <= state.arrowSlot.getMaxStackSize()) {
            state.arrowSlot.grow(stack.getCount());
            return true;
        }
        return false;
    }

    private static void mergeIntoArrowSlot(GuardBuffState state, ItemStack src) {
        if (state.arrowSlot.isEmpty()) {
            state.arrowSlot = src.copy();
        } else if (ItemStack.isSameItemSameTags(src, state.arrowSlot)) {
            state.arrowSlot.grow(src.getCount());
        }
    }
}
