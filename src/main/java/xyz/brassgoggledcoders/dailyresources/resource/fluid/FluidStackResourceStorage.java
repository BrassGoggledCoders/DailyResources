package xyz.brassgoggledcoders.dailyresources.resource.fluid;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageSelection;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

public class FluidStackResourceStorage extends ResourceStorage {
    @Override
    public ResourceType<?> getResourceType() {
        return DailyResourcesResources.FLUIDSTACK.get();
    }

    @Override
    public void invalidateCapabilities() {

    }

    @Override
    public void trigger(ResourceStorageSelection<?> resourceStorageSelection) {

    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }
}
