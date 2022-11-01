package fr.cloud.buildertb.toolbox;

import fr.cloud.buildertb.BuilderTB;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Toolbox extends Item {

    public Toolbox() {
        super(new FabricItemSettings().rarity(Rarity.UNCOMMON).group(BuilderTB.TAB).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking() && hand == Hand.MAIN_HAND) {
            user.openHandledScreen(ToolboxInventory.getFromStack(user.getStackInHand(hand)));
            return TypedActionResult.success(user.getStackInHand(hand));
        }

        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(MutableText.of(new TranslatableTextContent("tooltip.buildertb.toolbox")));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSneaking() && context.getHand() == Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        ItemStack stack = context.getStack();
        ToolboxInventory inventory = ToolboxInventory.getFromStack(stack);

        if (inventory.isEmpty()) {
            return ActionResult.PASS;
        }

        inventory.updateSlotIfNeeded();

        ItemStack stackToPlace = context.getPlayer().isCreative() ? inventory.getStack(inventory.getSelectedSlot()) : inventory.removeStack(inventory.getSelectedSlot(), 1);
        inventory.applyChanges(stack);
        BlockItem blockItem = (BlockItem) stackToPlace.getItem();

        return blockItem.place(new ItemPlacementContext(context.getPlayer(), context.getHand(), stackToPlace, new BlockHitResult(context.getHitPos(), context.getSide(), context.getBlockPos(), context.hitsInsideBlock())));
    }
}
