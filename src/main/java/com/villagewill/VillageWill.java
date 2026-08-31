package com.villagewill;

import com.mojang.logging.LogUtils;
import com.villagewill.behavior.VillageWillEvents;
import com.villagewill.entity.StoneGolem;
import com.villagewill.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VillageWill.MODID)
public class VillageWill
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "village_will";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "village_will" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "village_will" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "village_will" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);

    // 石头球物品（石傀儡投掷物，用于实体渲染与展示）
    public static final RegistryObject<Item> STONE_BALL = ITEMS.register("stone_ball", () -> new Item(new Item.Properties()));

    public VillageWill(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register entities (stone golem / thrown stone)
        ModEntities.ENTITY_TYPES.register(modEventBus);
        // Register block entities (village core)
        com.villagewill.registry.ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register menu types (village core info panel)
        com.villagewill.registry.ModMenuTypes.MENU_TYPES.register(modEventBus);
        // Register POI types (village core)
        com.villagewill.registry.ModPois.POI_TYPES.register(modEventBus);

        // 村庄核心方块（阶段三）
        BLOCKS.register("village_core", () -> com.villagewill.block.VillageCoreBlock.INSTANCE);
        ITEMS.register("village_core", () -> new net.minecraft.world.item.BlockItem(
                com.villagewill.block.VillageCoreBlock.INSTANCE, new net.minecraft.world.item.Item.Properties()));

        // Entity attributes
        modEventBus.addListener(VillageWill::registerAttributes);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        // Register the mod's behavior handlers (villager enhancement AI, guard food logic, etc.)
        MinecraftForge.EVENT_BUS.register(VillageWillEvents.class);
        // 威胁召唤（显式注册，确保 LivingHurtEvent 生效）
        MinecraftForge.EVENT_BUS.register(com.villagewill.village.ThreatResponse.class);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event)
    {
        event.put(ModEntities.STONE_GOLEM.get(), StoneGolem.createAttributes().build());
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Village Will (村庄意志) 加载完成，前置：Guard Villagers");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            // 注册村庄核心信息面板屏幕
            event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(
                    com.villagewill.registry.ModMenuTypes.VILLAGE_CORE.get(),
                    com.villagewill.block.VillageCoreScreen::new));
        }

        @SubscribeEvent
        public static void onRegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(ModEntities.STONE_GOLEM.get(), com.villagewill.entity.client.StoneGolemRenderer::new);
            event.registerEntityRenderer(ModEntities.THROWN_STONE.get(), com.villagewill.entity.client.ThrownStoneRenderer::new);
        }
    }
}
