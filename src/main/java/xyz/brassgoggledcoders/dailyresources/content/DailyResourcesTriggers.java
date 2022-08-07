package xyz.brassgoggledcoders.dailyresources.content;

import com.tterrag.registrate.util.entry.RegistryEntry;
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
            .simple(Trigger.class, Trigger::new);
    public static final RegistryEntry<Trigger> DAWN = DailyResources.getRegistrate()
            .object("dawn")
            .simple(Trigger.class, Trigger::new);

    public static final RegistryEntry<Trigger> NOON = DailyResources.getRegistrate()
            .object("noon")
            .simple(Trigger.class, Trigger::new);

    public static final RegistryEntry<Trigger> DUSK = DailyResources.getRegistrate()
            .object("dusk")
            .simple(Trigger.class, Trigger::new);

    public static final RegistryEntry<Trigger> MIDNIGHT= DailyResources.getRegistrate()
            .object("midnight")
            .simple(Trigger.class, Trigger::new);

    public static final RegistryEntry<Trigger> DREAMER = DailyResources.getRegistrate()
            .object("dreamer")
            .simple(Trigger.class, Trigger::new);

    public static void setup() {

    }
}
