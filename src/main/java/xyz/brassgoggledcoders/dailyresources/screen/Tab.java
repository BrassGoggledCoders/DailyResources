package xyz.brassgoggledcoders.dailyresources.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record Tab<T>(ItemStack icon, List<Component> components, T marker) {

}
