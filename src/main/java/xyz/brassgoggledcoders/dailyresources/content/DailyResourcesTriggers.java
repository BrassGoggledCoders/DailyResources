package xyz.brassgoggledcoders.dailyresources.content;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class DailyResourcesTriggers {
    public static final Supplier<IForgeRegistry<Trigger>> REGISTRY = DailyResources.getRegistrate()
            .makeRegistry("trigger", Trigger.class, RegistryBuilder::new);

    public static final RegistryEntry<Trigger> RESPAWN = DailyResources.getRegistrate()
            .object("respawn")
            .simple(Trigger.class, () -> new Trigger(new ResourceLocation("item/compass_18")));
    public static final RegistryEntry<Trigger> DAWN = DailyResources.getRegistrate()
            .object("dawn")
            .simple(Trigger.class, () -> new Trigger(new ResourceLocation("item/clock_18")));

    public static final RegistryEntry<Trigger> NOON = DailyResources.getRegistrate()
            .object("noon")
            .simple(Trigger.class, () -> new Trigger(new ResourceLocation("item/clock_00")));

    public static final RegistryEntry<Trigger> DUSK = DailyResources.getRegistrate()
            .object("dusk")
            .simple(Trigger.class, () -> new Trigger(new ResourceLocation("item/clock_47")));

    public static final RegistryEntry<Trigger> MIDNIGHT= DailyResources.getRegistrate()
            .object("midnight")
            .simple(Trigger.class, () -> new Trigger(new ResourceLocation("item/clock_32")));

    public static final RegistryEntry<Trigger> DREAMER = DailyResources.getRegistrate()
            .object("dreamer")
            .simple(Trigger.class, () -> new Trigger(new ResourceLocation("block/red_wool")));

    public static final RegistryEntry<Trigger> NONE = DailyResources.getRegistrate()
            .object("none")
            .simple(Trigger.class, () -> new Trigger(new ResourceLocation("item/barrier")));

    public static void setup() {

    }
}
