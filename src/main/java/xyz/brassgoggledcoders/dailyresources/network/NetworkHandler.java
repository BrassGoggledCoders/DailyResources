package xyz.brassgoggledcoders.dailyresources.network;

import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.brassgoggledcoders.dailyresources.DailyResources;

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
                .consumer(MenuClickPacket::consume)
                .add();
    }

    public void sendMenuClick(int containerId, int buttonId) {
        this.simpleChannel.send(PacketDistributor.SERVER.noArg(), new MenuClickPacket(buttonId, containerId));
    }

    public static NetworkHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NetworkHandler();
        }
        return INSTANCE;
    }
}
