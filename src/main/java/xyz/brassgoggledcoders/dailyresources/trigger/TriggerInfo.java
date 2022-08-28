package xyz.brassgoggledcoders.dailyresources.trigger;

import java.util.UUID;

public record TriggerInfo(
        UUID resourceStorageId,
        UUID selectionId,
        UUID chosenById
) {
}
