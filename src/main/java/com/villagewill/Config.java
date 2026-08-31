package com.villagewill;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

/**
 * 村庄意志配置（SERVER 类型，配置文件 village_will-server.toml）
 * 全部功能开关与数值均在此；改动配置后需重进世界生效。
 */
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // ---------- 通用 ----------
    public static final ForgeConfigSpec.IntValue VILLAGE_RADIUS;
    public static final ForgeConfigSpec.BooleanValue ENHANCE_INTERACTION_ENABLED;

    // ---------- 职业经验（约2天升2级，原版1→2级需累计10XP） ----------
    public static final ForgeConfigSpec.IntValue ARMORER_XP_PER_ACTION;
    public static final ForgeConfigSpec.IntValue FARMER_XP_PER_ACTION;
    public static final ForgeConfigSpec.IntValue CLERIC_XP_PER_ACTION;
    public static final ForgeConfigSpec.IntValue WEAPONSMITH_XP_PER_ACTION;
    public static final ForgeConfigSpec.IntValue LIBRARIAN_XP_PER_ACTION;
    public static final ForgeConfigSpec.IntValue FLETCHER_XP_PER_ACTION;
    public static final ForgeConfigSpec.IntValue BUTCHER_XP_PER_ACTION;

    // ---------- 警卫食物 ----------
    public static final ForgeConfigSpec.BooleanValue GUARD_FOOD_LOGIC_ENABLED;
    public static final ForgeConfigSpec.IntValue GUARD_FOOD_CHECK_TICKS;

    // ---------- 盔甲匠 ----------
    public static final ForgeConfigSpec.BooleanValue ARMORER_ENABLED;
    public static final ForgeConfigSpec.IntValue ARMORER_USES_BASE;
    public static final ForgeConfigSpec.IntValue ARMORER_USES_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue ARMORER_REPAIR_THRESHOLD;

    // ---------- 农民 ----------
    public static final ForgeConfigSpec.BooleanValue FARMER_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> FARMER_BREAD_PER_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> FARMER_USES_PER_LEVEL;

    // ---------- 牧师 ----------
    public static final ForgeConfigSpec.BooleanValue CLERIC_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> CLERIC_RESURRECT_PER_LEVEL;

    // ---------- 武器匠 ----------
    public static final ForgeConfigSpec.BooleanValue WEAPONSMITH_ENABLED;
    public static final ForgeConfigSpec.IntValue WEAPONSMITH_USES_PER_DAY;
    public static final ForgeConfigSpec.IntValue WEAPONSMITH_REPAIR_THRESHOLD;

    // ---------- 图书管理员 ----------
    public static final ForgeConfigSpec.BooleanValue LIBRARIAN_ENABLED;
    public static final ForgeConfigSpec.IntValue LIBRARIAN_ENCHANT_LEVEL_PER_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> LIBRARIAN_USES_PER_LEVEL;

    // ---------- 制箭师 ----------
    public static final ForgeConfigSpec.BooleanValue FLETCHER_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> FLETCHER_ARROWS_PER_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> FLETCHER_EFFECT_LEVEL_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue FLETCHER_USES_PER_DAY;
    public static final ForgeConfigSpec.IntValue FLETCHER_POISON_TICKS;
    public static final ForgeConfigSpec.IntValue FLETCHER_SLOWNESS_TICKS;
    public static final ForgeConfigSpec.IntValue FLETCHER_WEAKNESS_TICKS;

    // ---------- 屠夫 ----------
    public static final ForgeConfigSpec.BooleanValue BUTCHER_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> BUTCHER_STEAK_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue BUTCHER_USES_PER_DAY;

    // ---------- 每日动作与调度 ----------
    public static final ForgeConfigSpec.IntValue ENHANCE_RECHECK_TICKS;
    public static final ForgeConfigSpec.IntValue ENHANCE_MAX_DISTANCE;

    static {
        BUILDER.push("general");
        VILLAGE_RADIUS = BUILDER.comment("村庄范围半径（格），所有“村庄内”判定的基准")
                .defineInRange("villageRadius", 64, 16, 512);
        ENHANCE_INTERACTION_ENABLED = BUILDER.comment("总开关：村民走向警卫执行强化（交互感）")
                .define("enhanceInteractionEnabled", true);
        BUILDER.pop();

        BUILDER.push("experience");
        ARMORER_XP_PER_ACTION = BUILDER.comment("盔甲匠单次动作经验（1级每日2次，约2天升2级）")
                .defineInRange("armorerXpPerAction", 3, 0, 100);
        FARMER_XP_PER_ACTION = BUILDER.comment("农民单次动作经验（1级每日2次）")
                .defineInRange("farmerXpPerAction", 3, 0, 100);
        CLERIC_XP_PER_ACTION = BUILDER.comment("牧师单次动作经验（每日1次）")
                .defineInRange("clericXpPerAction", 5, 0, 100);
        WEAPONSMITH_XP_PER_ACTION = BUILDER.comment("武器匠单次动作经验（每日1次）")
                .defineInRange("weaponsmithXpPerAction", 5, 0, 100);
        LIBRARIAN_XP_PER_ACTION = BUILDER.comment("图书管理员单次动作经验（每日1次）")
                .defineInRange("librarianXpPerAction", 5, 0, 100);
        FLETCHER_XP_PER_ACTION = BUILDER.comment("制箭师单次动作经验（每日2次）")
                .defineInRange("fletcherXpPerAction", 3, 0, 100);
        BUTCHER_XP_PER_ACTION = BUILDER.comment("屠夫单次动作经验（每日2次）")
                .defineInRange("butcherXpPerAction", 3, 0, 100);
        BUILDER.pop();

        BUILDER.push("guards");
        GUARD_FOOD_LOGIC_ENABLED = BUILDER.comment("功能1/8：警卫自动切换面包/牛排到副手进食并归还盾牌")
                .define("foodLogicEnabled", true);
        GUARD_FOOD_CHECK_TICKS = BUILDER.comment("食物逻辑检查间隔（tick）")
                .defineInRange("foodCheckTicks", 10, 1, 200);
        BUILDER.pop();

        BUILDER.push("armorer");
        ARMORER_ENABLED = BUILDER.comment("盔甲匠：给警卫发甲/升级甲/维修甲")
                .define("enabled", true);
        ARMORER_USES_BASE = BUILDER.comment("每日升级次数基数（1级时）")
                .defineInRange("usesBase", 2, 0, 20);
        ARMORER_USES_PER_LEVEL = BUILDER.comment("每升一级额外增加每日升级次数")
                .defineInRange("usesPerLevel", 1, 0, 10);
        ARMORER_REPAIR_THRESHOLD = BUILDER.comment("盔甲耐久低于该百分比时维修（0-100），维修无消耗")
                .defineInRange("repairThresholdPercent", 60, 0, 100);
        BUILDER.pop();

        BUILDER.push("farmer");
        FARMER_ENABLED = BUILDER.comment("农民：给警卫补充面包（凭空生成，不消耗农民食物）")
                .define("enabled", true);
        FARMER_BREAD_PER_LEVEL = BUILDER.comment("各职业等级单次补给面包数 [1,2,3,4,5] 级（1/3/5级为用户指定，2/4级插值）")
                .defineList("breadPerLevel", Arrays.asList(6, 8, 10, 12, 14), o -> o instanceof Integer);
        FARMER_USES_PER_LEVEL = BUILDER.comment("各职业等级每日补给次数 [1,2,3,4,5] 级")
                .defineList("usesPerLevel", Arrays.asList(2, 2, 3, 3, 4), o -> o instanceof Integer);
        BUILDER.pop();

        BUILDER.push("cleric");
        CLERIC_ENABLED = BUILDER.comment("牧师：每天日出时复活（生成）警卫村民")
                .define("enabled", true);
        CLERIC_RESURRECT_PER_LEVEL = BUILDER.comment("各职业等级每日复活数 [1,2,3,4,5] 级；上限=guardvillagers 配置的每村庄警卫生成数")
                .defineList("resurrectPerLevel", Arrays.asList(1, 1, 2, 2, 3), o -> o instanceof Integer);
        BUILDER.pop();

        BUILDER.push("weaponsmith");
        WEAPONSMITH_ENABLED = BUILDER.comment("武器匠：给警卫升级武器/维修武器（弩、弓只修不升）")
                .define("enabled", true);
        WEAPONSMITH_USES_PER_DAY = BUILDER.comment("每日武器升级次数")
                .defineInRange("usesPerDay", 1, 0, 10);
        WEAPONSMITH_REPAIR_THRESHOLD = BUILDER.comment("武器耐久低于该百分比时维修（0-100），维修无消耗")
                .defineInRange("repairThresholdPercent", 50, 0, 100);
        BUILDER.pop();

        BUILDER.push("librarian");
        LIBRARIAN_ENABLED = BUILDER.comment("图书管理员：随机附魔警卫的武器/盔甲（附魔台规则）")
                .define("enabled", true);
        LIBRARIAN_ENCHANT_LEVEL_PER_LEVEL = BUILDER.comment("附魔等级 = 职业等级 × 该值（如 10 → 1级=附魔台10级）")
                .defineInRange("enchantLevelPerLevel", 10, 1, 30);
        LIBRARIAN_USES_PER_LEVEL = BUILDER.comment("各职业等级每日附魔次数 [1,2,3,4,5] 级")
                .defineList("usesPerLevel", Arrays.asList(1, 1, 2, 2, 3), o -> o instanceof Integer);
        BUILDER.pop();

        BUILDER.push("fletcher");
        FLETCHER_ENABLED = BUILDER.comment("制箭师：制作剧毒/迟缓/虚弱药水箭并给予持弩警卫")
                .define("enabled", true);
        FLETCHER_ARROWS_PER_LEVEL = BUILDER.comment("各职业等级单次给药水箭数 [1,2,3,4,5] 级（1/3/5级为用户指定，2/4级插值）")
                .defineList("arrowsPerLevel", Arrays.asList(8, 8, 12, 12, 16), o -> o instanceof Integer);
        FLETCHER_EFFECT_LEVEL_PER_LEVEL = BUILDER.comment("各职业等级药水效果等级（0=I,1=II,2=III）")
                .defineList("effectLevelPerLevel", Arrays.asList(0, 0, 1, 1, 2), o -> o instanceof Integer);
        FLETCHER_USES_PER_DAY = BUILDER.comment("每日赠送次数")
                .defineInRange("usesPerDay", 2, 0, 10);
        FLETCHER_POISON_TICKS = BUILDER.comment("剧毒箭效果时长（tick）")
                .defineInRange("poisonTicks", 300, 20, 12000);
        FLETCHER_SLOWNESS_TICKS = BUILDER.comment("迟缓箭效果时长（tick）")
                .defineInRange("slownessTicks", 400, 20, 12000);
        FLETCHER_WEAKNESS_TICKS = BUILDER.comment("虚弱箭效果时长（tick）")
                .defineInRange("weaknessTicks", 300, 20, 12000);
        BUILDER.pop();

        BUILDER.push("butcher");
        BUTCHER_ENABLED = BUILDER.comment("屠夫：给警卫牛排")
                .define("enabled", true);
        BUTCHER_STEAK_PER_LEVEL = BUILDER.comment("各职业等级单次给牛排数 [1,2,3,4,5] 级")
                .defineList("steakPerLevel", Arrays.asList(1, 2, 3, 4, 5), o -> o instanceof Integer);
        BUTCHER_USES_PER_DAY = BUILDER.comment("每日赠送次数")
                .defineInRange("usesPerDay", 2, 0, 10);
        BUILDER.pop();

        BUILDER.push("schedule");
        ENHANCE_RECHECK_TICKS = BUILDER.comment("村民强化目标重评估间隔（tick）")
                .defineInRange("enhanceRecheckTicks", 40, 5, 400);
        ENHANCE_MAX_DISTANCE = BUILDER.comment("村民寻找强化目标的最大距离（格，超过则本次跳过）")
                .defineInRange("enhanceMaxDistance", 48, 8, 128);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static final ForgeConfigSpec SPEC;

    // ---------- 便捷读取 ----------
    public static int levelValue(List<? extends Integer> list, int villagerLevel) {
        int idx = Math.max(0, Math.min(list.size() - 1, villagerLevel - 1));
        return list.get(idx);
    }

    public static int armorerUsesPerDay(int villagerLevel) {
        return ARMORER_USES_BASE.get() + ARMORER_USES_PER_LEVEL.get() * (villagerLevel - 1);
    }
}
