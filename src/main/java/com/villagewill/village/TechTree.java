package com.villagewill.village;

import com.villagewill.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * 村庄核心科技树（自主升级）：
 * - 每 TECH_CHECK_TICKS 检查一次（由核心方块实体 ticker 驱动）
 * - 预算：升级可用 = 余额 × (1 - threatSpendRatio)（其余留给威胁召唤保底）
 * - 购买优先级：信标效果 → 信标范围 → 围墙 → 队长 → 护卫 → 警卫（每次只买一项）
 * - 成本：见 Config tech_tree 段（基础+每级递增/倍数）
 * - 队长/护卫/警卫等级由 Part 3 消费（血量/攻击/装备/附魔）
 */
public final class TechTree {
    private TechTree() {
    }

    public static void tick(ServerLevel level, BlockPos corePos, VillageState state) {
        if (!state.isCoreActive() || state.isCoreDamaged()) return;
        // 队长阵亡待复活：暂停科技消费，优先攒复活金（否则余额永远不够复活成本）
        if (state.captainUUID() != null && state.isCaptainDead()) return;
        int max = Config.TECH_MAX_LEVEL.get();

        // 1) 信标效果（0=再生I，1-5解锁效果，6=全部II）
        if (state.beaconEffectLevel() < max + 1) {
            int cost = Config.TECH_BEACON_EFFECT_COST_BASE.get()
                    + Config.TECH_BEACON_EFFECT_COST_PER_LEVEL.get() * state.beaconEffectLevel();
            if (buy(level, state, cost, () -> {
                state.setBeaconEffectLevel(state.beaconEffectLevel() + 1);
                LogUtils.getLogger().info("[VW] 科技: 信标效果升级至 {}", state.beaconEffectLevel());
            })) return;
        }
        // 2) 信标范围
        if (state.beaconRangeLevel() < max) {
            int cost = Config.TECH_BEACON_RANGE_COST_BASE.get()
                    + Config.TECH_BEACON_RANGE_COST_PER_LEVEL.get() * state.beaconRangeLevel();
            if (buy(level, state, cost, () -> {
                state.setBeaconRangeLevel(state.beaconRangeLevel() + 1);
                LogUtils.getLogger().info("[VW] 科技: 信标范围升级至 {}", state.beaconRangeLevel());
            })) return;
        }
        // 3) 围墙（1=圆石 2=石砖 3=深板岩砖）
        if (state.wallLevel() < 3) {
            int cost = (int) (Config.TECH_WALL_COST_BASE.get()
                    * Math.pow(Config.TECH_WALL_COST_MULTIPLIER.get(), state.wallLevel()));
            if (buy(level, state, cost, () -> {
                state.setWallLevel(state.wallLevel() + 1);
                WallBuilder.buildOrUpgrade(level, corePos, state);
                LogUtils.getLogger().info("[VW] 科技: 围墙升级至 {}", state.wallLevel());
            })) return;
        }
        // 4) 队长科技（Part 3 消费）
        if (state.captainTechLevel() < max) {
            int cost = Config.TECH_CAPTAIN_COST_PER_LEVEL.get() * (state.captainTechLevel() + 1);
            if (buy(level, state, cost, () -> {
                state.setCaptainTechLevel(state.captainTechLevel() + 1);
                LogUtils.getLogger().info("[VW] 科技: 队长科技升级至 {}", state.captainTechLevel());
            })) return;
        }
        // 5) 护卫科技
        if (state.escortTechLevel() < max) {
            int cost = Config.TECH_ESCORT_COST_PER_LEVEL.get() * (state.escortTechLevel() + 1);
            if (buy(level, state, cost, () -> {
                state.setEscortTechLevel(state.escortTechLevel() + 1);
                LogUtils.getLogger().info("[VW] 科技: 护卫科技升级至 {}", state.escortTechLevel());
            })) return;
        }
        // 6) 警卫科技
        if (state.guardTechLevel() < max) {
            int cost = Config.TECH_GUARD_COST_PER_LEVEL.get() * (state.guardTechLevel() + 1);
            if (buy(level, state, cost, () -> {
                state.setGuardTechLevel(state.guardTechLevel() + 1);
                LogUtils.getLogger().info("[VW] 科技: 警卫科技升级至 {}", state.guardTechLevel());
            })) return;
        }
    }

    /** 预算内购买（升级预算 = 余额×(1-spendRatio)，威胁召唤保底） */
    private static boolean buy(ServerLevel level, VillageState state, int cost, Runnable apply) {
        if (cost <= 0) return false;
        long balance = state.emeraldBalance();
        long upgradeBudget = (long) (balance * (1.0 - Config.THREAT_SPEND_RATIO.get()));
        if (balance < cost || upgradeBudget < cost) return false;
        if (state.spendEmeralds(cost)) {
            apply.run();
            return true;
        }
        return false;
    }
}
