package com.villagewill.registry;

import com.villagewill.VillageWill;
import com.villagewill.entity.GuardCaptain;
import com.villagewill.entity.StoneGolem;
import com.villagewill.entity.ThrownStone;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 实体注册（标准 DeferredRegister，无自定义注册框架）
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, VillageWill.MODID);

    /** 石傀儡（雪傀儡形态，投掷石头球） */
    public static final RegistryObject<EntityType<StoneGolem>> STONE_GOLEM =
            ENTITY_TYPES.register("stone_golem", () -> EntityType.Builder
                    .of(StoneGolem::new, MobCategory.MISC)
                    .sized(0.7F, 2.4F)
                    .clientTrackingRange(10)
                    .build("stone_golem"));

    /** 石头球（投掷物，命中附带伤害+缓慢） */
    public static final RegistryObject<EntityType<ThrownStone>> THROWN_STONE =
            ENTITY_TYPES.register("thrown_stone", () -> EntityType.Builder
                    .<ThrownStone>of(ThrownStone::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("thrown_stone"));

    /** 警卫队长（阶段三 Part 3：村庄核心护卫，独立战斗 AI） */
    public static final RegistryObject<EntityType<GuardCaptain>> GUARD_CAPTAIN =
            ENTITY_TYPES.register("guard_captain", () -> EntityType.Builder
                    .<GuardCaptain>of(GuardCaptain::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("guard_captain"));

    private ModEntities() {
    }
}
