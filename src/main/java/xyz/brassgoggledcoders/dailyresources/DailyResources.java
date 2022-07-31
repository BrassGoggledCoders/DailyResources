package xyz.brassgoggledcoders.dailyresources;


import com.google.common.base.Suppliers;
import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesBlocks;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.selector.CodecReloadListener;

import java.util.function.Supplier;

@Mod(DailyResources.ID)
public class DailyResources {
    public static final String ID = "daily_resources";

    private static final Supplier<Registrate> REGISTRATE_SUPPLIER = Suppliers.memoize(() -> Registrate.create(ID));

    public static CodecReloadListener<ResourceGroup> RESOURCE_GROUP_MANAGER;

    public DailyResources() {
        DailyResourcesBlocks.setup();
        DailyResourcesResources.setup();
    }

    public static Registrate getRegistrate() {
        return REGISTRATE_SUPPLIER.get();
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(ID, path);
    }
}
