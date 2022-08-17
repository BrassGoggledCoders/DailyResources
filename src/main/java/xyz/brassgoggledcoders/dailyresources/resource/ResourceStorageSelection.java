package xyz.brassgoggledcoders.dailyresources.resource;

import net.minecraft.resources.ResourceLocation;
import xyz.brassgoggledcoders.dailyresources.menu.Choice;

import java.util.Optional;
import java.util.UUID;

public record ResourceStorageSelection<T>(
        UUID id,
        ResourceLocation resourceGroupId,
        Choice<T> choice,
        UUID chosenBy
) {

    public <U> Optional<U> getChoice(ResourceType<U> resourceType) {
        return resourceType.castChoice(this.choice());
    }
}
