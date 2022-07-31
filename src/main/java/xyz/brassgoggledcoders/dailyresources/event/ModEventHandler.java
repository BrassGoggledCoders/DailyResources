package xyz.brassgoggledcoders.dailyresources.event;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorageStorage;

@EventBusSubscriber(modid = DailyResources.ID, bus = Bus.MOD)
public class ModEventHandler {

    @SubscribeEvent
    public static void capabilityRegister(RegisterCapabilitiesEvent event) {
        event.register(ResourceStorageStorage.class);
    }
}
