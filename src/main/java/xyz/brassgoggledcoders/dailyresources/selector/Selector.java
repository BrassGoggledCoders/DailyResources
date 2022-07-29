package xyz.brassgoggledcoders.dailyresources.selector;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class Selector {

    public static final Codec<Selector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ItemStackGenerator.CODEC)
                    .fieldOf("generators")
                    .forGetter(Selector::getGenerators)
    ).apply(instance, Selector::new));

    private final List<ItemStackGenerator> generators;
    private final Supplier<List<ItemStack>> itemStacks;

    public Selector(List<ItemStackGenerator> generators) {
        this.generators = generators;
        this.itemStacks = Suppliers.memoize(() -> this.getGenerators()
                .stream()
                .map(ItemStackGenerator::generate)
                .flatMap(List::stream)
                .toList()
        );
    }

    public List<ItemStack> getItemStacks() {
        return this.itemStacks.get();
    }

    public List<ItemStackGenerator> getGenerators() {
        return this.generators;
    }
}

record ItemStackGenerator(
        Optional<TagKey<Item>> tagKey,
        Optional<Integer> count,
        Optional<ItemStack> itemStack
) {
    public static final Codec<ItemStackGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registry.ITEM_REGISTRY).optionalFieldOf("tag").forGetter(ItemStackGenerator::tagKey),
            Codec.INT.optionalFieldOf("count").forGetter(ItemStackGenerator::count),
            ItemStack.CODEC.optionalFieldOf("itemStack").forGetter(ItemStackGenerator::itemStack)
    ).apply(instance, ItemStackGenerator::new));

    public List<ItemStack> generate() {
        List<ItemStack> list = Lists.newArrayList();
        this.tagKey()
                .map(Objects.requireNonNull(ForgeRegistries.ITEMS.tags())::getTag)
                .stream()
                .flatMap(ITag::stream)
                .map(item -> new ItemStack(item, count().orElse(1)))
                .forEach(list::add);

        this.itemStack.ifPresent(list::add);

        return list;
    }
}
