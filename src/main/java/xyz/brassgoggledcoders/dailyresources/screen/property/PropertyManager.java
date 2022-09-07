package xyz.brassgoggledcoders.dailyresources.screen.property;

import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Triple;
import xyz.brassgoggledcoders.dailyresources.network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;

public class PropertyManager {
    private final List<Property<?>> properties;
    private final short menuId;

    public PropertyManager(short menuId) {
        this.menuId = menuId;
        this.properties = new ArrayList<>();
    }

    public <T> Property<T> addTrackedProperty(Property<T> property) {
        this.properties.add(property);
        return property;
    }

    public void sendChanges(ServerPlayer serverPlayer, boolean firstTime) {

        List<Triple<PropertyType<?>, Short, Object>> dirtyProperties = new ArrayList<>();
        for (short i = 0; i < properties.size(); i++) {
            Property<?> property = properties.get(i);
            if (property.isDirty() || firstTime) {
                dirtyProperties.add(Triple.of(property.getPropertyType(), i, property.get()));
            }
        }

        if (!dirtyProperties.isEmpty()) {
            NetworkHandler.getInstance().sendUpdateClientContainerPropertiesPacket(serverPlayer, menuId, dirtyProperties);
        }
    }

    public void update(PropertyType<?> propertyType, short propertyId, Object value) {
        if (propertyId < properties.size()) {
            Property<?> property = properties.get(propertyId);
            if (property != null && property.getPropertyType() == propertyType) {
                propertyType.attemptSet(value, property);
            }
        }
    }
}
