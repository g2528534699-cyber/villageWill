package com.villagewill.block;

import com.villagewill.util.VillageContext;
import com.villagewill.village.VillageState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

/**
 * 村庄核心信息面板（只读）
 * 服务端每 tick 刷新数据（ContainerData 自动同步到客户端屏幕）
 * 数据索引：0村民数 1警卫数 2玩家声望 3绿宝石余额 4核心激活
 *          5信标效果等级 6信标范围等级 7围墙等级 8队长科技 9护卫科技 10警卫科技
 */
public class VillageCoreMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 11;

    private final ContainerData data;
    private final BlockPos corePos;
    private final Player player;

    /** 客户端：从网络缓冲读取核心位置 */
    public VillageCoreMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, new SimpleContainerData(DATA_COUNT), buf.readBlockPos());
    }

    /** 服务端：从方块实体构造 */
    public VillageCoreMenu(int id, Inventory playerInventory, VillageCoreBlockEntity core) {
        this(id, playerInventory, new SimpleContainerData(DATA_COUNT), core.getBlockPos());
    }

    private VillageCoreMenu(int id, Inventory playerInventory, ContainerData data, BlockPos corePos) {
        super(com.villagewill.registry.ModMenuTypes.VILLAGE_CORE.get(), id);
        this.data = data;
        this.corePos = corePos;
        this.player = playerInventory.player;
        addDataSlots(data);
    }

    @Override
    public void broadcastChanges() {
        if (player != null && player.level() instanceof ServerLevel serverLevel) {
            VillageState state = VillageState.get(serverLevel, corePos);
            data.set(0, VillageContext.villagerCount(serverLevel, state.key()));
            data.set(1, VillageContext.guardsInVillage(serverLevel, state.key()).size());
            data.set(2, playerReputation(serverLevel, state));
            data.set(3, (int) Math.min(Integer.MAX_VALUE, state.emeraldBalance()));
            data.set(4, state.isCoreActive() ? 1 : 0);
            data.set(5, state.beaconEffectLevel());
            data.set(6, state.beaconRangeLevel());
            data.set(7, state.wallLevel());
            data.set(8, state.captainTechLevel());
            data.set(9, state.escortTechLevel());
            data.set(10, state.guardTechLevel());
        }
        super.broadcastChanges();
    }

    private int playerReputation(ServerLevel level, VillageState state) {
        int rep = 0;
        for (net.minecraft.world.entity.npc.Villager v : VillageContext.villagersInVillage(level, state.key())) {
            rep += v.getPlayerReputation(player);
        }
        return rep;
    }

    public int getData(int index) {
        return data.get(index);
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY; // 只读面板
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
