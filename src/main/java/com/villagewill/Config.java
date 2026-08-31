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
    public static final ForgeConfigSpec.BooleanValue THREAT_ATTR_EVALUATION;
    public static final ForgeConfigSpec.DoubleValue THREAT_SPEND_RATIO;
    public static final ForgeConfigSpec.IntValue THREAT_IRON_COST;
    public static final ForgeConfigSpec.IntValue THREAT_STONE_COST;
    public static final ForgeConfigSpec.IntValue THREAT_IRON_POWER;
    public static final ForgeConfigSpec.IntValue THREAT_STONE_POWER;
    public static final ForgeConfigSpec.IntValue THREAT_MAX_GOLEMS;
    public static final ForgeConfigSpec.IntValue THREAT_IRON_THRESHOLD;
    public static final ForgeConfigSpec.IntValue THREAT_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue THREAT_COOLDOWN_TICKS;

    // ---------- 村庄核心 ----------
    public static final ForgeConfigSpec.IntValue CORE_VILLAGER_THRESHOLD;
    public static final ForgeConfigSpec.IntValue CORE_CONVERT_CHECK_TICKS;
    public static final ForgeConfigSpec.IntValue CORE_FORCE_LOAD_RADIUS;
    public static final ForgeConfigSpec.IntValue CORE_DAMAGE_CHECK_TICKS;
    public static final ForgeConfigSpec.IntValue CORE_DAMAGE_THRESHOLD_TICKS;
    public static final ForgeConfigSpec.IntValue CORE_REPAIR_THRESHOLD_TICKS;

    // ---------- 科技树（核心自主升级） ----------
    public static final ForgeConfigSpec.IntValue TECH_CHECK_TICKS;
    public static final ForgeConfigSpec.IntValue TECH_MAX_LEVEL;
    public static final ForgeConfigSpec.IntValue TECH_BEACON_EFFECT_COST_BASE;
    public static final ForgeConfigSpec.IntValue TECH_BEACON_EFFECT_COST_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue TECH_BEACON_RANGE_COST_BASE;
    public static final ForgeConfigSpec.IntValue TECH_BEACON_RANGE_COST_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue TECH_WALL_COST_BASE;
    public static final ForgeConfigSpec.IntValue TECH_WALL_COST_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue TECH_CAPTAIN_COST_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue TECH_ESCORT_COST_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue TECH_GUARD_COST_PER_LEVEL;

    // ---------- 信标光环 ----------
    public static final ForgeConfigSpec.IntValue BEACON_RADIUS_BASE;
    public static final ForgeConfigSpec.IntValue BEACON_RADIUS_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue BEACON_REPUTATION_THRESHOLD;
    public static final ForgeConfigSpec.IntValue BEACON_TICK_INTERVAL;

    // ---------- 围墙 ----------
    public static final ForgeConfigSpec.IntValue WALL_RADIUS;
    public static final ForgeConfigSpec.IntValue WALL_HEIGHT;
    public static final ForgeConfigSpec.IntValue WALL_GATE_WIDTH;

    // ---------- 警卫队长（Part 3） ----------
    public static final ForgeConfigSpec.BooleanValue CAPTAIN_ENABLED;
    public static final ForgeConfigSpec.IntValue CAPTAIN_HEALTH;
    public static final ForgeConfigSpec.IntValue CAPTAIN_ATTACK;
    public static final ForgeConfigSpec.IntValue CAPTAIN_ESCORTS;
    public static final ForgeConfigSpec.IntValue CAPTAIN_RESURRECT_COST;
    public static final ForgeConfigSpec.IntValue CAPTAIN_SCAN_RANGE;
    public static final ForgeConfigSpec.IntValue CAPTAIN_SCAN_TICKS;
    public static final ForgeConfigSpec.IntValue CAPTAIN_CHECK_TICKS;
    public static final ForgeConfigSpec.DoubleValue CAPTAIN_EXPLOSION_POWER;
    public static final ForgeConfigSpec.IntValue CAPTAIN_EXPLOSION_INTERVAL;
    public static final ForgeConfigSpec.IntValue CAPTAIN_BRIDGE_INTERVAL;
    public static final ForgeConfigSpec.IntValue CAPTAIN_SHOOT_INTERVAL;
    public static final ForgeConfigSpec.IntValue TECH_CAPTAIN_HP_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue TECH_CAPTAIN_ATK_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue TECH_GUARD_HP_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue TECH_GUARD_ATK_PER_LEVEL;

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
        THREAT_WEIGHTS = BUILDER.comment("威胁基础权重表（modid:entity=权重，未列出=1）")
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
        THREAT_ATTR_EVALUATION = BUILDER.comment("综合属性评估：威胁值=基础权重×属性因子（生命/护甲/攻击，兼容其他mod生物）")
                .define("attrEvaluation", true);
        THREAT_SPEND_RATIO = BUILDER.comment("召唤可用余额比例（0-1，其余绿宝石留给科技升级；小威胁自然少召、大威胁按预算上限满编）")
                .defineInRange("spendRatio", 0.5, 0.05, 1.0);
        THREAT_IRON_COST = BUILDER.comment("铁傀儡单只绿宝石成本")
                .defineInRange("ironCost", 3, 1, 100);
        THREAT_STONE_COST = BUILDER.comment("石傀儡单只绿宝石成本")
                .defineInRange("stoneCost", 1, 1, 100);
        THREAT_IRON_POWER = BUILDER.comment("铁傀儡战力（用于按威胁值组队）")
                .defineInRange("ironPower", 5, 1, 100);
        THREAT_STONE_POWER = BUILDER.comment("石傀儡战力")
                .defineInRange("stonePower", 2, 1, 100);
        THREAT_MAX_GOLEMS = BUILDER.comment("单次最多召唤傀儡数")
                .defineInRange("maxGolemsPerCall", 3, 1, 10);
        THREAT_IRON_THRESHOLD = BUILDER.comment("威胁值≥该值时召唤铁傀儡（否则仅石傀儡）")
                .defineInRange("ironGolemThreshold", 8, 1, 100);
        THREAT_DURATION_TICKS = BUILDER.comment("召唤傀儡存在时长（tick，默认600=30秒）")
                .defineInRange("durationTicks", 600, 100, 72000);
        THREAT_COOLDOWN_TICKS = BUILDER.comment("同一村庄触发冷却（tick，防止连续攻击刷傀儡）")
                .defineInRange("cooldownTicks", 100, 20, 12000);
        BUILDER.pop();

        BUILDER.push("village_core");
        CORE_VILLAGER_THRESHOLD = BUILDER.comment("村庄村民数（不含警卫）达到该值后，最近的钟转换为村庄核心")
                .defineInRange("villagerThreshold", 20, 5, 100);
        CORE_CONVERT_CHECK_TICKS = BUILDER.comment("核心转换检查间隔（tick）")
                .defineInRange("convertCheckTicks", 100, 20, 600);
        CORE_FORCE_LOAD_RADIUS = BUILDER.comment("核心区块强加载半径（区块数，1=3×3区块）")
                .defineInRange("forceLoadRadius", 1, 0, 8);
        CORE_DAMAGE_CHECK_TICKS = BUILDER.comment("核心损坏检测间隔（tick）")
                .defineInRange("damageCheckTicks", 100, 20, 600);
        CORE_DAMAGE_THRESHOLD_TICKS = BUILDER.comment("村庄内村民全部死亡持续该时长（tick）后核心损坏（默认1200=60秒）")
                .defineInRange("damageThresholdTicks", 1200, 100, 24000);
        CORE_REPAIR_THRESHOLD_TICKS = BUILDER.comment("村民重新出现并存活该时长（tick）后核心自动恢复")
                .defineInRange("repairThresholdTicks", 1200, 100, 24000);
        BUILDER.pop();

        BUILDER.push("tech_tree");
        TECH_CHECK_TICKS = BUILDER.comment("科技树自主升级检查间隔（tick）")
                .defineInRange("checkTicks", 200, 20, 1200);
        TECH_MAX_LEVEL = BUILDER.comment("科技等级上限")
                .defineInRange("maxLevel", 5, 1, 10);
        TECH_BEACON_EFFECT_COST_BASE = BUILDER.comment("信标效果解锁基础成本（绿宝石）")
                .defineInRange("beaconEffectCostBase", 20, 1, 1000);
        TECH_BEACON_EFFECT_COST_PER_LEVEL = BUILDER.comment("信标效果每级递增成本")
                .defineInRange("beaconEffectCostPerLevel", 10, 1, 1000);
        TECH_BEACON_RANGE_COST_BASE = BUILDER.comment("信标范围升级基础成本")
                .defineInRange("beaconRangeCostBase", 30, 1, 1000);
        TECH_BEACON_RANGE_COST_PER_LEVEL = BUILDER.comment("信标范围每级递增成本")
                .defineInRange("beaconRangeCostPerLevel", 20, 1, 1000);
        TECH_WALL_COST_BASE = BUILDER.comment("围墙建造/升级基础成本（每级×倍数）")
                .defineInRange("wallCostBase", 30, 1, 1000);
        TECH_WALL_COST_MULTIPLIER = BUILDER.comment("围墙每级成本倍数")
                .defineInRange("wallCostMultiplier", 2, 1, 10);
        TECH_CAPTAIN_COST_PER_LEVEL = BUILDER.comment("队长科技每级成本")
                .defineInRange("captainCostPerLevel", 50, 1, 1000);
        TECH_ESCORT_COST_PER_LEVEL = BUILDER.comment("护卫科技每级成本")
                .defineInRange("escortCostPerLevel", 40, 1, 1000);
        TECH_GUARD_COST_PER_LEVEL = BUILDER.comment("警卫科技每级成本")
                .defineInRange("guardCostPerLevel", 30, 1, 1000);
        BUILDER.pop();

        BUILDER.push("beacon");
        BEACON_RADIUS_BASE = BUILDER.comment("信标初始半径（格，50×50=25）")
                .defineInRange("radiusBase", 25, 5, 200);
        BEACON_RADIUS_PER_LEVEL = BUILDER.comment("信标范围每级增加半径")
                .defineInRange("radiusPerLevel", 10, 1, 100);
        BEACON_REPUTATION_THRESHOLD = BUILDER.comment("玩家获得信标效果的最低村庄声望（聚合村民好感）")
                .defineInRange("reputationThreshold", 10, 0, 1000);
        BEACON_TICK_INTERVAL = BUILDER.comment("信标光环刷新间隔（tick）")
                .defineInRange("tickInterval", 100, 20, 600);
        BUILDER.pop();

        BUILDER.push("wall");
        WALL_RADIUS = BUILDER.comment("围墙半径（格，需大于小屋网格+余量）")
                .defineInRange("radius", 48, 16, 256);
        WALL_HEIGHT = BUILDER.comment("围墙高度（格）")
                .defineInRange("height", 3, 1, 10);
        WALL_GATE_WIDTH = BUILDER.comment("围墙通道宽度（格，单数）")
                .defineInRange("gateWidth", 3, 1, 9);
        BUILDER.pop();

        BUILDER.push("captain");
        CAPTAIN_ENABLED = BUILDER.comment("警卫队长功能总开关")
                .define("enabled", true);
        CAPTAIN_HEALTH = BUILDER.comment("队长基础生命")
                .defineInRange("health", 80, 20, 500);
        CAPTAIN_ATTACK = BUILDER.comment("队长基础攻击力")
                .defineInRange("attack", 10, 1, 100);
        CAPTAIN_ESCORTS = BUILDER.comment("队长护卫数量（每次生成/复活）")
                .defineInRange("escorts", 4, 0, 8);
        CAPTAIN_RESURRECT_COST = BUILDER.comment("队长阵亡后复活消耗（绿宝石）")
                .defineInRange("resurrectCost", 100, 10, 10000);
        CAPTAIN_SCAN_RANGE = BUILDER.comment("队长威胁扫描半径（格，仅已加载区块）")
                .defineInRange("scanRange", 300, 32, 1024);
        CAPTAIN_SCAN_TICKS = BUILDER.comment("队长威胁扫描间隔（tick）")
                .defineInRange("scanTicks", 100, 20, 1200);
        CAPTAIN_CHECK_TICKS = BUILDER.comment("队长管理检查间隔（生成/复活/科技，tick）")
                .defineInRange("checkTicks", 100, 20, 1200);
        CAPTAIN_EXPLOSION_POWER = BUILDER.comment("队长开路爆炸威力（破坏方块，实体无伤）")
                .defineInRange("explosionPower", 3.0D, 1.0D, 8.0D);
        CAPTAIN_EXPLOSION_INTERVAL = BUILDER.comment("开路爆炸间隔（tick）")
                .defineInRange("explosionInterval", 60, 10, 600);
        CAPTAIN_BRIDGE_INTERVAL = BUILDER.comment("垫方块间隔（tick）")
                .defineInRange("bridgeInterval", 8, 1, 100);
        CAPTAIN_SHOOT_INTERVAL = BUILDER.comment("弓射间隔（tick）")
                .defineInRange("shootInterval", 30, 5, 200);
        TECH_CAPTAIN_HP_PER_LEVEL = BUILDER.comment("队长科技每级额外生命")
                .defineInRange("captainHpPerLevel", 10, 0, 200);
        TECH_CAPTAIN_ATK_PER_LEVEL = BUILDER.comment("队长科技每级额外攻击")
                .defineInRange("captainAtkPerLevel", 2, 0, 50);
        TECH_GUARD_HP_PER_LEVEL = BUILDER.comment("警卫科技每级额外生命（全村警卫）")
                .defineInRange("guardHpPerLevel", 4, 0, 100);
        TECH_GUARD_ATK_PER_LEVEL = BUILDER.comment("警卫科技每级额外攻击（全村警卫）")
                .defineInRange("guardAtkPerLevel", 1, 0, 20);
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
