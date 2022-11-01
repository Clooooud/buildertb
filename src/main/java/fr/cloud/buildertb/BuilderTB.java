package fr.cloud.buildertb;

import fr.cloud.buildertb.toolbox.Toolbox;
import fr.cloud.buildertb.toolbox.ToolboxInventory;
import fr.cloud.buildertb.toolbox.ToolboxScreenHandler;
import java.util.Timer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BuilderTB implements ModInitializer {

    public static final String MOD_ID = "buildertb";
    public static final ItemGroup TAB = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "general"), () -> new ItemStack(BuilderTB.BUILDER_TOOLBOX_ITEM));
    public static final Identifier TOOLBOX_IDENTIFIER = new Identifier(MOD_ID, "toolbox");
    public static final Identifier TOOLBOX_UPDATE_IDENTIFIER = new Identifier(MOD_ID, "toolbox_update");

    public static final Item BUILDER_TOOLBOX_ITEM = new Toolbox();

    public static ScreenHandlerType<ToolboxScreenHandler> TOOLBOX_SCREENHANDLER;

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, TOOLBOX_IDENTIFIER, BUILDER_TOOLBOX_ITEM);
        TOOLBOX_SCREENHANDLER = Registry.register(Registry.SCREEN_HANDLER, TOOLBOX_IDENTIFIER, new ScreenHandlerType<>(ToolboxScreenHandler::new));

        // Receive update packet after scrolling the toolbox
        ServerPlayNetworking.registerGlobalReceiver(BuilderTB.TOOLBOX_UPDATE_IDENTIFIER, (server, player, handler, buf, responseSender) -> {
            ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

            if (!stack.getItem().equals(BuilderTB.BUILDER_TOOLBOX_ITEM)) {
                return;
            }

            ToolboxInventory inventory = ToolboxInventory.getFromStack(stack);
            inventory.setSelectedSlot(buf.getInt(0));
            inventory.applyChanges(stack);
        });
    }
}
