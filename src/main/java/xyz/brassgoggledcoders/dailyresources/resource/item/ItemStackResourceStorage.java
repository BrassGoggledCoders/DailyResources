package xyz.brassgoggledcoders.dailyresources.resource.item;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageSelection;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

import java.util.function.Supplier;

public class ItemStackResourceStorage extends ResourceStorage {
    public static final Supplier<Codec<ItemStackResourceStorage>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(instance -> instance.group(
                    ItemStackResourceItemHandler.CODEC.fieldOf("itemHandler")
                            .forGetter(ItemStackResourceStorage::getItemHandler)
            ).apply(instance, ItemStackResourceStorage::new))
    );

    private final ItemStackResourceItemHandler itemHandler;
    private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(this::getItemHandler);

    public ItemStackResourceStorage(ItemStackResourceItemHandler itemHandler) {
        super();
        this.itemHandler = itemHandler;
    }

    public ItemStackResourceItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public void invalidateCapabilities() {
        lazyOptional.invalidate();
    }

    @Override
    public void trigger(ResourceStorageSelection<?> storageSelection) {
        storageSelection.getChoice(DailyResourcesResources.ITEMSTACK.get())
                .ifPresent(itemStack -> ItemHandlerHelper.insertItemStacked(this.getItemHandler(), itemStack, false));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, lazyOptional);
    }

    @Override
    public ResourceType<ItemStack> getResourceType() {
        return DailyResourcesResources.ITEMSTACK.get();
    }
}
