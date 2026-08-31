package com.villagewill.capability;

import com.villagewill.VillageWill;
import com.villagewill.compat.GuardCompat;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 能力注册与挂载：村民任务记忆 / 警卫强化状态
 * 全部通过 Forge Capability 系统（随实体 NBT 持久化），不新建数据存储方案。
 */
@Mod.EventBusSubscriber(modid = VillageWill.MODID)
public final class CapabilityRegistry {
    public static final Capability<VillagerJobMemory> VILLAGER_JOB =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<GuardBuffState> GUARD_STATE =
            CapabilityManager.get(new CapabilityToken<>() {});

    /** 村民任务记忆（无则 empty） */
    public static java.util.Optional<VillagerJobMemory> jobOf(Villager villager) {
        return villager.getCapability(VILLAGER_JOB).resolve();
    }

    /** 警卫强化状态（无则 empty） */
    public static java.util.Optional<GuardBuffState> guardStateOf(Entity entity) {
        return entity.getCapability(GUARD_STATE).resolve();
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof Villager) {
            event.addCapability(new ResourceLocation(VillageWill.MODID, "villager_job"),
                    new SimpleProvider<>(VILLAGER_JOB, new VillagerJobMemory()));
        }
        if (GuardCompat.isGuard(entity)) {
            event.addCapability(new ResourceLocation(VillageWill.MODID, "guard_state"),
                    new SimpleProvider<>(GUARD_STATE, new GuardBuffState()));
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
            if (instance instanceof GuardBuffState s) return s.serializeNBT();
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (instance instanceof VillagerJobMemory m) m.deserializeNBT(nbt);
            if (instance instanceof GuardBuffState s) s.deserializeNBT(nbt);
        }
    }
}
