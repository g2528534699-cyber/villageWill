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

    // ---------- 村庄核心：威胁召唤（村民受攻击时耗绿宝石召唤傀儡） ----------
    public static final ForgeConfigSpec.BooleanValue THREAT_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> THREAT_WEIGHTS;
    public static final ForgeConfigSpec.DoubleValue THREAT_COST_PER_THREAT;
    public static final ForgeConfigSpec.IntValue THREAT_MAX_GOLEMS;
    public static final ForgeConfigSpec.IntValue THREAT_IRON_THRESHOLD;
    public static final ForgeConfigSpec.IntValue THREAT_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue THREAT_COOLDOWN_TICKS;

    // ---------- 石匠 ----------
    public static final ForgeConfigSpec.BooleanValue MASON_ENABLED;
    public static final ForgeConfigSpec.IntValue MASON_MAX_HOUSES;
    public static final ForgeConfigSpec.IntValue MASON_HOUSES_PER_DAY;
    public static final ForgeConfigSpec.IntValue MASON_HUT_SIZE;
    public static final ForgeConfigSpec.IntValue MASON_HUT_SPACING;
    public static final ForgeConfigSpec.IntValue MASON_XP_PER_ACTION;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MASON_JOB_BLOCKS;

    // ---------- 石傀儡 ----------
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> GOLEM_DAMAGE_PER_TIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> GOLEM_HP_PER_TIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> GOLEM_ARMOR_PER_TIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> GOLEM_TOUGHNESS_PER_TIER;
    public static final ForgeConfigSpec.IntValue GOLEM_SLOW_TICKS;
    public static final ForgeConfigSpec.IntValue GOLEM_MAX_PER_MASON;

    // ---------- 牧羊人 ----------
    public static final ForgeConfigSpec.BooleanValue SHEPHERD_ENABLED;
    public static final ForgeConfigSpec.IntValue SHEPHERD_BEDS_PER_DAY;
    public static final ForgeConfigSpec.IntValue SHEPHERD_BEDS_PER_HOUSE;
    public static final ForgeConfigSpec.IntValue SHEPHERD_DOGS;
    public static final ForgeConfigSpec.DoubleValue SHEPHERD_DOG_HP_BASE;
    public static final ForgeConfigSpec.DoubleValue SHEPHERD_DOG_ATTACK_BASE;
    public static final ForgeConfigSpec.DoubleValue SHEPHERD_DOG_SCALE_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue SHEPHERD_XP_PER_ACTION;

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

        BUILDER.push("mason");
        MASON_ENABLED = BUILDER.comment("石匠：村庄内建造村民小屋（每天最多1栋，全村≤上限）")
                .define("enabled", true);
        MASON_MAX_HOUSES = BUILDER.comment("一个村庄内最多建造的小屋数")
                .defineInRange("maxHousesPerVillage", 15, 1, 64);
        MASON_HOUSES_PER_DAY = BUILDER.comment("每个石匠每日最多建造数")
                .defineInRange("housesPerDay", 1, 0, 10);
        MASON_HUT_SIZE = BUILDER.comment("小屋边长（格，奇数）")
                .defineInRange("hutSize", 7, 5, 15);
        MASON_HUT_SPACING = BUILDER.comment("小屋地块间距（格）")
                .defineInRange("hutSpacing", 9, 6, 24);
        MASON_XP_PER_ACTION = BUILDER.comment("石匠建屋单次经验（每日1次，约2天升2级）")
                .defineInRange("masonXpPerAction", 5, 0, 100);
        MASON_JOB_BLOCKS = BUILDER.comment("小屋内的职业方块池（全随机取2个）")
                .defineList("jobBlocks", Arrays.asList(
                        "minecraft:smithing_table", "minecraft:stonecutter", "minecraft:cartography_table",
                        "minecraft:fletching_table", "minecraft:grindstone", "minecraft:loom",
                        "minecraft:barrel", "minecraft:smoker", "minecraft:blast_furnace",
                        "minecraft:lectern", "minecraft:composter", "minecraft:cauldron",
                        "minecraft:furnace", "minecraft:brewing_stand"),
                        o -> o instanceof String);
        BUILDER.pop();

        BUILDER.push("stone_golem");
        GOLEM_DAMAGE_PER_TIER = BUILDER.comment("石傀儡各等级攻击力 [1..5]")
                .defineList("damagePerTier", Arrays.asList(2.0, 3.0, 4.0, 5.0, 7.0), o -> o instanceof Double);
        GOLEM_HP_PER_TIER = BUILDER.comment("石傀儡各等级生命 [1..5]")
                .defineList("hpPerTier", Arrays.asList(15.0, 18.0, 21.0, 24.0, 30.0), o -> o instanceof Double);
        GOLEM_ARMOR_PER_TIER = BUILDER.comment("石傀儡各等级护甲 [1..5]")
                .defineList("armorPerTier", Arrays.asList(10.0, 12.0, 14.0, 16.0, 20.0), o -> o instanceof Double);
        GOLEM_TOUGHNESS_PER_TIER = BUILDER.comment("石傀儡各等级韧性 [1..5]")
                .defineList("toughnessPerTier", Arrays.asList(1.0, 1.5, 2.0, 2.5, 3.0), o -> o instanceof Double);
        GOLEM_SLOW_TICKS = BUILDER.comment("石头球命中缓慢时长（tick）")
                .defineInRange("slowTicks", 200, 20, 12000);
        GOLEM_MAX_PER_MASON = BUILDER.comment("每个石匠最多可拥有的石傀儡数")
                .defineInRange("maxPerMason", 2, 0, 8);
        BUILDER.pop();

        BUILDER.push("shepherd");
        SHEPHERD_ENABLED = BUILDER.comment("牧羊人：在小屋内放床、认领职业后获得驯服狗")
                .define("enabled", true);
        SHEPHERD_BEDS_PER_DAY = BUILDER.comment("牧羊人每日放床次数")
                .defineInRange("bedsPerDay", 2, 0, 10);
        SHEPHERD_BEDS_PER_HOUSE = BUILDER.comment("每间小屋最多床数")
                .defineInRange("bedsPerHouse", 2, 1, 4);
        SHEPHERD_DOGS = BUILDER.comment("认领牧羊人职业后获得的狗数量")
                .defineInRange("dogs", 2, 0, 8);
        SHEPHERD_DOG_HP_BASE = BUILDER.comment("狗基础生命（×等级缩放）")
                .defineInRange("dogHpBase", 20.0, 1.0, 200.0);
        SHEPHERD_DOG_ATTACK_BASE = BUILDER.comment("狗基础攻击（×等级缩放）")
                .defineInRange("dogAttackBase", 4.0, 1.0, 100.0);
        SHEPHERD_DOG_SCALE_PER_LEVEL = BUILDER.comment("狗属性每级缩放系数（1+系数×(等级-1)）")
                .defineInRange("dogScalePerLevel", 0.2, 0.0, 1.0);
        SHEPHERD_XP_PER_ACTION = BUILDER.comment("牧羊人放床单次经验（每日2次）")
                .defineInRange("shepherdXpPerAction", 3, 0, 100);
        BUILDER.pop();

        BUILDER.push("threat");
        THREAT_ENABLED = BUILDER.comment("村庄核心威胁召唤：村民受敌对生物攻击时，按威胁值耗绿宝石召唤傀儡（限时）")
                .define("enabled", true);
        THREAT_WEIGHTS = BUILDER.comment("威胁权重表（modid:entity=权重，未列出=1）")
                .defineList("weights", Arrays.asList(
                        "minecraft:zombie=1", "minecraft:husk=2", "minecraft:drowned=2",
                        "minecraft:skeleton=2", "minecraft:stray=2", "minecraft:spider=1",
                        "minecraft:cave_spider=2", "minecraft:creeper=3", "minecraft:slime=1",
                        "minecraft:witch=6", "minecraft:pillager=5", "minecraft:vindicator=6",
                        "minecraft:evoker=8", "minecraft:ravager=10", "minecraft:vex=2",
                        "minecraft:phantom=3", "minecraft:enderman=4", "minecraft:zombie_villager=2",
                        "minecraft:zoglin=6", "minecraft:hoglin=4", "minecraft:blaze=4",
                        "minecraft:ghast=6", "minecraft:magma_cube=2", "minecraft:wither_skeleton=5",
                        "minecraft:warden=30", "minecraft:wither=50", "minecraft:ender_dragon=100"),
                        o -> o instanceof String);
        THREAT_COST_PER_THREAT = BUILDER.comment("每威胁值消耗绿宝石（消耗=ceil(威胁值×系数)，最小1；默认僵尸≈1颗）")
                .defineInRange("costPerThreat", 0.5, 0.05, 10.0);
        THREAT_MAX_GOLEMS = BUILDER.comment("单次最多召唤傀儡数")
                .defineInRange("maxGolemsPerCall", 3, 1, 10);
        THREAT_IRON_THRESHOLD = BUILDER.comment("威胁值≥该值时召唤铁傀儡，否则召唤石傀儡")
                .defineInRange("ironGolemThreshold", 8, 1, 100);
        THREAT_DURATION_TICKS = BUILDER.comment("召唤傀儡存在时长（tick，默认600=30秒）")
                .defineInRange("durationTicks", 600, 100, 72000);
        THREAT_COOLDOWN_TICKS = BUILDER.comment("同一村庄触发冷却（tick，防止连续攻击刷傀儡）")
                .defineInRange("cooldownTicks", 100, 20, 12000);
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
