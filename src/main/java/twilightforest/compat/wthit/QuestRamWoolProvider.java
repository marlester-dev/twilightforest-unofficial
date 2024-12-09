package twilightforest.compat.wthit;

import com.google.common.collect.Iterables;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemListComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import twilightforest.entity.passive.QuestRam;
import twilightforest.util.ColorUtil;

import java.util.ArrayList;
import java.util.Map;

public enum QuestRamWoolProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendBody(ITooltip tooltip, IEntityAccessor entityAccessor, IPluginConfig config) {
		if (entityAccessor.getEntity() instanceof QuestRam ram) {
			ArrayList<ArrayList<ItemStack>> woolArrayListsArrayList = new ArrayList<>();
			int woolCount = 0;
			for (Map.Entry<DyeColor, Block> entry : ColorUtil.WOOL_TO_DYE_IN_RAM_ORDER.entrySet()) {
				if (!ram.isColorPresent(entry.getKey())) {
					ItemStack woolItemStack = new ItemStack(entry.getValue());

					ArrayList<ItemStack> lastWoolList;
					if (woolArrayListsArrayList.isEmpty() || woolCount % 8 == 0) {
						lastWoolList = new ArrayList<>();
						lastWoolList.add(woolItemStack);
						woolArrayListsArrayList.add(lastWoolList);
					} else {
						lastWoolList = Iterables.getLast(woolArrayListsArrayList);
						lastWoolList.add(woolItemStack);
					}
					woolCount++;
				}
			}
			for (ArrayList<ItemStack> woolArrayList : woolArrayListsArrayList) {
				tooltip.addLine(new ItemListComponent(woolArrayList, 1,  0.5F));
			}
		}
	}
}
