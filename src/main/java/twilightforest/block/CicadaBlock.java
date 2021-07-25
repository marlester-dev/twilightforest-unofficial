package twilightforest.block;

import net.minecraft.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import twilightforest.TwilightForestMod;
import twilightforest.tileentity.TFTileEntities;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour;

public class CicadaBlock extends CritterBlock {

	protected CicadaBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Nullable
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return TFTileEntities.CICADA.get().create();
	}

	@Override
	public ItemStack getSquishResult() {
		return new ItemStack(Items.GRAY_DYE, 1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter world, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, world, tooltip, flag);

		if (ModList.get().isLoaded("immersiveengineering")) {
			tooltip.add(new TranslatableComponent("block.twilightforest.cicada.desc")
					.withStyle(TwilightForestMod.getRarity().color)
					.withStyle(ChatFormatting.ITALIC));
		}
	}
}
