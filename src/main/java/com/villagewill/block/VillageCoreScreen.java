package com.villagewill.block;

import com.villagewill.VillageWill;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 村庄核心信息面板（客户端，只读展示）
 * 数据由 VillageCoreMenu 的 ContainerData 服务端同步
 */
public class VillageCoreScreen extends AbstractContainerScreen<VillageCoreMenu> {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;

    public VillageCoreScreen(VillageCoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        VillageCoreMenu menu = this.menu;
        int line = 0;
        graphics.drawString(font, Component.translatable("gui." + VillageWill.MODID + ".core.title"), 8, 6 + 12 * line++, 0xFFFFFF);
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.villagers", menu.getData(0)), 8, 6 + 12 * line++, 0xE0E0E0);
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.guards", menu.getData(1)), 8, 6 + 12 * line++, 0xE0E0E0);
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.reputation", menu.getData(2)), 8, 6 + 12 * line++, 0xE0E0E0);
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.emeralds", menu.getData(3)), 8, 6 + 12 * line++, 0xE0E0E0);
        line++;
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.beacon", menu.getData(5)), 8, 6 + 12 * line++, 0xC0C0C0);
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.beacon_range", menu.getData(6)), 8, 6 + 12 * line++, 0xC0C0C0);
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.wall", menu.getData(7)), 8, 6 + 12 * line++, 0xC0C0C0);
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.captain", menu.getData(8)), 8, 6 + 12 * line++, 0xC0C0C0);
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.escort", menu.getData(9)), 8, 6 + 12 * line++, 0xC0C0C0);
        graphics.drawString(font, lineText("gui." + VillageWill.MODID + ".core.guard_tech", menu.getData(10)), 8, 6 + 12 * line++, 0xC0C0C0);
        if (menu.getData(4) == 0) {
            graphics.drawString(font, Component.translatable("gui." + VillageWill.MODID + ".core.inactive"), 8, 6 + 12 * line, 0xFF5555);
        }
        if (menu.getData(11) == 1) {
            graphics.drawString(font, Component.translatable("gui." + VillageWill.MODID + ".core.damaged"), 8, 6 + 12 * line, 0xFF3333);
        }
    }

    private Component lineText(String key, int value) {
        return Component.translatable(key, value);
    }
}
