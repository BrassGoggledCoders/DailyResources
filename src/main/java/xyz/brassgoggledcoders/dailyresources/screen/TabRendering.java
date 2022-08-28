package xyz.brassgoggledcoders.dailyresources.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TabRendering {
    private static final ResourceLocation COMPONENT_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    public static <T> void renderTabs(int leftPos, int topPos, PoseStack poseStack, List<Tab<T>> tabs, boolean active,
                                      T activeMarker, Screen screen) {
        for (int i = 0; i < tabs.size(); i++) {
            Tab<T> tab = tabs.get(i);
            if ((tab.marker() == activeMarker) == active) {
                renderTab(tab, i, active, leftPos, topPos, poseStack, screen);
            }
        }
    }

    public static <T> void renderTab(Tab<T> tab, int position, boolean active, int leftPos, int topPos, PoseStack poseStack, Screen screen) {
        int offset = position * 28;
        int textureYPos = 0;
        int xPos = leftPos + 28 * position;
        int yPos = topPos;

        if (active) {
            textureYPos += 32;
        }

        if (position > 0) {
            xPos += position;
        }

        yPos -= 28;

        RenderSystem.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.
        RenderSystem.setShaderTexture(0, COMPONENT_LOCATION);
        screen.blit(poseStack, xPos, yPos, offset, textureYPos, 28, 32);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.blitOffset = 100.0F;
        xPos += 6;
        yPos += 9;
        ItemStack itemstack = tab.icon();

        itemRenderer.renderAndDecorateItem(itemstack, xPos, yPos);
        itemRenderer.renderGuiItemDecorations(Minecraft.getInstance().font, itemstack, xPos, yPos);
        itemRenderer.blitOffset = 0.0F;
    }

    public static <T> boolean checkClicked(int topPos, int leftPos, double mouseX, double mouseY, List<Tab<T>> tabs,
                                           Predicate<T> onClicked) {
        if (mouseY <= topPos && mouseY >= topPos - 32) {
            for (int i = 0; i < tabs.size(); i++) {
                if (mouseX >= leftPos + (i * 28) && mouseX < leftPos + ((i + 1) * 28)) {
                    return onClicked.test(tabs.get(i).marker());
                }
            }
        }

        return false;
    }

    public static <T> void renderTooltips(int topPos, int leftPos, int mouseX, int mouseY, List<Tab<T>> tabs,
                                          PoseStack poseStack, Screen screen) {
        if (mouseY <= topPos - 5 && mouseY >= topPos - 27) {
            for (int i = 0; i < tabs.size(); i++) {
                if (mouseX >= leftPos + (i * 28) + 5 && mouseX < leftPos + ((i + 1) * 28) - 5) {
                    screen.renderTooltip(
                            poseStack,
                            tabs.get(i).components(),
                            Optional.empty(),
                            mouseX,
                            mouseY,
                            screen.getMinecraft().font
                    );
                }
            }
        }
    }
}
