package xyz.brassgoggledcoders.dailyresources.screen.fluid;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.menu.FluidResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.network.NetworkHandler;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceScreenType;
import xyz.brassgoggledcoders.dailyresources.screen.TabRendering;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class FluidResourceStorageScreen extends AbstractContainerScreen<FluidResourceStorageMenu> {
    private static final ResourceLocation BG_LOCATION = DailyResources.rl("textures/screen/fluid_storage.png");

    public FluidResourceStorageScreen(FluidResourceStorageMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        TabRendering.renderTabs(this.leftPos, this.topPos, pPoseStack, this.menu.getTabs(), false, Predicate.not(ResourceScreenType::isSelector), this);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        TabRendering.renderTabs(this.leftPos, this.topPos, pPoseStack, this.menu.getTabs(), true, Predicate.not(ResourceScreenType::isSelector), this);
        this.renderTanks(pPoseStack);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    private void renderTanks(@NotNull PoseStack pPoseStack) {
        IFluidHandler fluidHandler = this.menu.getFluidHandler();
        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack fluidStack = fluidHandler.getFluidInTank(i);
            int x = leftPos + 26 + (i * 36);
            int y = topPos + 18;
            int capacity = this.menu.getFluidHandler().getTankCapacity(i);
            int height = 52;
            if (!fluidStack.isEmpty()) {
                int stored = fluidStack.getAmount();
                if (stored > capacity) {
                    stored = capacity;
                }
                int offset = stored * height / capacity;

                FluidType fluidType = fluidStack.getFluid()
                        .getFluidType();
                IClientFluidTypeExtensions clientFluidTypeExtensions = IClientFluidTypeExtensions.of(fluidType);
                ResourceLocation flowing = clientFluidTypeExtensions.getStillTexture(fluidStack);
                if (flowing != null) {
                    TextureAtlasSprite flowingSprite = Minecraft.getInstance()
                            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                            .apply(flowing);
                    RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

                    Color color = new Color(clientFluidTypeExtensions.getTintColor(fluidStack));

                    RenderSystem.setShaderColor(
                            (float) color.getRed() / 255.0F,
                            (float) color.getGreen() / 255.0F,
                            (float) color.getBlue() / 255.0F,
                            (float) color.getAlpha() / 255.0F
                    );
                    RenderSystem.enableBlend();
                    int startY = y + (fluidType.isAir() ? 0 : height - offset);
                    blit(pPoseStack, x, startY, 0, 16, offset, flowingSprite);
                    RenderSystem.disableBlend();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }

            RenderSystem.setShaderTexture(0, BG_LOCATION);
            this.blit(pPoseStack, x, y, 176, 0, 16, 52);
        }
    }

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pX, int pY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
        TabRendering.renderTooltips(this.topPos, this.leftPos, pX, pY, this.menu.getTabs(), pPoseStack, this);

        for (int i = 0; i < this.menu.getFluidHandler().getTanks(); i++) {
            int x = leftPos + 26 + (i * 36);
            int y = topPos + 18;
            if (pX > x && pX < x + 18 && pY > y && pY < y + 54) {
                FluidStack fluidStack = this.menu.getFluidHandler().getFluidInTank(i);
                if (!fluidStack.isEmpty()) {
                    List<Component> components = new ArrayList<>();
                    components.add(fluidStack.getDisplayName());
                    components.add(Component.literal(fluidStack.getAmount() + "mb"));
                    renderTooltip(
                            pPoseStack,
                            components,
                            Optional.empty(),
                            pX,
                            pY,
                            this.getMinecraft().font
                    );
                }

            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (TabRendering.checkClicked(this.topPos, this.leftPos, pMouseX, pMouseY, this.menu.getTabs(), this::tabClicked)) {
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private boolean tabClicked(ResourceScreenType resourceScreenType) {
        if (resourceScreenType.isSelector()) {
            if (this.menu.clickMenuButton(Objects.requireNonNull(Minecraft.getInstance().player), 0)) {
                NetworkHandler.getInstance().sendMenuClick((this.menu).containerId, 0);
                return true;
            }
        }
        return false;
    }
}
