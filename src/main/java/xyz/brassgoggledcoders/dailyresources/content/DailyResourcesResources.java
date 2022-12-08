package xyz.brassgoggledcoders.dailyresources.content;

import com.google.common.base.Suppliers;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;
import xyz.brassgoggledcoders.dailyresources.resource.fluid.FluidStackResource;
import xyz.brassgoggledcoders.dailyresources.resource.fluid.FluidStackResourceStorage;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResource;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceStorage;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class DailyResourcesResources {
    public static final ResourceKey<Registry<ResourceType<?>>> REGISTRY_KEY = DailyResources.getRegistrate()
            .makeRegistry("resource", () -> new RegistryBuilder<ResourceType<?>>()
                    .setDefaultKey(DailyResources.rl("itemstack"))
            );

    public static final Supplier<IForgeRegistry<ResourceType<?>>> REGISTRY = Suppliers.memoize(() -> RegistryManager.ACTIVE.getRegistry(REGISTRY_KEY));

    public static final RegistryEntry<ResourceType<ItemStack>> ITEMSTACK = DailyResources.getRegistrate()
            .object("itemstack")
            .simple(REGISTRY_KEY, () -> new ResourceType<>(
                    ItemStack.class,
                    ItemStackResource.CODEC,
                    ItemStackResourceStorage.CODEC.get()
            ));

    public static final RegistryEntry<ResourceType<FluidStack>> FLUIDSTACK = DailyResources.getRegistrate()
            .object("fluidstack")
            .simple(REGISTRY_KEY, () -> new ResourceType<>(
                    FluidStack.class,
                    FluidStackResource.CODEC,
                    FluidStackResourceStorage.CODEC
            ));

    public static IForgeRegistry<ResourceType<?>> getRegistry() {
        return REGISTRY.get();
    }

    public static void setup() {

    }
}
