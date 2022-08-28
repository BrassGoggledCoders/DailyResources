package xyz.brassgoggledcoders.dailyresources.resource;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public record ResourceStorageSelection<T>(
        UUID id,
        ResourceLocation resourceGroupId,
        Choice<T> choice,
        UUID chosenBy
) {

    public static final Supplier<Codec<ResourceStorageSelection<?>>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> instance.group(
            Codecs.UNIQUE_ID.fieldOf("id").forGetter(ResourceStorageSelection::id),
            ResourceLocation.CODEC.fieldOf("resourceGroupId").forGetter(ResourceStorageSelection::resourceGroupId),
            Choice.CODEC.get().fieldOf("choice").forGetter(ResourceStorageSelection::choice),
            Codecs.UNIQUE_ID.fieldOf("chosenBy").forGetter(ResourceStorageSelection::chosenBy)
    ).apply(instance, ResourceStorageSelection::new)));

    public <U> Optional<U> getChoice(ResourceType<U> resourceType) {
        return resourceType.castChoice(this.choice());
    }
}
