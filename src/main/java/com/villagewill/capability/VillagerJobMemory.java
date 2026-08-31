package com.villagewill.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 村民任务记忆（挂在 Villager 上，随实体 NBT 持久化）
 * - 每日动作次数（按职业动作名计数）
 * - 牧羊犬 UUID（阶段二使用）
 * - 职业快照（检测失业/转职，阶段二使用）
 */
public class VillagerJobMemory {
    /** 上次结算的游戏日（level.getDayTime() / 24000） */
    public long lastDay = -1;
    /** 职业动作名 -> 今日剩余次数 */
    public final Map<String, Integer> usesLeft = new HashMap<>();
    /** 牧羊人驯服的狗 */
    public final List<UUID> dogs = new ArrayList<>();
    /** 上次记录职业 */
    public String professionSnapshot = "";

    /** 换天时重置每日次数，返回是否是新的一天 */
    public boolean advanceDay(long day) {
        if (day != lastDay) {
            lastDay = day;
            usesLeft.clear();
            return true;
        }
        return false;
    }

    public int getUses(String action) {
        return usesLeft.getOrDefault(action, 0);
    }

    public void setUses(String action, int count) {
        usesLeft.put(action, count);
    }

    public void consumeUse(String action) {
        usesLeft.put(action, Math.max(0, getUses(action) - 1));
    }

    /** 检测村民职业是否发生变更（用于牧羊人失业收回狗），返回是否变更 */
    public boolean checkProfessionChange(Villager villager) {
        VillagerProfession now = villager.getVillagerData().getProfession();
        String id = now.toString();
        if (!id.equals(professionSnapshot)) {
            professionSnapshot = id;
            return true;
        }
        return false;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("LastDay", lastDay);
        CompoundTag uses = new CompoundTag();
        for (Map.Entry<String, Integer> e : usesLeft.entrySet()) {
            uses.putInt(e.getKey(), e.getValue());
        }
        tag.put("UsesLeft", uses);
        ListTag dogList = new ListTag();
        for (UUID uuid : dogs) {
            CompoundTag d = new CompoundTag();
            d.putUUID("UUID", uuid);
            dogList.add(d);
        }
        tag.put("Dogs", dogList);
        tag.putString("ProfessionSnapshot", professionSnapshot);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        lastDay = tag.getLong("LastDay");
        usesLeft.clear();
        if (tag.contains("UsesLeft", Tag.TAG_COMPOUND)) {
            CompoundTag uses = tag.getCompound("UsesLeft");
            for (String key : uses.getAllKeys()) {
                usesLeft.put(key, uses.getInt(key));
            }
        }
        dogs.clear();
        if (tag.contains("Dogs", Tag.TAG_LIST)) {
            ListTag dogList = tag.getList("Dogs", Tag.TAG_COMPOUND);
            for (int i = 0; i < dogList.size(); i++) {
                dogs.add(dogList.getCompound(i).getUUID("UUID"));
            }
        }
        professionSnapshot = tag.getString("ProfessionSnapshot");
    }
}
