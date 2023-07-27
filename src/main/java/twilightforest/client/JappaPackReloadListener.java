package twilightforest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import twilightforest.TwilightForestMod;

import java.util.function.BooleanSupplier;

//TODO
// I would like to look at migrating the models to using EntityModelJson (https://www.curseforge.com/minecraft/mc-mods/entity-model-json) in the future.
// we can make the pack depend on it to load the new models instead of having them hardcoded here.
// could also shade the mod since I dont trust people to actually download the mod. I can already see the bug reports flooding in, yikes
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = TwilightForestMod.ID)
public class JappaPackReloadListener implements ResourceManagerReloadListener {

	private static boolean jappaPackLoaded = false;
	public static final JappaPackReloadListener INSTANCE = new JappaPackReloadListener();

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		jappaPackLoaded = Minecraft.getInstance().getResourcePackRepository().getSelectedPacks().stream().anyMatch(pack -> pack.open().getResource(PackType.CLIENT_RESOURCES, TwilightForestMod.prefix("jappa_models.marker")) != null);
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		jappaPackLoaded = Minecraft.getInstance().getResourcePackRepository().getSelectedPacks().stream().anyMatch(pack -> pack.open().getResource(PackType.CLIENT_RESOURCES, TwilightForestMod.prefix("jappa_models.marker")) != null);
	}

	public boolean isJappaPackLoaded() {
		return jappaPackLoaded;
	}

	//Avoid using this. Its needed for entity models only due to reload ordering.
	public BooleanSupplier uncachedJappaPackCheck() {
		return () -> Minecraft.getInstance().getResourcePackRepository().getSelectedPacks().stream().anyMatch(pack -> pack.open().getResource(PackType.CLIENT_RESOURCES, TwilightForestMod.prefix("jappa_models.marker")) != null);
	}
}
