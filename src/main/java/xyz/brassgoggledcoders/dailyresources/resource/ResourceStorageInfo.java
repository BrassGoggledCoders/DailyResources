package xyz.brassgoggledcoders.dailyresources.resource;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public record ResourceStorageInfo(
        ResourceLocation resourceGroupId,
        Resource resource,
        ItemStack choice
) {
    public static final Supplier<Codec<ResourceStorageInfo>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("resourceGroupId").forGetter(ResourceStorageInfo::resourceGroupId),
                    Resource.CODEC.get().fieldOf("resource").forGetter(ResourceStorageInfo::resource),
                    ItemStack.CODEC.fieldOf("choice").forGetter(ResourceStorageInfo::choice)
            ).apply(instance, ResourceStorageInfo::new))
    );
}
