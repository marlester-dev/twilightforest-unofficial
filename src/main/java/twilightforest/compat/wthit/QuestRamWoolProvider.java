package twilightforest.compat.wthit;

import com.google.common.collect.Iterables;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemListComponent;
import net.minecraft.world.item.ItemStack;
import twilightforest.entity.passive.QuestRam;
import twilightforest.util.ColorUtil;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public enum QuestRamWoolProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendBody(ITooltip tooltip, IEntityAccessor entityAccessor, IPluginConfig config) {
		if (entityAccessor.getEntity() instanceof QuestRam ram) {
			ArrayList<ArrayList<ItemStack>> woolArrayListsArrayList = new ArrayList<>();
			AtomicInteger woolCount = new AtomicInteger(0);
			ColorUtil.WOOL_TO_DYE_IN_RAM_ORDER.forEach((color, block) -> {
				if (!ram.isColorPresent(color)) {
					int originalWoolCount = woolCount.getAndIncrement();
					ItemStack woolItemStack = new ItemStack(block);

					ArrayList<ItemStack> lastWoolList;
					if (woolArrayListsArrayList.isEmpty() || originalWoolCount % 8 == 0) {
						lastWoolList = new ArrayList<>();
						lastWoolList.add(woolItemStack);
						woolArrayListsArrayList.add(lastWoolList);
					} else {
						lastWoolList = Iterables.getLast(woolArrayListsArrayList);
						lastWoolList.add(woolItemStack);
					}
				}
			});
			for (ArrayList<ItemStack> woolArrayList : woolArrayListsArrayList) {
				tooltip.addLine(new ItemListComponent(woolArrayList, 1,  0.5F));
			}
		}
	}
}
