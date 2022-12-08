package xyz.brassgoggledcoders.dailyresources.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

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

    public void consume(Supplier<NetworkEvent.Context> contextSupplier) {
        ServerPlayer serverPlayer = contextSupplier.get().getSender();
        if (serverPlayer != null) {
            if (serverPlayer.containerMenu.containerId == this.containerId) {
                serverPlayer.containerMenu.clickMenuButton(serverPlayer, buttonId);
            }
        }
    }

    public static MenuClickPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new MenuClickPacket(
                friendlyByteBuf.readInt(),
                friendlyByteBuf.readInt()
        );
    }
}
