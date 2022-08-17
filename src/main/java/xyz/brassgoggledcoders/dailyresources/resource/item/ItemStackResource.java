package xyz.brassgoggledcoders.dailyresources.resource.item;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.menu.Choice;
import xyz.brassgoggledcoders.dailyresources.resource.Resource;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ItemStackResource implements Resource<ItemStack> {
    public static final Codec<ItemStackResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(TagKey.hashedCodec(Registry.ITEM_REGISTRY), ForgeRegistries.ITEMS.getCodec())
                    .fieldOf("item")
                    .forGetter(ItemStackResource::getItem),
            Codec.INT.optionalFieldOf("count", 1)
                    .forGetter(ItemStackResource::getCount),
            CompoundTag.CODEC.optionalFieldOf("nbt")
                    .forGetter(itemStackResource -> Optional.ofNullable(itemStackResource.getNbt()))
    ).apply(instance, (item, count, nbt) -> new ItemStackResource(item, count, nbt.orElse(null))));


    private final Either<TagKey<Item>, Item> item;
    private final int count;
    private final CompoundTag nbt;

    private final Supplier<Collection<Choice<ItemStack>>> choices = Suppliers.memoize(this::generateChoices);

    public ItemStackResource(Either<TagKey<Item>, Item> item, int count, CompoundTag nbt) {
        this.item = item;
        this.count = count;
        this.nbt = nbt;
    }

    @Override
    @NotNull
    public ResourceType<ItemStack> getResourceType() {
        return DailyResourcesResources.ITEMSTACK.get();
    }

    @Override
    @NotNull
    public Collection<Choice<ItemStack>> asChoices() {
        return this.choices.get();
    }

    @Override
    public boolean contains(@NotNull ItemStack itemStack) {
        return !itemStack.isEmpty() && this.asChoices()
                .stream()
                .anyMatch(choice -> ItemStack.matches(choice.getObject(), itemStack));
    }

    @Override
    public void addToStorage(@NotNull ItemStack choice, @NotNull ICapabilityProvider capabilityProvider) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Optional<Resource<U>> cast(ResourceType<U> resourceType) {
        if (resourceType == this.getResourceType()) {
            return Optional.of((Resource<U>) this);
        }
        return Optional.empty();
    }

    public Either<TagKey<Item>, Item> getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    private List<Choice<ItemStack>> generateChoices() {
        return this.getItem()
                .map(
                        tag -> Objects.requireNonNull(ForgeRegistries.ITEMS.tags())
                                .getTag(tag)
                                .stream(),
                        Stream::of
                ).map(value -> {
                    ItemStack itemStack = new ItemStack(value, this.getCount());
                    itemStack.setTag(this.getNbt());
                    return itemStack;
                })
                .map(itemStack -> new Choice<>(this, itemStack))
                .toList();
    }
}
