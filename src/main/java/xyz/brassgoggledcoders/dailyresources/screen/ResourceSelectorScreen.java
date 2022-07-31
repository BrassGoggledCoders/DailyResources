package xyz.brassgoggledcoders.dailyresources.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.resource.Resource;

import java.util.List;
import java.util.Objects;

public class ResourceSelectorScreen extends AbstractContainerScreen<ResourceSelectorMenu> {
    private static final ResourceLocation BG_LOCATION = DailyResources.rl("textures/screen/resource_selector.png");

    private float scrollOffs;

    private boolean scrolling;

    private int startIndex;

    public ResourceSelectorScreen(ResourceSelectorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        --this.titleLabelY;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new Button(
                this.leftPos + 118,
                this.topPos + 30,
                53,
                20,
                new TextComponent("Confirm"),
                pButton -> ResourceSelectorScreen.this.clickConfirm()
        ));
    }

    private void clickConfirm() {
        if (this.menu.clickMenuButton(Objects.requireNonNull(this.getMinecraft().player), -1)) {
            Objects.requireNonNull(this.getMinecraft().gameMode).handleInventoryButtonClick((this.menu).containerId, -1);
        }
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        for(Widget widget : this.renderables) {
            widget.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pX, int pY) {
        this.renderBackground(pPoseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        int k = (int) (41.0F * this.scrollOffs);
        this.blit(pPoseStack, i + 103, j + 15 + k, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15);
        int l = this.leftPos + 36;
        int i1 = this.topPos + 14;
        int j1 = this.startIndex + 12;
        this.renderButtons(pPoseStack, pX, pY, l, i1, j1);
        this.renderItemStacks(l, i1, j1);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
        int i = this.leftPos + 52;
        int j = this.topPos + 14;
        int k = this.startIndex + 12;
        List<Pair<Resource, ItemStack>> list = this.menu.getItemStacks();

        for (int l = this.startIndex; l < k && l < this.menu.getNumItemStacks(); ++l) {
            int i1 = l - this.startIndex;
            int j1 = i + i1 % 4 * 16;
            int k1 = j + i1 / 4 * 18 + 2;
            if (pX >= j1 && pX < j1 + 16 && pY >= k1 && pY < k1 + 18) {
                this.renderTooltip(pPoseStack, list.get(l).getSecond(), pX, pY);
            }
        }
    }

    private void renderButtons(PoseStack pPoseStack, int pMouseX, int pMouseY, int pX, int pY, int pLastVisibleElementIndex) {
        for (int i = this.startIndex; i < pLastVisibleElementIndex && i < this.menu.getNumItemStacks(); ++i) {
            int j = i - this.startIndex;
            int k = pX + j % 4 * 16;
            int l = j / 4;
            int i1 = pY + l * 18 + 2;
            int j1 = this.imageHeight;
            if (i == this.menu.getSelectedItemStackIndex()) {
                j1 += 18;
            } else if (pMouseX >= k && pMouseY >= i1 && pMouseX < k + 16 && pMouseY < i1 + 18) {
                j1 += 36;
            }

            this.blit(pPoseStack, k, i1 - 1, 0, j1, 16, 18);
        }

    }

    private void renderItemStacks(int pLeft, int pTop, int pIndexOffsetMax) {
        List<Pair<Resource, ItemStack>> list = this.menu.getItemStacks();

        for (int i = this.startIndex; i < pIndexOffsetMax && i < this.menu.getNumItemStacks(); ++i) {
            int j = i - this.startIndex;
            int k = pLeft + j % 4 * 16;
            int l = j / 4;
            int i1 = pTop + l * 18 + 2;
            this.itemRenderer.renderAndDecorateItem(list.get(i).getSecond(), k, i1);
            this.itemRenderer.renderGuiItemDecorations(this.font, list.get(i).getSecond(), k, i1, null);
        }

    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        this.scrolling = false;
        int i = this.leftPos + 36;
        int j = this.topPos + 14;
        int k = this.startIndex + 12;

        for (int l = this.startIndex; l < k; ++l) {
            int i1 = l - this.startIndex;
            double d0 = pMouseX - (double) (i + i1 % 4 * 16);
            double d1 = pMouseY - (double) (j + i1 / 4 * 18);
            if (d0 >= 0.0D && d1 >= 0.0D && d0 < 16.0D && d1 < 18.0D &&
                    this.menu.clickMenuButton(Objects.requireNonNull(this.getMinecraft().player), l)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                Objects.requireNonNull(this.getMinecraft().gameMode).handleInventoryButtonClick((this.menu).containerId, l);
                return true;
            }
        }

        i = this.leftPos + 119;
        j = this.topPos + 9;
        if (pMouseX >= (double) i && pMouseX < (double) (i + 12) && pMouseY >= (double) j && pMouseY < (double) (j + 54)) {
            this.scrolling = true;
        }

        for (GuiEventListener listener : this.children()) {
            listener.mouseClicked(pMouseX, pMouseY, pButton);
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + 14;
            int j = i + 54;
            this.scrollOffs = ((float) pMouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) ((double) (this.scrollOffs * (float) this.getOffscreenRows()) + 0.5D) * 4;
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.isScrollBarActive()) {
            int i = this.getOffscreenRows();
            float f = (float) pDelta / (float) i;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int) ((double) (this.scrollOffs * (float) i) + 0.5D) * 4;
        }

        return true;
    }

    private boolean isScrollBarActive() {
        return this.menu.getNumItemStacks() > 12;
    }

    private int getOffscreenRows() {
        return (this.menu.getNumItemStacks() + 4 - 1) / 4 - 3;
    }
}