package twilightforest.client.model.block.forcefield;

import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Function;

public record UnbakedForceFieldModel(
		Map<BlockElement, ForceFieldModelLoader.Condition> elementsAndConditions) implements IUnbakedGeometry<UnbakedForceFieldModel> {

	@Override
	public BakedModel bake(BlockModel context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation, boolean isGui3d) {
		return new ForceFieldModel(this.elementsAndConditions, spriteGetter, context, overrides);
	}
}
