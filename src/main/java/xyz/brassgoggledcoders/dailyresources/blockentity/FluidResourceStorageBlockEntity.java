package xyz.brassgoggledcoders.dailyresources.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;
import xyz.brassgoggledcoders.dailyresources.capability.fluid.FluidHandlerWrapper;
import xyz.brassgoggledcoders.dailyresources.capability.fluid.IFluidHandlerModifiable;
import xyz.brassgoggledcoders.dailyresources.resource.Choice;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.resource.fluid.FluidStackResourceFluidHandler;
import xyz.brassgoggledcoders.dailyresources.resource.fluid.FluidStackResourceStorage;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceScreenType;

import java.util.Arrays;
import java.util.UUID;

public class FluidResourceStorageBlockEntity extends ResourceStorageBlockEntity<FluidStack> {
    public static final ModelProperty<FluidStack[]> TANK_FLUIDS_PROPERTY = new ModelProperty<>();

    private LazyOptional<IFluidHandler> wrapper;
    private FluidStack[] tankFluids = null;

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

    @Override
    protected boolean checkChangeSize() {
        if (this.tankFluids == null || this.tankFluids.length != this.getHandler().getTanks()) {
            this.tankFluids = new FluidStack[this.getHandler().getTanks()];
            Arrays.fill(this.tankFluids, FluidStack.EMPTY);
            for (int i = 0; i < this.tankFluids.length; i++) {
                FluidStack fluidStack = this.getHandler().getFluidInTank(i).copy();
                fluidStack.setAmount((int) Math.ceil((fluidStack.getAmount() / (float) this.getHandler().getTankCapacity(i)) * 100));
                this.tankFluids[i] = fluidStack;
            }
            return true;
        } else {
            boolean different = false;
            for (int i = 0; i < this.tankFluids.length; i++) {
                FluidStack prior = this.tankFluids[i];
                FluidStack current = this.getHandler().getFluidInTank(i);
                if (!prior.isFluidEqual(current)) {
                    FluidStack fluidStack = current.copy();
                    fluidStack.setAmount((int) Math.ceil((current.getAmount() / (float) this.getHandler().getTankCapacity(i)) * 100));
                    this.tankFluids[i] = fluidStack;
                    different = true;
                }
                if (Math.abs(prior.getAmount() - current.getAmount()) > FluidAttributes.BUCKET_VOLUME) {
                    different = true;
                    this.tankFluids[i].setAmount((int) Math.ceil((current.getAmount() / (float) this.getHandler().getTankCapacity(i)) * 100));
                }
            }
            return different;
        }
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
                .map(storageStorage -> storageStorage.getCapability(this.getUniqueId(), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY))
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

    @Override
    protected boolean sendClientUpdate() {
        return true;
    }

    @Override
    @NotNull
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = super.getUpdateTag();
        ListTag tanks = new ListTag();
        if (this.tankFluids == null) {
            checkChangeSize();
        }
        for (FluidStack tankFluid : this.tankFluids) {
            tanks.add(tankFluid.writeToNBT(new CompoundTag()));
        }
        compoundTag.put("tanks", tanks);
        return compoundTag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("tanks")) {
            ListTag tanks = tag.getList("tanks", Tag.TAG_COMPOUND);
            this.tankFluids = new FluidStack[tanks.size()];
            for (int i = 0; i < tanks.size(); i++) {
                this.tankFluids[i] = FluidStack.loadFluidStackFromNBT(tanks.getCompound(i));
            }

            requestModelDataUpdate();
            if (level != null) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    @NotNull
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(ResourceStorageBlockEntity.TRIGGER_PROPERTY, this.getTrigger())
                .withInitial(TANK_FLUIDS_PROPERTY, tankFluids)
                .build();
    }

    public FluidStack[] getTankFluids() {
        return this.tankFluids;
    }
}
