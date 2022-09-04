package xyz.brassgoggledcoders.dailyresources;


import com.google.common.base.Suppliers;
import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.brassgoggledcoders.dailyresources.content.*;
import xyz.brassgoggledcoders.dailyresources.network.NetworkHandler;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.selector.CodecReloadListener;

import java.util.function.Supplier;

@Mod(DailyResources.ID)
public class DailyResources {
    public static final String ID = "daily_resources";

    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    private static final Supplier<Registrate> REGISTRATE_SUPPLIER = Suppliers.memoize(() -> Registrate.create(ID));

    public static CodecReloadListener<ResourceGroup> RESOURCE_GROUP_MANAGER;

    public DailyResources() {
        DailyResourcesBlocks.setup();
        DailyResourcesContainers.setup();
        DailyResourcesResources.setup();
        DailyResourcesText.setup();
        DailyResourcesTriggers.setup();

        NetworkHandler.getInstance();
    }

    public static Registrate getRegistrate() {
        return REGISTRATE_SUPPLIER.get();
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(ID, path);
    }
}
