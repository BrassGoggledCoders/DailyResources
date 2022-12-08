package xyz.brassgoggledcoders.dailyresources.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Triple;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.screen.property.PropertyType;
import xyz.brassgoggledcoders.dailyresources.screen.property.UpdateClientContainerPropertiesPacket;

import java.util.List;

public class NetworkHandler {
    private static final String VERSION = "1";
    private static NetworkHandler INSTANCE;

    private final SimpleChannel simpleChannel;

    public NetworkHandler() {
        this.simpleChannel = ChannelBuilder.named(DailyResources.rl("network"))
                .networkProtocolVersion(() -> VERSION)
                .clientAcceptedVersions(VERSION::matches)
                .serverAcceptedVersions(VERSION::matches)
                .simpleChannel();

        this.simpleChannel.messageBuilder(MenuClickPacket.class, 0)
                .encoder(MenuClickPacket::encode)
                .decoder(MenuClickPacket::decode)
                .consumerMainThread(MenuClickPacket::consume)
                .add();

        this.simpleChannel.messageBuilder(UpdateClientContainerPropertiesPacket.class, 1)
                .encoder(UpdateClientContainerPropertiesPacket::encode)
                .decoder(UpdateClientContainerPropertiesPacket::decode)
                .consumerMainThread(UpdateClientContainerPropertiesPacket::consume)
                .add();
    }

    public void sendMenuClick(int containerId, int buttonId) {
        this.simpleChannel.send(PacketDistributor.SERVER.noArg(), new MenuClickPacket(buttonId, containerId));
    }

    public void sendUpdateClientContainerPropertiesPacket(ServerPlayer serverPlayer,
                                                          short menuId,
                                                          List<Triple<PropertyType<?>, Short, Object>> dirtyProperties) {
        this.simpleChannel.send(
                PacketDistributor.PLAYER.with(() -> serverPlayer),
                new UpdateClientContainerPropertiesPacket(menuId, dirtyProperties)
        );
    }

    public static NetworkHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NetworkHandler();
        }
        return INSTANCE;
    }
}
