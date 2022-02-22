package fr.cloud.buildertb;

import fr.cloud.buildertb.toolbox.ToolboxScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class BuilderTBClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BuilderTB.TOOLBOX_SCREENHANDLER, ToolboxScreen::new);
    }
}
