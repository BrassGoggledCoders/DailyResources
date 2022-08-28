package xyz.brassgoggledcoders.dailyresources.content;

import net.minecraft.network.chat.Component;
import xyz.brassgoggledcoders.dailyresources.DailyResources;

public class DailyResourcesText {

    public static final Component SELECTION = DailyResources.getRegistrate()
            .addLang("text", DailyResources.rl("selection"), "Resource Selection");

    public static final Component STORAGE = DailyResources.getRegistrate()
            .addLang("text", DailyResources.rl("storage"), "Resource Storage");

    public static void setup() {

    }
}
