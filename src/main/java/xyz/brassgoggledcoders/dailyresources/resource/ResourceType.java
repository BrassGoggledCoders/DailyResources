package xyz.brassgoggledcoders.dailyresources.resource;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistryEntry;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;

import java.util.Optional;
import java.util.function.Function;

public class ResourceType<T> extends ForgeRegistryEntry<ResourceType<?>> {
    private final Class<T> type;
    private final Codec<? extends Resource<T>> resourceCodec;
    private final Codec<? extends ResourceStorage> storageCodec;

    public ResourceType(Class<T> type, Codec<? extends Resource<T>> resourceCodec, Codec<? extends ResourceStorage> storageCodec) {
        this.type = type;
        this.resourceCodec = resourceCodec;
        this.storageCodec = storageCodec;
    }

    public Codec<? extends Resource<T>> getResourceCodec() {
        return resourceCodec;
    }

    public Codec<? extends ResourceStorage> getStorageCodec() {
        return this.storageCodec;
    }

    public Class<T> getType() {
        return type;
    }

    public Optional<T> castChoice(Choice<?> choice) {
        if (choice.getResource().getResourceType() == this) {
            if (this.getType().isInstance(choice.getObject())) {
                return Optional.of(this.getType().cast(choice.getObject()));
            }
        }
        return Optional.empty();
    }
}
