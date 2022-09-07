package xyz.brassgoggledcoders.dailyresources.content;

import com.tterrag.registrate.util.entry.MenuEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.menu.FluidResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.menu.ItemResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.screen.fluid.FluidResourceSelectorScreen;
import xyz.brassgoggledcoders.dailyresources.screen.ItemResourceSelectorScreen;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceSelectorScreen;
import xyz.brassgoggledcoders.dailyresources.screen.ItemResourceStorageScreen;
import xyz.brassgoggledcoders.dailyresources.screen.fluid.FluidResourceStorageScreen;

public class DailyResourcesContainers {
    public static final MenuEntry<ItemResourceStorageMenu> STORAGE_MENU = DailyResources.getRegistrate()
            .object("storage")
            .menu(ItemResourceStorageMenu::create, () -> ItemResourceStorageScreen::new)
            .register();

    public static final MenuEntry<ResourceSelectorMenu<ItemStack>> ITEM_SELECTOR_MENU = DailyResources.getRegistrate()
            .object("selector")
            .<ResourceSelectorMenu<ItemStack>, ResourceSelectorScreen<ItemStack>>menu(
                    ResourceSelectorMenu::createItem,
                    () -> ItemResourceSelectorScreen::new
            )
            .register();

    public static final MenuEntry<FluidResourceStorageMenu> FLUID_STORAGE_MENU = DailyResources.getRegistrate()
            .object("fluid_storage")
            .menu(FluidResourceStorageMenu::create, () -> FluidResourceStorageScreen::new)
            .register();

    public static final MenuEntry<ResourceSelectorMenu<FluidStack>> FLUID_SELECTOR_MENU = DailyResources.getRegistrate()
            .object("fluid_selector")
            .<ResourceSelectorMenu<FluidStack>, ResourceSelectorScreen<FluidStack>>menu(
                    ResourceSelectorMenu::createFluid,
                    () -> FluidResourceSelectorScreen::new
            )
            .register();

    public static void setup() {

    }
}
