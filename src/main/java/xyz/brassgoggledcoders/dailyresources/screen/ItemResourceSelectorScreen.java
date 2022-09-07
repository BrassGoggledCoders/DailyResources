package xyz.brassgoggledcoders.dailyresources.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;

public class ItemResourceSelectorScreen extends ResourceSelectorScreen<ItemStack> {
    public ItemResourceSelectorScreen(ResourceSelectorMenu<ItemStack> pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderIconTooltip(@NotNull PoseStack pPoseStack, ItemStack object, int mouseX, int mouseY) {
        this.renderTooltip(pPoseStack, object, mouseX, mouseY);
    }

    @Override
    protected void renderButtonIcon(ItemStack object, int left, int top) {
        this.itemRenderer.renderAndDecorateItem(object, left, top);
        this.itemRenderer.renderGuiItemDecorations(this.font, object, left, top, null);
    }
}
