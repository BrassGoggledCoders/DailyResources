package xyz.brassgoggledcoders.dailyresources.capability;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.ListenerType;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageSelection;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

import java.util.*;
import java.util.function.Supplier;

public abstract class ResourceStorage implements ICapabilityProvider {
    public static Supplier<Codec<ResourceStorage>> STORAGE_CODEC = Suppliers.memoize(() -> DailyResourcesResources.REGISTRY.get()
            .getCodec()
            .dispatch(ResourceStorage::getResourceType, ResourceType::getStorageCodec)
    );

    private final Map<UUID, ResourceStorageSelection<?>> selections;
    private final EnumMap<ListenerType, Map<ResourceKey<Level>, List<BlockPos>>> listenerPos;

    public ResourceStorage() {
        this.selections = new HashMap<>();
        this.listenerPos = new EnumMap<>(ListenerType.class);
    }

    public Collection<ResourceStorageSelection<?>> getSelections() {
        return selections.values();
    }

    public ResourceStorageSelection<?> getSelection(UUID uniqueId) {
        return this.selections.get(uniqueId);
    }

    public abstract ResourceType<?> getResourceType();

    public abstract void invalidateCapabilities();

    /**
     * @return If the Storage is unable to receive a full selection
     */
    public abstract boolean trigger(ResourceStorageSelection<?> resourceStorageSelection);

    public boolean addSelection(ResourceStorageSelection<?> resourceSelection) {
        if (this.hasSelection(resourceSelection.id())) {
            return false;
        } else {
            this.selections.put(resourceSelection.id(), resourceSelection);
            return true;
        }
    }

    public boolean hasSelection(UUID uuid) {
        return this.selections.containsKey(uuid);
    }

    public void addListener(ListenerType listenerType, Level level, BlockPos blockPos) {
        List<BlockPos> positions = this.listenerPos.computeIfAbsent(listenerType, value -> new HashMap<>())
                .computeIfAbsent(level.dimension(), value -> new ArrayList<>());

        if (positions.stream().noneMatch(blockPos::equals)) {
            positions.add(blockPos);
        }
    }

    public void addListeners(ListenerType listenerType, ResourceKey<Level> level, List<BlockPos> blockPos) {
        this.listenerPos.computeIfAbsent(listenerType, value -> new HashMap<>())
                .computeIfAbsent(level, value -> new ArrayList<>())
                .addAll(blockPos);
    }

    public Map<ResourceKey<Level>, List<BlockPos>> getListenersFor(ListenerType listenerType) {
        return this.listenerPos.getOrDefault(listenerType, Collections.emptyMap());
    }

    public void removeListenerPos(Level level, BlockPos blockPos) {
        for (Map<ResourceKey<Level>, List<BlockPos>> listeners : this.listenerPos.values()) {
            List<BlockPos> currentLists = listeners.get(level.dimension());
            if (currentLists != null) {
                currentLists.removeIf(blockPos::equals);
            }

            if (currentLists != null && currentLists.isEmpty()) {
                listeners.remove(level.dimension());
            }
        }
        this.listenerPos.values()
                .removeIf(Map::isEmpty);
    }

    public Map<ListenerType, Map<ResourceKey<Level>, List<BlockPos>>> getListeners() {
        return this.listenerPos;
    }
}
