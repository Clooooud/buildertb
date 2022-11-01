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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class ToolboxInventory implements Inventory, NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private int selectedSlot;
    private Text title = null;

    public static ToolboxInventory getFromStack(ItemStack stack) {
        return new ToolboxInventory(stack);
    }

    private ToolboxInventory(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateSubNbt("toolbox");

        if (stack.hasCustomName()) {
            title = stack.getName();
        }

        this.fromTag(nbt);
    }

    public boolean updateSlotIfNeeded() {
        int before = this.getSelectedSlot();

        if (!this.isEmpty() && this.getStack(this.getSelectedSlot()).isEmpty()) {
            int slot = 0;
            while (this.getStack(slot).isEmpty()) {
                slot++;
            }

            this.setSelectedSlot(slot);
        }

        return before != this.getSelectedSlot();
    }

    @Override
    public void onClose(PlayerEntity player) {
        Inventory.super.onClose(player);

        applyChanges(player.getStackInHand(Hand.MAIN_HAND));
    }

    public void applyChanges(ItemStack stack) {
        stack.getOrCreateNbt().put("toolbox", toTag());
    }

    public NbtCompound toTag() {
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
        return title == null ? MutableText.of(new TranslatableTextContent("container.buildertb.toolbox")).formatted(Formatting.YELLOW) : this.title.copy().formatted(Formatting.YELLOW);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ToolboxScreenHandler(syncId, inv, this);
    }
}
