package xyz.brassgoggledcoders.dailyresources.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.network.NetworkHandler;
import xyz.brassgoggledcoders.dailyresources.resource.Choice;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;

import java.util.*;

public class ResourceSelectorScreen<T> extends AbstractContainerScreen<ResourceSelectorMenu<T>> {
    private static final ResourceLocation BG_LOCATION = DailyResources.rl("textures/screen/resource_selector.png");

    private float scrollOffs;

    private boolean scrolling;

    private int startIndex;

    private final int[] currentIcons;
    private int change = 40;

    public ResourceSelectorScreen(ResourceSelectorMenu<T> pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        --this.titleLabelY;
        this.currentIcons = new int[pMenu.getResourceGroups().size()];
        Arrays.fill(currentIcons, 0);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        TabRendering.renderTabs(this.leftPos, this.topPos, pPoseStack, this.menu.getTabs(), false, ResourceScreenType.ITEM_SELECTOR, this);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        TabRendering.renderTabs(this.leftPos, this.topPos, pPoseStack, this.menu.getTabs(), true, ResourceScreenType.ITEM_SELECTOR, this);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void containerTick() {
        if (this.change-- <= 0) {
            for (int i = 0; i < currentIcons.length; i++) {
                int currentIcon = currentIcons[i] + 1;
                if (currentIcon >= this.getMenu().getChoices(i).size()) {
                    currentIcon = 0;
                }
                currentIcons[i] = currentIcon;
            }
            this.change = 40;
        }
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
        this.blit(pPoseStack, i + 91, j + 15 + k, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15);
        int l = this.leftPos + 8;
        int i1 = this.topPos + 14;
        int j1 = this.startIndex + 15;
        this.renderButtons(pPoseStack, pX, pY, l, i1, j1);
        this.renderItemStacks(l, i1, j1);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack pPoseStack, int mouseX, int mouseY) {
        super.renderTooltip(pPoseStack, mouseX, mouseY);
        int i = this.leftPos + 4;
        int j = this.topPos + 14;
        int k = this.startIndex + 15;
        List<Choice<T>> list = this.menu.getChoices();

        for (int l = this.startIndex; l < k && l < this.menu.getNumChoices(); ++l) {
            int i1 = l - this.startIndex;
            int j1 = i + i1 % 5 * 16;
            int k1 = j + i1 / 5 * 18 + 2;
            if (mouseX >= j1 && mouseX < j1 + 16 && mouseY >= k1 && mouseY < k1 + 18) {
                this.renderTooltip(pPoseStack, list.get(l).asItemStack(), mouseX, mouseY);
            }
        }

        int groupLeftPos = i + 128;
        List<Pair<UUID, ResourceGroup>> resourceGroups = this.menu.getResourceGroups();
        int groupLength = Math.min(8, resourceGroups.size());
        for (int groupIndex = 0; groupIndex < groupLength; groupIndex++) {
            int j1 = groupLeftPos + groupIndex % 2 * 16;
            int k1 = j + groupIndex / 2 * 18 + 2;
            if (mouseX >= j1 && mouseX < j1 + 16 && mouseY >= k1 && mouseY < k1 + 18) {
                List<Choice<T>> choices = this.menu.getChoices(groupIndex);
                if (!choices.isEmpty()) {
                    this.renderTooltip(
                            pPoseStack,
                            Collections.singletonList(resourceGroups.get(groupIndex).getSecond().name()),
                            Optional.empty(),
                            mouseX,
                            mouseY
                    );
                }
            }
        }

        TabRendering.renderTooltips(this.topPos, this.leftPos, mouseX, mouseY, this.menu.getTabs(), pPoseStack, this);
    }

    private void renderButtons(PoseStack pPoseStack, int pMouseX, int pMouseY, int pX, int pY, int pLastVisibleElementIndex) {
        for (int i = this.startIndex; i < pLastVisibleElementIndex && i < this.menu.getNumChoices(); ++i) {
            int j = i - this.startIndex;
            int k = pX + j % 5 * 16;
            int l = j / 5;
            int i1 = pY + l * 18 + 2;
            int j1 = this.imageHeight;
            if (i == this.menu.getSelectedChoiceIndex()) {
                j1 += 18;
            } else if (pMouseX >= k && pMouseY >= i1 && pMouseX < k + 16 && pMouseY < i1 + 18) {
                j1 += 36;
            }

            this.blit(pPoseStack, k, i1 - 1, 0, j1, 16, 18);
        }

        int groupLeftPos = pX + 128;
        List<Pair<UUID, ResourceGroup>> resourceGroups = this.menu.getResourceGroups();
        int groupLength = Math.min(8, resourceGroups.size());
        for (int groupIndex = 0; groupIndex < groupLength; groupIndex++) {
            int leftPos = groupLeftPos + groupIndex % 2 * 16;
            int topPos = pY + groupIndex / 2 * 18 + 2;
            int imageTop = this.imageHeight;
            if (groupIndex == this.menu.getSelectedGroupIndex()) {
                imageTop += 18;
            } else if (pMouseX >= leftPos && pMouseY >= topPos && pMouseX < leftPos + 16 && pMouseY < topPos + 18) {
                imageTop += 36;
            }
            this.blit(pPoseStack, leftPos, topPos - 1, 0, imageTop, 16, 18);
        }
    }

    private void renderItemStacks(int pLeft, int pTop, int pIndexOffsetMax) {
        List<Choice<T>> list = this.menu.getChoices();

        for (int i = this.startIndex; i < pIndexOffsetMax && i < this.menu.getNumChoices(); ++i) {
            int j = i - this.startIndex;
            int k = pLeft + j % 5 * 16;
            int l = j / 5;
            int i1 = pTop + l * 18 + 2;
            this.itemRenderer.renderAndDecorateItem(list.get(i).asItemStack(), k, i1);
            this.itemRenderer.renderGuiItemDecorations(this.font, list.get(i).asItemStack(), k, i1, null);
        }

        int groupLeftPos = pLeft + 128;
        List<Pair<UUID, ResourceGroup>> resourceGroups = this.menu.getResourceGroups();
        int groupLength = Math.min(8, resourceGroups.size());
        for (int groupIndex = 0; groupIndex < groupLength; groupIndex++) {
            int leftPos = groupLeftPos + groupIndex % 2 * 16;
            int topPos = pTop + groupIndex / 2 * 18 + 2;
            Choice<T> groupChoice = this.menu.getChoice(groupIndex);
            List<Choice<T>> choices = this.menu.getChoices(groupIndex);
            int currentIcon = currentIcons[groupIndex];
            if (groupChoice != null) {
                this.itemRenderer.renderAndDecorateItem(groupChoice.asItemStack(), leftPos, topPos);
                this.itemRenderer.renderGuiItemDecorations(this.font, groupChoice.asItemStack(), leftPos, topPos, null);
            } else if (choices.size() > currentIcon) {
                this.itemRenderer.renderAndDecorateItem(choices.get(currentIcon).asItemStack(), leftPos, topPos);
                this.itemRenderer.renderGuiItemDecorations(this.font, choices.get(currentIcon).asItemStack(), leftPos, topPos, null);
            }
        }
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        this.scrolling = false;
        int i = this.leftPos + 8;
        int j = this.topPos + 14;
        int k = this.startIndex + 15;

        for (int l = this.startIndex; l < k; ++l) {
            int i1 = l - this.startIndex;
            double d0 = pMouseX - (double) (i + i1 % 5 * 16);
            double d1 = pMouseY - (double) (j + i1 / 5 * 18);
            if (d0 >= 0.0D && d1 >= 0.0D && d0 < 16.0D && d1 < 18.0D &&
                    this.menu.clickMenuButton(Objects.requireNonNull(this.getMinecraft().player), l)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                NetworkHandler.getInstance().sendMenuClick((this.menu).containerId, l);
                return true;
            }
        }

        int groupLeftPos = i + 128;
        List<Pair<UUID, ResourceGroup>> resourceGroups = this.menu.getResourceGroups();
        int groupLength = Math.min(8, resourceGroups.size());

        for (int groupIndex = 0; groupIndex < groupLength; groupIndex++) {
            double d0 = pMouseX - (double) (groupLeftPos + groupIndex % 2 * 16);
            double d1 = pMouseY - (double) (j + groupIndex / 2 * 18);

            if (d0 >= 0.0D && d1 >= 0.0D && d0 < 16.0D && d1 < 18.0D &&
                    this.menu.clickMenuButton(Objects.requireNonNull(this.getMinecraft().player), groupIndex + this.menu.getNumChoices())) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                NetworkHandler.getInstance().sendMenuClick((this.menu).containerId, groupIndex + this.menu.getNumChoices());
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

        if (TabRendering.checkClicked(this.topPos, this.leftPos, pMouseX, pMouseY, this.menu.getTabs(), this::tabClick)) {
            return true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public boolean tabClick(ResourceScreenType type) {
        if (type == ResourceScreenType.ITEM_STORAGE) {
            if (this.menu.clickMenuButton(Objects.requireNonNull(Minecraft.getInstance().player), this.menu.getTabId())) {
                NetworkHandler.getInstance().sendMenuClick((this.menu).containerId, this.menu.getTabId());
                return true;
            }
        }
        return false;
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + 14;
            int j = i + 54;
            this.scrollOffs = ((float) pMouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) ((double) (this.scrollOffs * (float) this.getOffscreenRows()) + 0.5D) * 5;
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
            this.startIndex = (int) ((double) (this.scrollOffs * (float) i) + 0.5D) * 5;
        }

        return true;
    }

    private boolean isScrollBarActive() {
        return this.menu.getNumChoices() > 15;
    }

    private int getOffscreenRows() {
        return (this.menu.getNumChoices() + 5 - 1) / 5 - 3;
    }
}
