package xyz.brassgoggledcoders.dailyresources.event;

import com.google.gson.GsonBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.capability.CapabilityProvider;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorageStorage;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.selector.CodecReloadListener;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

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

    @SubscribeEvent
    public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event) {
        if (event.getObject() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == Level.OVERWORLD) {
                CapabilityProvider<ResourceStorageStorage, ResourceStorageStorage> provider = new CapabilityProvider<>(
                        ResourceStorageStorage.CAP,
                        ResourceStorageStorage.CODEC.get(),
                        ResourceStorageStorage::new,
                        ResourceStorageStorage::invalidate
                );
                event.addListener(provider::invalidate);
                event.addCapability(DailyResources.rl("resource_storage_storage"), provider);
            }
        }
    }

    @SubscribeEvent
    public static void timeTrigger(TickEvent.WorldTickEvent worldTickEvent) {
        Level level = worldTickEvent.world;
        if (level instanceof ServerLevel serverLevel && worldTickEvent.phase == TickEvent.Phase.END &&
                level.dimension() == Level.OVERWORLD) {
            long time = level.getDayTime() % 24000L;
            if (time == 1000) {
                triggerAllOnline(serverLevel, DailyResourcesTriggers.DAWN);
            } else if (time == 6000) {
                triggerAllOnline(serverLevel, DailyResourcesTriggers.NOON);
            } else if (time == 13000) {
                triggerAllOnline(serverLevel, DailyResourcesTriggers.DUSK);
            } else if (time == 18000) {
                triggerAllOnline(serverLevel, DailyResourcesTriggers.MIDNIGHT);
            }
        }
    }


    @SubscribeEvent
    public static void respawnTrigger(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.isEndConquered() && event.getPlayer().getLevel() instanceof ServerLevel serverLevel) {
            trigger(serverLevel, DailyResourcesTriggers.RESPAWN, Collections.singleton(event.getPlayer().getUUID()));
        }
    }

    @SubscribeEvent
    public static void dreamerTrigger(PlayerWakeUpEvent event) {
        if (!event.wakeImmediately() && event.getPlayer().getLevel() instanceof ServerLevel serverLevel) {
            trigger(serverLevel, DailyResourcesTriggers.DREAMER, Collections.singleton(event.getPlayer().getUUID()));
        }
    }


    private static void triggerAllOnline(ServerLevel serverLevel, Supplier<Trigger> trigger) {
        trigger(
                serverLevel,
                trigger,
                serverLevel.getServer()
                        .getPlayerList()
                        .getPlayers()
                        .stream()
                        .map(ServerPlayer::getUUID)
                        .toList()
        );
    }

    private static void trigger(ServerLevel level, Supplier<Trigger> trigger, Collection<UUID> players) {
        if (level.dimension() != Level.OVERWORLD) {
            level = level.getServer()
                    .getLevel(Level.OVERWORLD);
        }

        if (level != null) {
            level.getCapability(ResourceStorageStorage.CAP)
                    .ifPresent(storageStorage -> storageStorage.trigger(
                            trigger,
                            players
                    ));
        }
    }
}
