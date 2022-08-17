package xyz.brassgoggledcoders.dailyresources.resource;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.codec.OptionalTypeKeyDispatchCodec;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.menu.Choice;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface Resource<T> {
    Supplier<Codec<Resource<?>>> CODEC = Suppliers.memoize(() -> new OptionalTypeKeyDispatchCodec<ResourceType<?>, Resource<?>>(
            DailyResourcesResources.ITEMSTACK.getId().toString(),
            "type",
            DailyResourcesResources.REGISTRY.get().getCodec(),
            Resource::getResourceType,
            ResourceType::getResourceCodec
    ).codec());

    @NotNull
    ResourceType<T> getResourceType();

    @NotNull
    Collection<Choice<T>> asChoices();

    boolean contains(T object);

    void addToStorage(@NotNull T choice, @NotNull ICapabilityProvider capabilityProvider);


    <U> Optional<Resource<U>> cast(ResourceType<U> resourceType);

}
