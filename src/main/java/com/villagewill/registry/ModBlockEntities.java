package com.villagewill.registry;

import com.villagewill.VillageWill;
import com.villagewill.block.VillageCoreBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 方块实体注册（标准 DeferredRegister）
 */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VillageWill.MODID);

    public static final RegistryObject<BlockEntityType<VillageCoreBlockEntity>> VILLAGE_CORE =
            BLOCK_ENTITY_TYPES.register("village_core", () ->
                    BlockEntityType.Builder.of(VillageCoreBlockEntity::new,
                            com.villagewill.block.VillageCoreBlock.INSTANCE).build(null));

    private ModBlockEntities() {
    }
}
