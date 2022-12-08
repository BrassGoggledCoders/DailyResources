package xyz.brassgoggledcoders.dailyresources.content;

import com.google.common.base.Suppliers;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class DailyResourcesTriggers {
    public static final ResourceKey<Registry<Trigger>> REGISTRY_KEY = DailyResources.getRegistrate()
            .makeRegistry("trigger", RegistryBuilder::new);

    private static final Supplier<IForgeRegistry<Trigger>> REGISTRY = Suppliers.memoize(() -> RegistryManager.ACTIVE.getRegistry(REGISTRY_KEY));

    public static final RegistryEntry<Trigger> RESPAWN = DailyResources.getRegistrate()
            .object("respawn")
            .simple(REGISTRY_KEY, () -> new Trigger(new ResourceLocation("item/compass_18")));
    public static final RegistryEntry<Trigger> DAWN = DailyResources.getRegistrate()
            .object("dawn")
            .simple(REGISTRY_KEY, () -> new Trigger(new ResourceLocation("item/clock_48")));

    public static final RegistryEntry<Trigger> NOON = DailyResources.getRegistrate()
            .object("noon")
            .simple(REGISTRY_KEY, () -> new Trigger(new ResourceLocation("item/clock_00")));

    public static final RegistryEntry<Trigger> DUSK = DailyResources.getRegistrate()
            .object("dusk")
            .simple(REGISTRY_KEY, () -> new Trigger(new ResourceLocation("item/clock_18")));

    public static final RegistryEntry<Trigger> MIDNIGHT = DailyResources.getRegistrate()
            .object("midnight")
            .simple(REGISTRY_KEY, () -> new Trigger(new ResourceLocation("item/clock_32")));

    public static final RegistryEntry<Trigger> DREAMER = DailyResources.getRegistrate()
            .object("dreamer")
            .simple(REGISTRY_KEY, () -> new Trigger(new ResourceLocation("block/red_wool")));

    public static final RegistryEntry<Trigger> ANY = DailyResources.getRegistrate()
            .object("any")
            .simple(REGISTRY_KEY, () -> new Trigger(new ResourceLocation("item/barrier")));

    public static final RegistryEntry<Trigger> NONE = DailyResources.getRegistrate()
            .object("none")
            .simple(REGISTRY_KEY, () -> new Trigger(new ResourceLocation("item/name_tag")));

    public static IForgeRegistry<Trigger> getRegistry() {
        return REGISTRY.get();
    }

    public static void setup() {

    }
}
