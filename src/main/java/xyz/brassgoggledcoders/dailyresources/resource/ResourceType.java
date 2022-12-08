package xyz.brassgoggledcoders.dailyresources.resource;

import com.mojang.serialization.Codec;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;

import java.util.Optional;

public record ResourceType<T>(
        Class<T> type,
        Codec<? extends Resource<T>> resourceCodec,
        Codec<? extends ResourceStorage> storageCodec
) {

    public Optional<T> castChoice(Choice<?> choice) {
        if (choice.getResource().getResourceType() == this) {
            if (this.type().isInstance(choice.getObject())) {
                return Optional.of(this.type().cast(choice.getObject()));
            }
        }
        return Optional.empty();
    }
}
