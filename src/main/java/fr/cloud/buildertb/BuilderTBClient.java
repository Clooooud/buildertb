package fr.cloud.buildertb;

import fr.cloud.buildertb.toolbox.ToolboxScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class BuilderTBClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BuilderTB.TOOLBOX_SCREENHANDLER, ToolboxScreen::new);
    }

    public static void sendUpdatePacket(int slot) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(slot);
        ClientPlayNetworking.send(BuilderTB.TOOLBOX_UPDATE_IDENTIFIER, buf);
    }
}
