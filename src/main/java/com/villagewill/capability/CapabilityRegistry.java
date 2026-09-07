package com.villagewill.capability;

import com.villagewill.VillageWill;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 能力注册与挂载：村民任务记忆（石匠/牧羊人每日次数等，随实体 NBT 持久化）
 * 注：警卫强化状态（食物槽/药水箭）与职业互动已独立为「警卫村民附加」模组。
 */
@Mod.EventBusSubscriber(modid = VillageWill.MODID)
public final class CapabilityRegistry {
    public static final Capability<VillagerJobMemory> VILLAGER_JOB =
            CapabilityManager.get(new CapabilityToken<>() {});

    /** 村民任务记忆（无则 empty） */
    public static java.util.Optional<VillagerJobMemory> jobOf(Villager villager) {
        return villager.getCapability(VILLAGER_JOB).resolve();
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof Villager) {
            event.addCapability(new ResourceLocation(VillageWill.MODID, "villager_job"),
                    new SimpleProvider<>(VILLAGER_JOB, new VillagerJobMemory()));
        }
    }

    /** 通用能力 Provider（NBT 序列化） */
    public static class SimpleProvider<T> implements ICapabilitySerializable<CompoundTag> {
        private final Capability<T> capability;
        private final T instance;
        private final LazyOptional<T> lazy;

        public SimpleProvider(Capability<T> capability, T instance) {
            this.capability = capability;
            this.instance = instance;
            this.lazy = LazyOptional.of(() -> this.instance);
        }

        @Nonnull
        @Override
        public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
            return capability.orEmpty(cap, lazy);
        }

        @Override
        public CompoundTag serializeNBT() {
            if (instance instanceof VillagerJobMemory m) return m.serializeNBT();
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (instance instanceof VillagerJobMemory m) m.deserializeNBT(nbt);
        }
    }
}
