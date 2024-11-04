package twilightforest.compat.wthit;

import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemListComponent;
import net.minecraft.world.item.ItemStack;
import twilightforest.entity.passive.QuestRam;
import twilightforest.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public enum QuestRamWoolProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendBody(ITooltip tooltip, IEntityAccessor entityAccessor, IPluginConfig config) {
		if (entityAccessor.getEntity() instanceof QuestRam ram) {
			List<ItemStack> items = new ArrayList<>();
			ColorUtil.WOOL_TO_DYE_IN_RAM_ORDER.forEach((color, block) -> {
				if (!ram.isColorPresent(color)) {
					items.add(new ItemStack(block));
				}
			});
			ItemListComponent itemListComponent = new ItemListComponent(items, 2);
			tooltip.addLine(itemListComponent);
		}
	}
}
