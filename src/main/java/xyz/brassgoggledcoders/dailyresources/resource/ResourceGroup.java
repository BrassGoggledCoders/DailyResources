package xyz.brassgoggledcoders.dailyresources.resource;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;
import xyz.brassgoggledcoders.dailyresources.menu.Choice;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public record ResourceGroup(
        List<Resource<?>> resources,
        Trigger trigger
) {
    public static final Supplier<Codec<ResourceGroup>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Resource.CODEC.get()).fieldOf("resources").forGetter(ResourceGroup::resources),
            DailyResourcesTriggers.REGISTRY.get().getCodec().fieldOf("trigger").forGetter(ResourceGroup::trigger)
    ).apply(instance, ResourceGroup::new)));

    public <T> List<Resource<T>> getResourceFor(ResourceType<T> resourceType) {
        List<Resource<T>> matchingResource = Lists.newArrayList();
        for (Resource<?> resource : resources) {
            resource.cast(resourceType)
                    .ifPresent(matchingResource::add);
        }
        return matchingResource;
    }

    public <T> List<Choice<T>> getChoicesFor(ResourceType<T> resourceType) {
        List<Choice<T>> choices = new ArrayList<>();
        for (Resource<T> resource : this.getResourceFor(resourceType)) {
            choices.addAll(resource.asChoices());
        }
        return choices;
    }
}
