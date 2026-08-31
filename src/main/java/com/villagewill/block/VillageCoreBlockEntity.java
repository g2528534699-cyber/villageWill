package com.villagewill.block;

import com.villagewill.village.CoreRegistry;
import com.villagewill.village.VillageState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 村庄核心方块实体：绑定所在村庄的 VillageState（按核心坐标存档）
 * 数据全部在 VillageState（SavedData），此处仅持有位置引用。
 */
public class VillageCoreBlockEntity extends BlockEntity {
    public VillageCoreBlockEntity(BlockPos pos, BlockState state) {
        super(com.villagewill.registry.ModBlockEntities.VILLAGE_CORE.get(), pos, state);
    }

    /** 核心绑定的村庄状态（核心坐标=村庄 key） */
    public VillageState villageState() {
        if (level instanceof ServerLevel serverLevel) {
            return VillageState.get(serverLevel, worldPosition);
        }
        return null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            CoreRegistry.register(level.dimension(), worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            CoreRegistry.unregister(level.dimension(), worldPosition);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
    }
}
