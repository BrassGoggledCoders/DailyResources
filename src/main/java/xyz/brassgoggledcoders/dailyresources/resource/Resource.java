package xyz.brassgoggledcoders.dailyresources.resource;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.codec.OptionalTypeKeyDispatchCodec;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;

import java.util.Collection;
import java.util.function.Supplier;

public interface Resource {
    Supplier<Codec<Resource>> CODEC = Suppliers.memoize(() -> new OptionalTypeKeyDispatchCodec<ResourceType, Resource>(
            DailyResourcesResources.ITEMSTACK.getId().toString(),
            "type",
            DailyResourcesResources.REGISTRY.get().getCodec(),
            Resource::getResourceType,
            ResourceType::getCodec
    ).codec());

    @NotNull
    ResourceType getResourceType();

    @NotNull
    Collection<ItemStack> asChoices();

    boolean choose(ItemStack itemStack);

    void addToStorage(@NotNull ICapabilityProvider capabilityProvider);
}
