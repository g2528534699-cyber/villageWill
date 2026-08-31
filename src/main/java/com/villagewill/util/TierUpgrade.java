package com.villagewill.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * 装备/武器层级路径工具
 * 盔甲：皮革(0) → 锁链(1) → 铁(2) → 钻石(3) → 下界合金(4)（跳过金）
 * 武器：石(0) → 铁(1) → 钻石(2) → 下界合金(3)（跳过金/木）
 */
public final class TierUpgrade {
    private TierUpgrade() {
    }

    // ---------------- 盔甲 ----------------

    public static int maxArmorTier(int villagerLevel) {
        return Math.max(0, villagerLevel - 1); // 1级→0(皮革) … 5级→4(下界合金)
    }

    /** 当前盔甲材质层级；非盔甲返回 -1 */
    public static int armorTierOf(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return -1;
        ArmorMaterial material = armor.getMaterial();
        if (material == ArmorMaterials.LEATHER) return 0;
        if (material == ArmorMaterials.CHAIN) return 1;
        if (material == ArmorMaterials.GOLD) return 1;
        if (material == ArmorMaterials.IRON) return 2;
        if (material == ArmorMaterials.DIAMOND) return 3;
        if (material == ArmorMaterials.NETHERITE) return 4;
        return -1;
    }

    /** 按层级+槽位构造盔甲物品（金/其他材质归为最近层级） */
    @Nullable
    public static Item armorItemForTier(int tier, EquipmentSlot slot) {
        int t = Math.max(0, Math.min(4, tier));
        return switch (slot) {
            case HEAD -> switch (t) {
                case 0 -> Items.LEATHER_HELMET;
                case 1 -> Items.CHAINMAIL_HELMET;
                case 2 -> Items.IRON_HELMET;
                case 3 -> Items.DIAMOND_HELMET;
                default -> Items.NETHERITE_HELMET;
            };
            case CHEST -> switch (t) {
                case 0 -> Items.LEATHER_CHESTPLATE;
                case 1 -> Items.CHAINMAIL_CHESTPLATE;
                case 2 -> Items.IRON_CHESTPLATE;
                case 3 -> Items.DIAMOND_CHESTPLATE;
                default -> Items.NETHERITE_CHESTPLATE;
            };
            case LEGS -> switch (t) {
                case 0 -> Items.LEATHER_LEGGINGS;
                case 1 -> Items.CHAINMAIL_LEGGINGS;
                case 2 -> Items.IRON_LEGGINGS;
                case 3 -> Items.DIAMOND_LEGGINGS;
                default -> Items.NETHERITE_LEGGINGS;
            };
            case FEET -> switch (t) {
                case 0 -> Items.LEATHER_BOOTS;
                case 1 -> Items.CHAINMAIL_BOOTS;
                case 2 -> Items.IRON_BOOTS;
                case 3 -> Items.DIAMOND_BOOTS;
                default -> Items.NETHERITE_BOOTS;
            };
            default -> null;
        };
    }

    // ---------------- 武器 ----------------

    public static int maxWeaponTier(int villagerLevel) {
        return Math.max(0, villagerLevel - 1);
    }

    /** 武器材质层级；非可升级武器返回 -1 */
    public static int weaponTierOf(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.STONE_SWORD || item == Items.STONE_AXE) return 0;
        if (item == Items.IRON_SWORD || item == Items.IRON_AXE) return 1;
        if (item == Items.DIAMOND_SWORD || item == Items.DIAMOND_AXE) return 2;
        if (item == Items.NETHERITE_SWORD || item == Items.NETHERITE_AXE) return 3;
        return -1;
    }

    public static boolean isUpgradeableWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    /** 升一级武器（同类型）；无法升级返回 null */
    @Nullable
    public static Item nextWeaponTier(ItemStack stack) {
        Item item = stack.getItem();
        int tier = weaponTierOf(stack);
        if (tier < 0 || tier >= 3) return null;
        return switch (tier) {
            case 0 -> item == Items.STONE_SWORD ? Items.IRON_SWORD : Items.IRON_AXE;
            case 1 -> item == Items.IRON_SWORD ? Items.DIAMOND_SWORD : Items.DIAMOND_AXE;
            default -> item == Items.DIAMOND_SWORD ? Items.NETHERITE_SWORD : Items.NETHERITE_AXE;
        };
    }

    /** 常见材质名（调试/日志用） */
    public static String describe(ItemStack stack) {
        return stack.getItem().getDescriptionId();
    }
}
