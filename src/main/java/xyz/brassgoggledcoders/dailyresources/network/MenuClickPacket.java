package xyz.brassgoggledcoders.dailyresources.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.function.Supplier;

public class MenuClickPacket {
    private final int buttonId;
    private final int containerId;

    public MenuClickPacket(int buttonId, int containerId) {
        this.buttonId = buttonId;
        this.containerId = containerId;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(buttonId);
        friendlyByteBuf.writeInt(containerId);
    }

    public boolean consume(Supplier<NetworkEvent.Context> contextSupplier) {
        ServerPlayer serverPlayer = contextSupplier.get().getSender();
        if (serverPlayer != null) {
            contextSupplier.get().enqueueWork(() -> {
                if (serverPlayer.containerMenu.containerId == this.containerId) {
                    serverPlayer.containerMenu.clickMenuButton(serverPlayer, buttonId);
                }
            });
        }

        return true;
    }

    public static MenuClickPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new MenuClickPacket(
                friendlyByteBuf.readInt(),
                friendlyByteBuf.readInt()
        );
    }
}
