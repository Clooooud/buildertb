package fr.cloud.buildertb.toolbox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class ToolboxInventory implements Inventory, NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private int selectedSlot;

    public static ToolboxInventory getFromStack(ItemStack stack) {
        return new ToolboxInventory(stack);
    }

    private ToolboxInventory(ItemStack stack) {
        if (!stack.hasNbt()) {
            stack.setNbt(new NbtCompound());
        }

        if (!stack.getNbt().contains("toolbox")) {
            stack.getNbt().put("toolbox", new NbtCompound());
            stack.getNbt().getCompound("toolbox").putInt("slot", 0);
        }

        this.fromTag(stack.getNbt().getCompound("toolbox"));
    }

    @Override
    public void onClose(PlayerEntity player) {
        Inventory.super.onClose(player);

        applyChanges(player.getStackInHand(Hand.MAIN_HAND));
    }

    public void applyChanges(ItemStack stack) {
        if (!stack.hasNbt()) {
            stack.setNbt(new NbtCompound());
        }

        stack.getNbt().put("toolbox", toTag());
    }

    private NbtCompound toTag() {
        NbtCompound nbtCompound = new NbtCompound();
        NbtList nbtList = new NbtList();

        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);

            if (itemStack.isEmpty()) {
                continue;
            }

            NbtCompound itemCompound = new NbtCompound();
            itemCompound.putInt("slot", i);
            itemStack.writeNbt(itemCompound);
            nbtList.add(itemCompound);
        }

        nbtCompound.putInt("slot", selectedSlot);
        nbtCompound.put("items", nbtList);

        return nbtCompound;
    }

    private void fromTag(NbtCompound tag) {
        this.selectedSlot = tag.getInt("slot");
        NbtList list = tag.getList("items", 10);

        list.forEach(nbtElement -> {
            NbtCompound compound = (NbtCompound) nbtElement;
            int slot = compound.getInt("slot");
            items.set(slot, ItemStack.fromNbt(compound));
        });
    }

    @Override
    public int size() {
        return 9;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.items, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)) {
            return;
        }

        this.items.set(slot, stack);
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.items.clear();
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container.buildertb.toolbox");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ToolboxScreenHandler(syncId, inv, this);
    }
}
