package twilightforest.client.model.block.doors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;

public class NewCastleDoorModelLoader implements IGeometryLoader<NewUnbakedCastleDoorModel> {
	public static final NewCastleDoorModelLoader INSTANCE = new NewCastleDoorModelLoader();
	public static final ResourceLocation ID = new ResourceLocation(TwilightForestMod.ID, "castle_door");

	public NewCastleDoorModelLoader() {}

	@Override
	public NewUnbakedCastleDoorModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
		return new NewUnbakedCastleDoorModel();
	}
}
