package xyz.brassgoggledcoders.dailyresources.content;

import com.tterrag.registrate.util.entry.MenuEntry;
import net.minecraft.world.item.ItemStack;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.screen.ItemResourceSelectorScreen;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceSelectorScreen;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceStorageScreen;

public class DailyResourcesContainers {
    public static final MenuEntry<ResourceStorageMenu> STORAGE_MENU = DailyResources.getRegistrate()
            .object("storage")
            .menu(ResourceStorageMenu::create, () -> ResourceStorageScreen::new)
            .register();

    public static final MenuEntry<ResourceSelectorMenu<ItemStack>> ITEM_SELECTOR_MENU = DailyResources.getRegistrate()
            .object("selector")
            .<ResourceSelectorMenu<ItemStack>, ResourceSelectorScreen<ItemStack>>menu(
                    ResourceSelectorMenu::createItem,
                    () -> ItemResourceSelectorScreen::new
            )
            .register();

    public static void setup() {

    }
}
