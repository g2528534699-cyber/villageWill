package com.villagewill.village;

import com.villagewill.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 村庄状态（SavedData，按村庄代表点存档）
 * 阶段二：小屋记录（数量/位置/每屋床数）；阶段三扩展：绿宝石/科技/围墙
 */
public class VillageState extends SavedData {
    private static final String DATA_PREFIX = "village_will_village_";

    /** 村庄代表点（最近的钟，无钟则为首个村民位置） */
    private final BlockPos key;
    private final List<BlockPos> houseCenters = new ArrayList<>();
    /** 小屋中心 -> 已放床数 */
    private final Map<BlockPos, Integer> bedsPerHouse = new HashMap<>();
    /** 围墙方块位置（升级材质时替换） */
    private final List<BlockPos> wallPositions = new ArrayList<>();
    private long lastEmeraldDay = -1;
    /** 绿宝石余额（村庄核心支出/威胁召唤消耗） */
    private long emeraldBalance = 0;
    /** 威胁召唤冷却（上次触发 gameTime） */
    private long lastThreatTime = -1;
    /** 村庄核心是否已激活（村民≥20转换钟后置 true；威胁召唤/收入仅核心激活后生效） */
    private boolean coreActive = false;
    /** 核心是否损坏（村庄内所有村民死亡持续阈值后置 true；损坏后核心功能全部停止） */
    private boolean coreDamaged = false;
    /** 损坏进度（tick，村民数为0时累计） */
    private int damageProgress = 0;
    /** 恢复进度（tick，村民数>0时累计） */
    private int repairProgress = 0;
    /** 信标效果等级（0=再生I初始，升级解锁其他效果/等级II） */
    private int beaconEffectLevel = 0;
    /** 信标范围等级（0=50×50初始） */
    private int beaconRangeLevel = 0;
    /** 围墙等级（0=未建墙，1=圆石，2=石砖，3=深板岩砖） */
    private int wallLevel = 0;
    /** 队长科技等级（血量/攻击/附魔） */
    private int captainTechLevel = 0;
    /** 护卫科技等级（装备/附魔） */
    private int escortTechLevel = 0;
    /** 警卫科技等级（血量/攻击） */
    private int guardTechLevel = 0;
    /** 警卫队长 UUID（null=从未生成；存在但实体不在=死亡待复活） */
    private String captainUUID = null;
    /** 护卫 UUID 列表（随队长复活重建） */
    private final List<String> escortUUIDs = new ArrayList<>();
    /** 队长装备存档（死亡时保存，复活时恢复） */
    private CompoundTag captainEquipment = new CompoundTag();
    /** 护卫是否已随队长生成过（复活时补全） */
    private boolean escortsSpawned = false;
    /** 队长是否已阵亡（false=存活或从未生成/服务器重启 → 免费重生；true=阵亡 → 消耗绿宝石复活） */
    private boolean captainDead = false;
    /** 上次已应用的护卫/警卫科技等级（避免重复换装/加属性） */
    private int appliedEscortTech = -1;
    private int appliedGuardTech = -1;

    public VillageState(BlockPos key) {
        this.key = key.immutable();
        setDirty();
    }

    public static VillageState get(ServerLevel level, BlockPos key) {
        BlockPos k = key.immutable();
        return level.getDataStorage().computeIfAbsent(
                (tag) -> load(tag, k),
                () -> new VillageState(k),
                DATA_PREFIX + k.getX() + "_" + k.getZ());
    }

    private static VillageState load(CompoundTag tag, BlockPos key) {
        VillageState state = new VillageState(key);
        ListTag houses = tag.getList("Houses", Tag.TAG_COMPOUND);
        for (int i = 0; i < houses.size(); i++) {
            CompoundTag h = houses.getCompound(i);
            state.houseCenters.add(new BlockPos(h.getInt("X"), h.getInt("Y"), h.getInt("Z")));
            state.bedsPerHouse.put(state.houseCenters.get(state.houseCenters.size() - 1), h.getInt("Beds"));
        }
        ListTag walls = tag.getList("Walls", Tag.TAG_COMPOUND);
        for (int i = 0; i < walls.size(); i++) {
            CompoundTag w = walls.getCompound(i);
            state.wallPositions.add(new BlockPos(w.getInt("X"), w.getInt("Y"), w.getInt("Z")));
        }
        state.lastEmeraldDay = tag.getLong("LastEmeraldDay");
        state.emeraldBalance = tag.getLong("EmeraldBalance");
        state.lastThreatTime = tag.getLong("LastThreatTime");
        state.coreActive = tag.getBoolean("CoreActive");
        state.coreDamaged = tag.getBoolean("CoreDamaged");
        state.damageProgress = tag.getInt("DamageProgress");
        state.repairProgress = tag.getInt("RepairProgress");
        state.beaconEffectLevel = tag.getInt("BeaconEffectLevel");
        state.beaconRangeLevel = tag.getInt("BeaconRangeLevel");
        state.wallLevel = tag.getInt("WallLevel");
        state.captainTechLevel = tag.getInt("CaptainTechLevel");
        state.escortTechLevel = tag.getInt("EscortTechLevel");
        state.guardTechLevel = tag.getInt("GuardTechLevel");
        state.captainUUID = tag.contains("CaptainUUID") ? tag.getString("CaptainUUID") : null;
        state.escortUUIDs.clear();
        ListTag escorts = tag.getList("Escorts", Tag.TAG_STRING);
        for (int i = 0; i < escorts.size(); i++) {
            state.escortUUIDs.add(escorts.getString(i));
        }
        state.captainEquipment = tag.contains("CaptainEquipment")
                ? tag.getCompound("CaptainEquipment") : new CompoundTag();
        state.escortsSpawned = tag.getBoolean("EscortsSpawned");
        state.captainDead = tag.getBoolean("CaptainDead");
        state.appliedEscortTech = tag.contains("AppliedEscortTech") ? tag.getInt("AppliedEscortTech") : -1;
        state.appliedGuardTech = tag.contains("AppliedGuardTech") ? tag.getInt("AppliedGuardTech") : -1;
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag houses = new ListTag();
        for (BlockPos pos : houseCenters) {
            CompoundTag h = new CompoundTag();
            h.putInt("X", pos.getX());
            h.putInt("Y", pos.getY());
            h.putInt("Z", pos.getZ());
            h.putInt("Beds", bedsPerHouse.getOrDefault(pos, 0));
            houses.add(h);
        }
        tag.put("Houses", houses);
        ListTag walls = new ListTag();
        for (BlockPos pos : wallPositions) {
            CompoundTag w = new CompoundTag();
            w.putInt("X", pos.getX());
            w.putInt("Y", pos.getY());
            w.putInt("Z", pos.getZ());
            walls.add(w);
        }
        tag.put("Walls", walls);
        tag.putLong("LastEmeraldDay", lastEmeraldDay);
        tag.putLong("EmeraldBalance", emeraldBalance);
        tag.putLong("LastThreatTime", lastThreatTime);
        tag.putBoolean("CoreActive", coreActive);
        tag.putBoolean("CoreDamaged", coreDamaged);
        tag.putInt("DamageProgress", damageProgress);
        tag.putInt("RepairProgress", repairProgress);
        tag.putInt("BeaconEffectLevel", beaconEffectLevel);
        tag.putInt("BeaconRangeLevel", beaconRangeLevel);
        tag.putInt("WallLevel", wallLevel);
        tag.putInt("CaptainTechLevel", captainTechLevel);
        tag.putInt("EscortTechLevel", escortTechLevel);
        tag.putInt("GuardTechLevel", guardTechLevel);
        if (captainUUID != null) tag.putString("CaptainUUID", captainUUID);
        ListTag escorts = new ListTag();
        for (String s : escortUUIDs) {
            escorts.add(net.minecraft.nbt.StringTag.valueOf(s));
        }
        tag.put("Escorts", escorts);
        tag.put("CaptainEquipment", captainEquipment);
        tag.putBoolean("EscortsSpawned", escortsSpawned);
        tag.putBoolean("CaptainDead", captainDead);
        tag.putInt("AppliedEscortTech", appliedEscortTech);
        tag.putInt("AppliedGuardTech", appliedGuardTech);
        return tag;
    }

    // ---------------- 科技等级 ----------------

    public int beaconEffectLevel() {
        return beaconEffectLevel;
    }

    public void setBeaconEffectLevel(int level) {
        this.beaconEffectLevel = level;
        setDirty();
    }

    public int beaconRangeLevel() {
        return beaconRangeLevel;
    }

    public void setBeaconRangeLevel(int level) {
        this.beaconRangeLevel = level;
        setDirty();
    }

    public int wallLevel() {
        return wallLevel;
    }

    public void setWallLevel(int level) {
        this.wallLevel = level;
        setDirty();
    }

    public int captainTechLevel() {
        return captainTechLevel;
    }

    public void setCaptainTechLevel(int level) {
        this.captainTechLevel = level;
        setDirty();
    }

    public int escortTechLevel() {
        return escortTechLevel;
    }

    public void setEscortTechLevel(int level) {
        this.escortTechLevel = level;
        setDirty();
    }

    public int guardTechLevel() {
        return guardTechLevel;
    }

    public void setGuardTechLevel(int level) {
        this.guardTechLevel = level;
        setDirty();
    }

    // ---------------- 队长 / 护卫 ----------------

    @javax.annotation.Nullable
    public String captainUUID() {
        return captainUUID;
    }

    public void setCaptainUUID(java.util.UUID uuid) {
        this.captainUUID = uuid.toString();
        setDirty();
    }

    public List<String> escortUUIDs() {
        return escortUUIDs;
    }

    public void addEscortUUID(java.util.UUID uuid) {
        if (!escortUUIDs.contains(uuid.toString())) {
            escortUUIDs.add(uuid.toString());
            setDirty();
        }
    }

    public CompoundTag captainEquipment() {
        return captainEquipment;
    }

    public void setCaptainEquipment(CompoundTag tag) {
        this.captainEquipment = tag;
        setDirty();
    }

    public boolean escortsSpawned() {
        return escortsSpawned;
    }

    public void setEscortsSpawned(boolean spawned) {
        if (this.escortsSpawned != spawned) {
            this.escortsSpawned = spawned;
            setDirty();
        }
    }

    public boolean isCaptainDead() {
        return captainDead;
    }

    public void setCaptainDead(boolean dead) {
        if (this.captainDead != dead) {
            this.captainDead = dead;
            setDirty();
        }
    }

    public int appliedEscortTech() {
        return appliedEscortTech;
    }

    public void setAppliedEscortTech(int level) {
        if (this.appliedEscortTech != level) {
            this.appliedEscortTech = level;
            setDirty();
        }
    }

    public int appliedGuardTech() {
        return appliedGuardTech;
    }

    public void setAppliedGuardTech(int level) {
        if (this.appliedGuardTech != level) {
            this.appliedGuardTech = level;
            setDirty();
        }
    }

    // ---------------- 围墙 ----------------

    public List<BlockPos> wallPositions() {
        return wallPositions;
    }

    public void addWallPos(BlockPos pos) {
        wallPositions.add(pos.immutable());
        setDirty();
    }

    // ---------------- 核心激活 ----------------

    public boolean isCoreActive() {
        return coreActive;
    }

    /** 核心激活（阶段三：村庄村民≥20时钟转换为核心时调用） */
    public void setCoreActive(boolean active) {
        if (this.coreActive != active) {
            this.coreActive = active;
            setDirty();
        }
    }

    // ---------------- 核心损坏 ----------------

    public boolean isCoreDamaged() {
        return coreDamaged;
    }

    public void setCoreDamaged(boolean damaged) {
        if (this.coreDamaged != damaged) {
            this.coreDamaged = damaged;
            setDirty();
        }
    }

    public int damageProgress() {
        return damageProgress;
    }

    public void setDamageProgress(int ticks) {
        this.damageProgress = ticks;
        setDirty();
    }

    public int repairProgress() {
        return repairProgress;
    }

    public void setRepairProgress(int ticks) {
        this.repairProgress = ticks;
        setDirty();
    }

    public long lastThreatTime() {
        return lastThreatTime;
    }

    public void setLastThreatTime(long gameTime) {
        this.lastThreatTime = gameTime;
        setDirty();
    }

    // ---------------- 绿宝石 ----------------

    public long emeraldBalance() {
        return emeraldBalance;
    }

    /** 惰性结算每日收入：村庄内村民每人 1 颗（3级2、5级3），换天时入账 */
    public void settleEmeraldIncome(ServerLevel level) {
        long day = level.getDayTime() / 24000L;
        if (day != lastEmeraldDay) {
            lastEmeraldDay = day;
            long income = 0;
            for (net.minecraft.world.entity.npc.Villager v : com.villagewill.util.VillageContext.villagersInVillage(level, key)) {
                int lvl = v.getVillagerData().getLevel();
                income += (lvl >= 5) ? 3 : (lvl >= 3) ? 2 : 1;
            }
            emeraldBalance += income;
            setDirty();
        }
    }

    /** 花费绿宝石，余额不足返回 false */
    public boolean spendEmeralds(int amount) {
        if (emeraldBalance < amount) return false;
        emeraldBalance -= amount;
        setDirty();
        return true;
    }

    // ---------------- 小屋 ----------------

    public BlockPos key() {
        return key;
    }

    public int houseCount() {
        return houseCenters.size();
    }

    public boolean canBuild() {
        return houseCount() < Config.MASON_MAX_HOUSES.get();
    }

    public List<BlockPos> houseCenters() {
        return houseCenters;
    }

    public void addHouse(BlockPos center) {
        houseCenters.add(center.immutable());
        setDirty();
    }

    public int bedsInHouse(BlockPos center) {
        return bedsPerHouse.getOrDefault(center.immutable(), 0);
    }

    /** 找最近一间床未放满的小屋 */
    public BlockPos findHouseNeedingBed(BlockPos from) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : houseCenters) {
            if (bedsInHouse(pos) < Config.SHEPHERD_BEDS_PER_HOUSE.get()) {
                double d = from.distSqr(pos);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos;
                }
            }
        }
        return best;
    }

    public void addBed(BlockPos houseCenter) {
        BlockPos k = houseCenter.immutable();
        bedsPerHouse.merge(k, 1, Integer::sum);
        setDirty();
    }

    public long lastEmeraldDay() {
        return lastEmeraldDay;
    }

    public void setLastEmeraldDay(long day) {
        this.lastEmeraldDay = day;
        setDirty();
    }
}
