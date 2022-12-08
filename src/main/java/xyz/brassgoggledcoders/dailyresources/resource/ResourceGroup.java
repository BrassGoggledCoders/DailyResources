package xyz.brassgoggledcoders.dailyresources.resource;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ResourceGroup(
        List<Resource<?>> resources,
        Trigger trigger,
        Component name
) {
    public static final Supplier<Codec<ResourceGroup>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Resource.RESOURCE_CODEC.get()).fieldOf("resources").forGetter(ResourceGroup::resources),
            DailyResourcesTriggers.getRegistry().getCodec().fieldOf("trigger").forGetter(ResourceGroup::trigger),
            Codecs.COMPONENT.fieldOf("name").forGetter(ResourceGroup::name)
    ).apply(instance, ResourceGroup::new)));

    public <T> List<Resource<T>> getResourceFor(ResourceType<T> resourceType) {
        List<Resource<T>> matchingResource = new ArrayList<>();
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
