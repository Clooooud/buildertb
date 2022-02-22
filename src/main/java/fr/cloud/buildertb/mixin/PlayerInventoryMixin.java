package fr.cloud.buildertb.mixin;

import fr.cloud.buildertb.BuilderTB;
import fr.cloud.buildertb.toolbox.ToolboxInventory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow @Final public PlayerEntity player;

    @Shadow public int selectedSlot;

    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    private void scrollToolbox(double scrollAmount, CallbackInfo callbackInfo) {
        ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);

        if (!player.isSneaking()) {
            return;
        }

        if (!stackInHand.getItem().equals(BuilderTB.BUILDER_TOOLBOX_ITEM)) {
            return;
        }

        callbackInfo.cancel();

        ToolboxInventory inventory = ToolboxInventory.getFromStack(stackInHand);

        if (inventory.isEmpty()) {
            return;
        }

        int selectedSlot = inventory.getSelectedSlot();

        if (scrollAmount > 0.0) {
            scrollAmount = 1.0;
        }
        if (scrollAmount < 0.0) {
            scrollAmount = -1.0;
        }

        do {
            selectedSlot -= scrollAmount;
            while (selectedSlot < 0) {
                selectedSlot += 9;
            }
            while (selectedSlot >= 9) {
                selectedSlot -= 9;
            }
        } while (inventory.getStack(selectedSlot).isEmpty());

        inventory.setSelectedSlot(selectedSlot);
        inventory.applyChanges(stackInHand);

        // Update stack on the server-side
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(selectedSlot);
        ClientPlayNetworking.send(BuilderTB.TOOLBOX_UPDATE_IDENTIFIER, buf);
    }
}
