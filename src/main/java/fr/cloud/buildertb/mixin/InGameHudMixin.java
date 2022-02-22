package fr.cloud.buildertb.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.cloud.buildertb.BuilderTB;
import fr.cloud.buildertb.toolbox.ToolboxInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Shadow @Final private static Identifier WIDGETS_TEXTURE;

    @Shadow public abstract TextRenderer getTextRenderer();
    @Shadow protected abstract PlayerEntity getCameraPlayer();
    @Shadow protected abstract void renderHotbarItem(int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed);

    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void renderToolboxHotbar(float tickDelta, MatrixStack matrices, CallbackInfo ci) {
        if (!currentStack.getItem().equals(BuilderTB.BUILDER_TOOLBOX_ITEM)) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);

        int x = this.scaledWidth / 2;
        int y = this.scaledHeight - 22 - 30;
        int offset = this.getZOffset();

        this.setZOffset(-90);
        this.drawTexture(matrices, x - 91, y, 0, 0, 182, 22);

        ToolboxInventory inventory = ToolboxInventory.getFromStack(currentStack);

        this.drawTexture(matrices, x - 92 + inventory.getSelectedSlot() * 20, y - 1, 0, 22, 24, 22);

        this.setZOffset(offset);
        RenderSystem.defaultBlendFunc();

        int m = 1;
        int x2;
        int y2;
        for (int n = 0; n < 9; ++n) {
            x2 = x - 90 + n * 20 + 2;
            y2 = this.scaledHeight - 16 - 3 - 30;
            this.renderHotbarItem(x2, y2, tickDelta, getCameraPlayer(), inventory.getStack(n), m++);
        }
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    private void renderToolboxTooltip(MatrixStack matrices, CallbackInfo ci) {
        if (!this.currentStack.getItem().equals(BuilderTB.BUILDER_TOOLBOX_ITEM)) {
            return;
        }

        // Basically just moving the tooltip above the newly rendered hotbar of the toolbox

        ci.cancel();

        this.client.getProfiler().push("selectedItemName");
        if (this.heldItemTooltipFade > 0 && !this.currentStack.isEmpty()) {
            int l;
            MutableText mutableText = new LiteralText("").append(this.currentStack.getName()).formatted(this.currentStack.getRarity().formatting);
            if (this.currentStack.hasCustomName()) {
                mutableText.formatted(Formatting.ITALIC);
            }
            int i = this.getTextRenderer().getWidth(mutableText);
            int j = (this.scaledWidth - i) / 2;
            int k = this.scaledHeight - 89;
            if (!this.client.interactionManager.hasStatusBars()) {
                k += 14;
            }
            if ((l = (int)((float)this.heldItemTooltipFade * 256.0f / 10.0f)) > 255) {
                l = 255;
            }
            if (l > 0) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                InGameHud.fill(matrices, j - 2, k - 2, j + i + 2, k + this.getTextRenderer().fontHeight + 2, this.client.options.getTextBackgroundColor(0));
                this.getTextRenderer().drawWithShadow(matrices, mutableText, (float)j, (float)k, 0xFFFFFF + (l << 24));
                RenderSystem.disableBlend();
            }
        }
        this.client.getProfiler().pop();
    }
}
