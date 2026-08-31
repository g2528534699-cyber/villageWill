package com.villagewill.behavior.actions;

import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.HashMap;
import java.util.Map;

/**
 * 职业动作注册表：村民职业 → 强化动作
 * 牧师（CLERIC）特殊处理（无警卫目标，走村庄中心），不在表中。
 */
public final class ProfessionActions {
    private static final Map<VillagerProfession, ProfessionAction> ACTIONS = new HashMap<>();

    static {
        ACTIONS.put(VillagerProfession.ARMORER, new ArmorerAction());
        ACTIONS.put(VillagerProfession.FARMER, new FarmerAction());
        ACTIONS.put(VillagerProfession.WEAPONSMITH, new WeaponsmithAction());
        ACTIONS.put(VillagerProfession.LIBRARIAN, new LibrarianAction());
        ACTIONS.put(VillagerProfession.FLETCHER, new FletcherAction());
        ACTIONS.put(VillagerProfession.BUTCHER, new ButcherAction());
    }

    private ProfessionActions() {
    }

    public static ProfessionAction forProfession(VillagerProfession profession) {
        return ACTIONS.get(profession);
    }
}
