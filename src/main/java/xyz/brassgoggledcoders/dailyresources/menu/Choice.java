package xyz.brassgoggledcoders.dailyresources.menu;

import com.google.common.base.Suppliers;
import net.minecraft.world.item.ItemStack;
import xyz.brassgoggledcoders.dailyresources.resource.Resource;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

import java.util.Optional;
import java.util.function.Supplier;

public class Choice<T> {
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
