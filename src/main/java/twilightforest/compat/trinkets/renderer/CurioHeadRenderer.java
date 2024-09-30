package twilightforest.compat.trinkets.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.emi.trinkets.api.SlotReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CurioHeadRenderer {

	public static void render(ItemStack stack, SlotReference slotContext, EntityModel<? extends LivingEntity> contextModel, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		if (contextModel instanceof HeadedModel headModel) {
			matrixStack.pushPose();
			headModel.getHead().translateAndRotate(matrixStack);
			matrixStack.translate(0.0D, -0.25D, 0.0D);
			matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			matrixStack.scale(0.625F, -0.625F, -0.625F);
			ItemInHandRenderer renderer = new ItemInHandRenderer(Minecraft.getInstance(), Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getItemRenderer());
			renderer.renderItem(entity, stack, ItemDisplayContext.HEAD, false, matrixStack, renderTypeBuffer, light);
			matrixStack.popPose();
		}
	}
}
