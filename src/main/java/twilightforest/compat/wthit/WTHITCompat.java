package twilightforest.compat.wthit;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import twilightforest.entity.passive.QuestRam;

public class WTHITCompat implements IWailaPlugin {

	@Override
	public void register(IRegistrar registrar) {
		registrar.body(QuestRamWoolProvider.INSTANCE, QuestRam.class);
	}
}