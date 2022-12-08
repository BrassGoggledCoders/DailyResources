package xyz.brassgoggledcoders.dailyresources.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.capability.fluid.IFluidHandlerModifiable;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesBlocks;
import xyz.brassgoggledcoders.dailyresources.resource.fluid.FluidStackResourceFluidHandler;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceScreenType;
import xyz.brassgoggledcoders.dailyresources.screen.Tab;
import xyz.brassgoggledcoders.dailyresources.screen.property.IPropertyManaged;
import xyz.brassgoggledcoders.dailyresources.screen.property.Property;
import xyz.brassgoggledcoders.dailyresources.screen.property.PropertyManager;
import xyz.brassgoggledcoders.dailyresources.screen.property.PropertyTypes;

import java.util.Collections;
import java.util.List;

public class FluidResourceStorageMenu extends AbstractContainerMenu implements IPropertyManaged {
    private final IFluidHandlerModifiable fluidHandler;
    private final ContainerLevelAccess containerLevelAccess;
    private final List<Tab<ResourceScreenType>> tabs;
    private final PropertyManager propertyManager;
    private final Inventory inventory;

    public FluidResourceStorageMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory,
                                    IFluidHandlerModifiable fluidHandler, ContainerLevelAccess containerLevelAccess,
                                    List<Tab<ResourceScreenType>> tabs) {
        super(pMenuType, pContainerId);
        this.fluidHandler = fluidHandler;
        this.containerLevelAccess = containerLevelAccess;
        this.inventory = inventory;
        this.tabs = tabs;
        this.propertyManager = new PropertyManager((short) pContainerId);

        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            int finalI = i;
            Property<FluidStack> fluidStackProperty = PropertyTypes.FLUID_STACK.create(
                    () -> fluidHandler.getFluidInTank(finalI),
                    fluidStack -> fluidHandler.setFluidInTank(finalI, fluidStack)
            );
            this.propertyManager.addTrackedProperty(fluidStackProperty);
        }

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(inventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 - 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(inventory, i1, 8 + i1 * 18, 161 - 18));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(containerLevelAccess, pPlayer, DailyResourcesBlocks.TANK.get());
    }

    public List<Tab<ResourceScreenType>> getTabs() {
        return this.tabs;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        if (pId == 0 && !this.getTabs().isEmpty()) {
            this.containerLevelAccess.execute((level, blockPos) -> DailyResourcesBlocks.FLUID_STORAGE_BLOCK_ENTITY.get(level, blockPos)
                    .ifPresent(storage -> storage.openMenu(pPlayer, ResourceScreenType.FLUID_SELECTOR))
            );
            return true;
        }

        return false;
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    public IFluidHandler getFluidHandler() {
        return this.fluidHandler;
    }

    @Override
    public PropertyManager getPropertyManager() {
        return this.propertyManager;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (inventory.player instanceof ServerPlayer serverPlayer) {
            this.propertyManager.sendChanges(serverPlayer, false);
        }
    }

    @Override
    public void broadcastFullState() {
        super.broadcastFullState();
        if (inventory.player instanceof ServerPlayer serverPlayer) {
            this.propertyManager.sendChanges(serverPlayer, true);
        }
    }

    @SuppressWarnings("deprecation")
    public static FluidResourceStorageMenu create(MenuType<FluidResourceStorageMenu> menuType, int id, Inventory inventory,
                                                  @Nullable FriendlyByteBuf friendlyByteBuf) {
        return new FluidResourceStorageMenu(
                menuType,
                id,
                inventory,
                new FluidStackResourceFluidHandler(4),
                ContainerLevelAccess.NULL,
                friendlyByteBuf != null ? friendlyByteBuf.readList(listBuf -> new Tab<>(
                        listBuf.readItem(),
                        listBuf.readList(subList -> subList.readWithCodec(Codecs.COMPONENT)),
                        listBuf.readEnum(ResourceScreenType.class)
                )) : Collections.emptyList()
        );
    }
}
