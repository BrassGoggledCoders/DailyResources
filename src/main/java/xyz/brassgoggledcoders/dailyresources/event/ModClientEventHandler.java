package xyz.brassgoggledcoders.dailyresources.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.model.barrel.TriggerModelLoader;
import xyz.brassgoggledcoders.dailyresources.model.fluidtop.FluidTopModelLoader;

@EventBusSubscriber(modid = DailyResources.ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ModClientEventHandler {
    @SubscribeEvent
    public static void registerModelLoader(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(TriggerModelLoader.ID, new TriggerModelLoader());
        ModelLoaderRegistry.registerLoader(FluidTopModelLoader.ID, new FluidTopModelLoader());
    }
}
