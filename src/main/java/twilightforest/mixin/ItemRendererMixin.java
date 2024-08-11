package twilightforest.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.renderer.GiantItemRenderHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "renderQuadList", at = @At("HEAD"))
    private void startRenderItem(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack itemStack, int combinedLight, int combinedOverlay, CallbackInfo ci) {
        poseStack.pushPose();
        GiantItemRenderHelper.handle(poseStack, itemStack);
    }

    @Inject(method = "renderQuadList", at = @At("RETURN"))
    private void endRenderItem(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack itemStack, int combinedLight, int combinedOverlay, CallbackInfo ci) {
        poseStack.popPose();
    }
}
