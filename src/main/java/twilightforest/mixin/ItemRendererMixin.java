package twilightforest.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.TwilightForestMod;
import twilightforest.client.renderer.GiantItemRenderHelper;
import twilightforest.init.TFItems;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
	@Shadow @Final private ItemModelShaper itemModelShaper;

	@Inject(method = "renderQuadList", at = @At("HEAD"))
	private void startRenderItem(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack itemStack, int combinedLight, int combinedOverlay, CallbackInfo ci) {
		poseStack.pushPose();
		GiantItemRenderHelper.handle(poseStack, itemStack);
	}

	@Inject(method = "renderQuadList", at = @At("RETURN"))
	private void endRenderItem(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack itemStack, int combinedLight, int combinedOverlay, CallbackInfo ci) {
		poseStack.popPose();
	}

	@ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
	private BakedModel render(BakedModel value, ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (!(displayContext == ItemDisplayContext.GUI)) {
			return value;
		}
		boolean sword;
		if (itemStack.is(TFItems.GIANT_SWORD.get())) {
			sword = true;
		} else if (itemStack.is(TFItems.GIANT_PICKAXE.get())) {
			sword = false;
		} else {
			return value;
		}
		String path;
		if (sword) {
			path = "giant_sword_gui";
		} else {
			path = "giant_pickaxe_gui";
		}
		return itemModelShaper.getModelManager().getModel(new ModelResourceLocation(TwilightForestMod.ID, path, "inventory"));
	}
}
