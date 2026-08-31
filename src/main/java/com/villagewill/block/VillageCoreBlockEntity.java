package com.villagewill.block;

import com.villagewill.Config;
import com.villagewill.util.VillageContext;
import com.villagewill.village.BeaconAura;
import com.villagewill.village.CoreRegistry;
import com.villagewill.village.TechTree;
import com.villagewill.village.VillageState;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 村庄核心方块实体：绑定所在村庄的 VillageState（按核心坐标存档）
 * - ticker：核心损坏检测（村庄内所有村民死亡持续阈值 → 损坏；村民恢复 → 自动修复）
 * - 数据全部在 VillageState（SavedData），此处仅持有位置引用
 */
public class VillageCoreBlockEntity extends BlockEntity {
    private int tickCounter;

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

    public static void tick(Level level, BlockPos pos, BlockState state, VillageCoreBlockEntity be) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        VillageState vs = VillageState.get(serverLevel, pos);
        if (!vs.isCoreActive()) return; // 未激活不检测

        // 绿宝石每日收入结算（核心激活期间持续入账）
        vs.settleEmeraldIncome(serverLevel);

        // 损坏检测/恢复（节流）
        if (++be.tickCounter % Config.CORE_DAMAGE_CHECK_TICKS.get() != 0) return;

        int interval = Config.CORE_DAMAGE_CHECK_TICKS.get();
        int villagers = VillageContext.villagerCount(serverLevel, vs.key());
        boolean damaged = vs.isCoreDamaged();

        if (villagers <= 0) {
            // 村庄无人 → 累计损坏进度
            vs.setRepairProgress(0);
            vs.setDamageProgress(vs.damageProgress() + interval);
            if (!damaged && vs.damageProgress() >= Config.CORE_DAMAGE_THRESHOLD_TICKS.get()) {
                vs.setCoreDamaged(true);
                vs.setDamageProgress(0);
                level.setBlock(pos, state.setValue(VillageCoreBlock.DAMAGED, true), 3);
                LogUtils.getLogger().warn("[VW] 村庄核心损坏: {}（村庄内所有村民死亡）", pos);
            }
            return;
        } else {
            // 有人 → 累计恢复进度
            vs.setDamageProgress(0);
            vs.setRepairProgress(vs.repairProgress() + interval);
            if (damaged && vs.repairProgress() >= Config.CORE_REPAIR_THRESHOLD_TICKS.get()) {
                vs.setCoreDamaged(false);
                vs.setRepairProgress(0);
                level.setBlock(pos, state.setValue(VillageCoreBlock.DAMAGED, false), 3);
                LogUtils.getLogger().info("[VW] 村庄核心恢复: {}（村民重新出现）", pos);
            }
        }

        // 信标光环（节流）
        if (be.tickCounter % Config.BEACON_TICK_INTERVAL.get() == 0) {
            BeaconAura.tick(serverLevel, pos, vs);
        }
        // 科技树自主升级（节流）
        if (be.tickCounter % Config.TECH_CHECK_TICKS.get() == 0) {
            TechTree.tick(serverLevel, pos, vs);
        }
        // 警卫队长管理（生成/复活/派遣/科技消费，节流）
        if (Config.CAPTAIN_ENABLED.get()
                && be.tickCounter % Config.CAPTAIN_CHECK_TICKS.get() == 0) {
            com.villagewill.village.CaptainManager.tick(serverLevel, pos, vs);
        }
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
        tag.putInt("TickCounter", tickCounter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tickCounter = tag.getInt("TickCounter");
    }
}
