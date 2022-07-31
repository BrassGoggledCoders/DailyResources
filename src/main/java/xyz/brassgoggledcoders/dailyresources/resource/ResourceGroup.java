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

import java.util.List;
import java.util.function.Supplier;

public record ResourceGroup(
        List<Resource> resources
) {
    public static final Supplier<Codec<ResourceGroup>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Resource.CODEC.get()).fieldOf("resources").forGetter(ResourceGroup::resources)
    ).apply(instance, ResourceGroup::new)));

    public List<Resource> getResourceFor(ResourceType resourceType) {
        List<Resource> matchingResource = Lists.newArrayList();
        for (Resource resource : resources) {
            if (resource.getResourceType() == resourceType) {
                matchingResource.add(resource);
            }
        }
        return matchingResource;
    }

    public Multimap<Resource, ItemStack> getChoicesFor(ResourceType resourceType) {
        Multimap<Resource, ItemStack> choices = Multimaps.newSetMultimap(Maps.newHashMap(), Sets::newHashSet);
        for (Resource resource : this.getResourceFor(resourceType)) {
            choices.putAll(resource, resource.asChoices());
        }
        return choices;
    }

    public boolean contains(Resource resource) {
        return resources().contains(resource);
    }
}
