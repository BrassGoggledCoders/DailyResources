package xyz.brassgoggledcoders.dailyresources.blockentity;

import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.resource.ListenedEvent;

public interface IResourceListener {
    void onEvent(@NotNull ListenedEvent listenedEvent);
}
