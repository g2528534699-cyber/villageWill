package com.villagewill.registry;

import com.villagewill.VillageWill;
import com.villagewill.block.VillageCoreMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 菜单类型注册（标准 DeferredRegister）
 */
public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, VillageWill.MODID);

    /** 村庄核心信息面板（只读） */
    public static final RegistryObject<MenuType<VillageCoreMenu>> VILLAGE_CORE =
            MENU_TYPES.register("village_core", () ->
                    IForgeMenuType.create(VillageCoreMenu::new));

    private ModMenuTypes() {
    }
}
