package xyz.brassgoggledcoders.dailyresources.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceStorageMenu;

public class ResourceStorageScreen extends AbstractContainerScreen<ResourceStorageMenu> {
    public ResourceStorageScreen(ResourceStorageMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {

    }
}
