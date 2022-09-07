package xyz.brassgoggledcoders.dailyresources.screen.property;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Triple;
import xyz.brassgoggledcoders.dailyresources.DailyResources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UpdateClientContainerPropertiesPacket {
    private final short menuId;
    private final List<Triple<PropertyType<?>, Short, Object>> updates;

    public UpdateClientContainerPropertiesPacket(short menuId, List<Triple<PropertyType<?>, Short, Object>> updates) {
        this.menuId = menuId;
        this.updates = updates;
    }

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeShort(menuId);
        List<Triple<PropertyType<?>, Short, Object>> validUpdates = new ArrayList<>();
        for (Triple<PropertyType<?>, Short, Object> update : updates) {
            if (update.getLeft().isValid(update.getRight())) {
                validUpdates.add(update);
            }
        }

        packetBuffer.writeShort(validUpdates.size());
        for (Triple<PropertyType<?>, Short, Object> update : validUpdates) {
            packetBuffer.writeShort(PropertyTypes.getIndex(update.getLeft()));
            packetBuffer.writeShort(update.getMiddle());
            update.getLeft().attemptWrite(packetBuffer, update.getRight());
        }
    }

    public boolean consume(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            LocalPlayer playerEntity = Minecraft.getInstance().player;
            if (playerEntity != null && playerEntity.containerMenu instanceof IPropertyManaged propertyManaged) {
                if (playerEntity.containerMenu.containerId == menuId) {
                    PropertyManager propertyManager = propertyManaged.getPropertyManager();
                    for (Triple<PropertyType<?>, Short, Object> update : updates) {
                        propertyManager.update(update.getLeft(), update.getMiddle(), update.getRight());
                    }
                }
            } else {
                DailyResources.LOGGER.info("Container is not instance of IPropertyManaged");
            }
        });
        return true;
    }

    public static UpdateClientContainerPropertiesPacket decode(FriendlyByteBuf packetBuffer) {
        short windowId = packetBuffer.readShort();
        short updateAmount = packetBuffer.readShort();
        List<Triple<PropertyType<?>, Short, Object>> updates = new ArrayList<>();
        for (short i = 0; i < updateAmount; i++) {
            PropertyType<?> propertyType = PropertyTypes.getByIndex(packetBuffer.readShort());
            short propertyLocation = packetBuffer.readShort();
            Object object = propertyType.getReader().apply(packetBuffer);
            updates.add(Triple.of(propertyType, propertyLocation, object));
        }
        return new UpdateClientContainerPropertiesPacket(windowId, updates);
    }
}
