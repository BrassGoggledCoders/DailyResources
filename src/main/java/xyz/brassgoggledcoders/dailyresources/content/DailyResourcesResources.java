package xyz.brassgoggledcoders.dailyresources.content;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;
import xyz.brassgoggledcoders.dailyresources.resource.fluid.FluidStackResource;
import xyz.brassgoggledcoders.dailyresources.resource.fluid.FluidStackResourceStorage;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResource;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceStorage;

import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class DailyResourcesResources {
    public static final Supplier<IForgeRegistry<ResourceType<?>>> REGISTRY = DailyResources.getRegistrate()
            .makeRegistry("resource", ResourceType.class, () -> new RegistryBuilder<ResourceType<?>>()
                    .setDefaultKey(DailyResources.rl("itemstack"))
            );

    public static final RegistryEntry<ResourceType<ItemStack>> ITEMSTACK = DailyResources.getRegistrate()
            .object("itemstack")
            .simple(ResourceType.class, () -> new ResourceType<>(
                    ItemStack.class,
                    ItemStackResource.CODEC,
                    ItemStackResourceStorage.CODEC.get()
            ));

    public static final RegistryEntry<ResourceType<FluidStack>> FLUIDSTACK = DailyResources.getRegistrate()
            .object("fluidstack")
            .simple(ResourceType.class, () -> new ResourceType<>(
                    FluidStack.class,
                    FluidStackResource.CODEC,
                    FluidStackResourceStorage.CODEC
            ));

    public static void setup() {

    }
}
