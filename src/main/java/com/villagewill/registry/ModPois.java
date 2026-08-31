package com.villagewill.registry;

import com.villagewill.VillageWill;
import com.villagewill.block.VillageCoreBlock;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/**
 * POI 注册：村庄核心（核心激活后村庄中心查找依赖此 POI）
 */
public final class ModPois {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, VillageWill.MODID);

    /** 村庄核心 POI（1 票，范围与钟一致） */
    public static final RegistryObject<PoiType> VILLAGE_CORE = POI_TYPES.register("village_core", () ->
            new PoiType(Set.of(VillageCoreBlock.INSTANCE.defaultBlockState()), 1, 1));

    private ModPois() {
    }
}
