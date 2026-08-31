package com.villagewill.behavior.actions;

import com.villagewill.Config;
import com.villagewill.capability.VillagerJobMemory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import tallestegg.guardvillagers.entities.Guard;

import java.util.List;

/**
 * 功能7：制箭师制作药水箭（剧毒/迟缓/虚弱）并给予持弩警卫
 * - 每次随机送一种类型；每日2次
 * - 1级8支效果I、3级12支效果II、5级16支效果III（2/4级插值）
 * - 放入警卫物品栏（槽0优先，装填弹药时最先被扫描→优先使用药水箭）
 * - 药水箭随弩消耗、普通箭免费——guardvillagers 原生机制，无需干预
 */
public final class FletcherAction implements ProfessionAction {
    public static final String ACTION_ID = "fletcher";

    private static final MobEffect[] EFFECTS = {MobEffects.POISON, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.WEAKNESS};

    @Override
    public String id() {
        return ACTION_ID;
    }

    @Override
    public int xpPerAction() {
        return Config.FLETCHER_XP_PER_ACTION.get();
    }

    @Override
    public boolean canApplyTo(Guard guard, int villagerLevel) {
        // 仅主手持弩的警卫，且物品栏中没有药水箭
        if (!(guard.guardInventory.getItem(5).getItem() instanceof net.minecraft.world.item.CrossbowItem)) return false;
        return ActionUtil.findItemSlot(guard.guardInventory, Items.TIPPED_ARROW) < 0;
    }

    @Override
    public boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory) {
        if (!(guard.guardInventory.getItem(5).getItem() instanceof net.minecraft.world.item.CrossbowItem)) return false;
        int villagerLevel = villager.getVillagerData().getLevel();
        int count = Config.levelValue(Config.FLETCHER_ARROWS_PER_LEVEL.get(), villagerLevel);
        int effectLevel = Config.levelValue(Config.FLETCHER_EFFECT_LEVEL_PER_LEVEL.get(), villagerLevel);

        int slot = ActionUtil.firstEmptySlot(guard.guardInventory, 0, 4); // 槽0-3，槽0最先被弹药扫描
        if (slot < 0) return false;

        // 随机选一种效果
        MobEffect effect = EFFECTS[level.random.nextInt(EFFECTS.length)];
        int duration;
        if (effect == MobEffects.POISON) {
            duration = Config.FLETCHER_POISON_TICKS.get();
        } else if (effect == MobEffects.MOVEMENT_SLOWDOWN) {
            duration = Config.FLETCHER_SLOWNESS_TICKS.get();
        } else {
            duration = Config.FLETCHER_WEAKNESS_TICKS.get();
        }

        ItemStack arrows = new ItemStack(Items.TIPPED_ARROW, count);
        PotionUtils.setCustomEffects(arrows, List.of(new MobEffectInstance(effect, duration, effectLevel)));
        guard.guardInventory.setItem(slot, arrows);
        memory.consumeUse(ACTION_ID);
        return true;
    }
}
