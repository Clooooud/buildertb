package fr.cloud.buildertb.toolbox;

import fr.cloud.buildertb.BuilderTB;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ToolboxScreenHandler extends ScreenHandler {

    private final Inventory inventory;

    public ToolboxScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }

    public ToolboxScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(BuilderTB.TOOLBOX_SCREENHANDLER, syncId);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        int j;
        for(j = 0; j < 9; ++j) {
            this.addSlot(new CustomSlot(inventory, j, 8 + j * 18, 20));
        }

        for(j = 0; j < 3; ++j) {
            for(int k = 0; k < 9; ++k) {
                this.addSlot(new CustomSlot(playerInventory, k + j * 9 + 9, 8 + k * 18, j * 18 + 51));
            }
        }

        for(j = 0; j < 9; ++j) {
            this.addSlot(new CustomSlot(playerInventory, j, 8 + j * 18, 109));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < this.inventory.size() ? !this.insertItem(itemStack2, this.inventory.size(), this.slots.size(), true) : !this.insertItem(itemStack2, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return itemStack;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.inventory.onClose(player);
    }

    private static class CustomSlot extends Slot {

        public CustomSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return isMovementAllowed(getStack());
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return isMovementAllowed(stack);
        }

        private boolean isMovementAllowed(ItemStack stack) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                return !(blockItem.getBlock() instanceof BlockEntityProvider);
            }
            return false;
        }
    }
}
