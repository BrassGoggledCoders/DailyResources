package xyz.brassgoggledcoders.dailyresources.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import xyz.brassgoggledcoders.dailyresources.capability.ItemHandlerWrapper;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;
import xyz.brassgoggledcoders.dailyresources.capability.fluid.FluidHandlerWrapper;
import xyz.brassgoggledcoders.dailyresources.capability.fluid.IFluidHandlerModifiable;
import xyz.brassgoggledcoders.dailyresources.resource.Choice;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.resource.fluid.FluidStackResourceFluidHandler;
import xyz.brassgoggledcoders.dailyresources.resource.fluid.FluidStackResourceStorage;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceScreenType;

import java.util.UUID;

public class FluidResourceStorageBlockEntity extends ResourceStorageBlockEntity<FluidStack> {
    private LazyOptional<IFluidHandler> wrapper;

    public FluidResourceStorageBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.wrapper = LazyOptional.of(() -> new FluidHandlerWrapper(this::getStorageFluidHandler, 4, 16 * FluidAttributes.BUCKET_VOLUME));
    }

    @Override
    protected ResourceScreenType getDefaultScreenType(boolean hasChoices) {
        return hasChoices ? ResourceScreenType.FLUID_SELECTOR : ResourceScreenType.FLUID_STORAGE;
    }

    @Override
    protected ResourceStorage createDefaultResourceStorage() {
        return new FluidStackResourceStorage(new FluidStackResourceFluidHandler(4));
    }

    public int calculateComparator() {
        return this.getHandler().getSignal();
    }

    public IFluidHandlerModifiable getHandler() {
        return this.wrapper.resolve()
                .map(wrapper -> wrapper instanceof IFluidHandlerModifiable fluidHandlerModifiable ? fluidHandlerModifiable : null)
                .orElseThrow(() -> new IllegalStateException("Found No Wrapper"));
    }

    protected void refreshStorageFluidHandler(LazyOptional<IFluidHandler> lazyOptional) {
        wrapper.invalidate();
        wrapper = LazyOptional.of(() -> new FluidHandlerWrapper(this::getStorageFluidHandler, 4, 16 * FluidAttributes.BUCKET_VOLUME));
    }

    private LazyOptional<IFluidHandler> getStorageFluidHandler() {
        LazyOptional<IFluidHandler> handlerLazyOptional = this.getResourceStorageStorage()
                .<LazyOptional<IFluidHandler>>map(storageStorage -> {
                    ResourceStorage resourceStorage = storageStorage.getResourceStorage(this.getUniqueId());
                    if (resourceStorage != null) {
                        return resourceStorage.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    } else {
                        return LazyOptional.empty();
                    }
                })
                .orElse(LazyOptional.empty());
        handlerLazyOptional.addListener(this::refreshStorageFluidHandler);
        return handlerLazyOptional;
    }

    @Override
    public boolean onConfirmed(UUID id, ResourceGroup resourceGroup, Choice<FluidStack> choice, UUID owner) {
        this.wrapper.invalidate();
        wrapper = LazyOptional.of(() -> new FluidHandlerWrapper(this::getStorageFluidHandler, 4, 16 * FluidAttributes.BUCKET_VOLUME));
        return super.onConfirmed(id, resourceGroup, choice, owner);
    }
}
