package com.villagewill.behavior.actions;

import com.villagewill.Config;
import com.villagewill.behavior.GuardFoodLogic;
import com.villagewill.capability.CapabilityRegistry;
import com.villagewill.capability.GuardBuffState;
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
 * - 放入专用药水箭槽（§4.2 弹药库，不占装备槽）；战斗时由 GuardFoodLogic 上膛到副手
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
    public int dailyUses(int villagerLevel) {
        return Config.FLETCHER_USES_PER_DAY.get();
    }

    @Override
    public boolean canApplyTo(Guard guard, int villagerLevel) {
        // 仅主手持弩的警卫，且药水箭槽为空
        if (!(guard.guardInventory.getItem(5).getItem() instanceof net.minecraft.world.item.CrossbowItem)) return false;
        GuardBuffState state = CapabilityRegistry.guardStateOf(guard).orElse(null);
        return state != null && !GuardFoodLogic.hasAmmo(state);
    }

    @Override
    public boolean execute(ServerLevel level, Villager villager, Guard guard, VillagerJobMemory memory) {
        if (!(guard.guardInventory.getItem(5).getItem() instanceof net.minecraft.world.item.CrossbowItem)) return false;
        GuardBuffState state = CapabilityRegistry.guardStateOf(guard).orElse(null);
        if (state == null) return false;
        int villagerLevel = villager.getVillagerData().getLevel();
        int count = Config.levelValue(Config.FLETCHER_ARROWS_PER_LEVEL.get(), villagerLevel);
        int effectLevel = Config.levelValue(Config.FLETCHER_EFFECT_LEVEL_PER_LEVEL.get(), villagerLevel);

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
        if (!GuardFoodLogic.addAmmo(state, arrows)) return false;
        memory.consumeUse(ACTION_ID, dailyUses(villagerLevel));
        return true;
    }
}
