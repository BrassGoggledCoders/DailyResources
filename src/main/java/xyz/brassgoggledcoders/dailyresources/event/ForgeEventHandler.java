package xyz.brassgoggledcoders.dailyresources.event;

import com.google.gson.GsonBuilder;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.selector.CodecReloadListener;

@EventBusSubscriber(modid = DailyResources.ID, bus = Bus.FORGE)
public class ForgeEventHandler {

    @SubscribeEvent
    public static void datapackRegistry(AddReloadListenerEvent event) {
        CodecReloadListener<ResourceGroup> selectorCodecReloadListener = new CodecReloadListener<>(
                new GsonBuilder().create(),
                "daily_resources/group",
                ResourceGroup.CODEC.get(),
                event.getConditionContext()
        );
        event.addListener(selectorCodecReloadListener);
        DailyResources.RESOURCE_GROUP_MANAGER = selectorCodecReloadListener;
    }
}
