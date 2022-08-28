package xyz.brassgoggledcoders.dailyresources.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceStorageMenu;

import java.util.Objects;

public class ResourceStorageScreen extends AbstractContainerScreen<ResourceStorageMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");

    private final int containerRows;

    public ResourceStorageScreen(ResourceStorageMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.passEvents = false;
        this.containerRows = pMenu.getItemHandler().getSlots() / 9;
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        TabRendering.renderTabs(this.leftPos, this.topPos, pPoseStack, this.menu.getTabs(), false, ResourceScreenType.ITEM_STORAGE, this);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        TabRendering.renderTabs(this.leftPos, this.topPos, pPoseStack, this.menu.getTabs(), true, ResourceScreenType.ITEM_STORAGE, this);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pX, int pY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        this.blit(pPoseStack, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
        TabRendering.renderTooltips(this.topPos, this.leftPos, pX, pY, this.menu.getTabs(), pPoseStack, this);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (TabRendering.checkClicked(this.topPos, this.leftPos, pMouseX, pMouseY, this.menu.getTabs(), this::tabClicked)) {
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private boolean tabClicked(ResourceScreenType resourceScreenType) {
        if (resourceScreenType == ResourceScreenType.ITEM_SELECTOR) {
            if (this.menu.clickMenuButton(Objects.requireNonNull(Minecraft.getInstance().player), 0)) {
                Objects.requireNonNull(this.getMinecraft().gameMode).handleInventoryButtonClick((this.menu).containerId, 0);
                return true;
            }
        }
        return false;
    }
}
