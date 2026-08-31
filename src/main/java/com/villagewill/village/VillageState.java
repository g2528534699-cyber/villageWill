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
    private long lastEmeraldDay = -1;
    /** 绿宝石余额（村庄核心支出/威胁召唤消耗） */
    private long emeraldBalance = 0;
    /** 威胁召唤冷却（上次触发 gameTime） */
    private long lastThreatTime = -1;

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
        state.lastEmeraldDay = tag.getLong("LastEmeraldDay");
        state.emeraldBalance = tag.getLong("EmeraldBalance");
        state.lastThreatTime = tag.getLong("LastThreatTime");
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
        tag.putLong("LastEmeraldDay", lastEmeraldDay);
        tag.putLong("EmeraldBalance", emeraldBalance);
        tag.putLong("LastThreatTime", lastThreatTime);
        return tag;
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
