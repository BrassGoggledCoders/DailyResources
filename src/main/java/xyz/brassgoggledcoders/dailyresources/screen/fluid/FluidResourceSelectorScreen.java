package xyz.brassgoggledcoders.dailyresources.screen.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceSelectorScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FluidResourceSelectorScreen extends ResourceSelectorScreen<FluidStack> {
    public FluidResourceSelectorScreen(ResourceSelectorMenu<FluidStack> pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderIconTooltip(@NotNull PoseStack pPoseStack, FluidStack object, int mouseX, int mouseY) {
        List<Component> components = new ArrayList<>();
        components.add(object.getDisplayName());
        components.add(Component.literal(object.getAmount() + "mb"));
        this.renderTooltip(pPoseStack, components, Optional.empty(), mouseX, mouseY);
    }

    @Override
    protected void renderButtonIcon(FluidStack object, int left, int top) {
        ItemStack itemStack = new ItemStack(object.getFluid().getBucket());
        this.itemRenderer.renderAndDecorateItem(itemStack, left, top);
        this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, left, top, null);
    }
}
