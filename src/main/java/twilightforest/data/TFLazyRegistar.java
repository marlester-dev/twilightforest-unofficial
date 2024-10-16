package twilightforest.data;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class TFLazyRegistar {
  public final String modId;
  private final ResourceKey<? extends Registry<?>> registryKey;
  public TFLazyRegistar(ResourceKey<? extends Registry<?>> registryKey, String modid) {
    this.registryKey = registryKey;
    this.modId = modid;
  }
  public static TFLazyRegistar create(ResourceKey<? extends Registry<?>> registryKey, String modid) {
    return new TFLazyRegistar(registryKey, modid);
  }
  public void register() {
    FabricRegistryBuilder.createSimple(null, registryKey.location()).buildAndRegister();
  }
}
