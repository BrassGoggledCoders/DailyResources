package xyz.brassgoggledcoders.dailyresources.resource;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class Choice<T> {
    public static final Supplier<Codec<Choice<?>>> CODEC = Suppliers.memoize(() -> Resource.RESOURCE_CODEC.get()
            .dispatch(
                    Choice::getResource,
                    Resource::getChoiceCodec
            )
    );

    private final Resource<T> resource;
    private final T object;
    private final Supplier<ItemStack> asItemStack;

    public Choice(Resource<T> resource, T object) {
        this.resource = resource;
        this.object = object;
        this.asItemStack = Suppliers.memoize(() -> this.getResource()
                .getResourceType()
                .asItemStack(this.getObject())
        );
    }

    public Resource<T> getResource() {
        return resource;
    }

    public T getObject() {
        return object;
    }

    public ItemStack asItemStack() {
        return this.asItemStack.get();
    }
}
