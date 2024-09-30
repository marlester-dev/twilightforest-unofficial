package twilightforest.block;

import io.github.fabricators_of_create.porting_lib.enchant.EnchantmentBonusBlock;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BookshelfBlock extends Block implements EnchantmentBonusBlock {

	public BookshelfBlock(Properties properties) {
		super(properties);
		FlammableBlockRegistry.getDefaultInstance().add(this, getFireSpreadSpeed(), getFlammability());
	}

	@Override
	public float getEnchantPowerBonus(BlockState state, LevelReader reader, BlockPos pos) {
		return 1.0F;
	}

	public int getFlammability() {
		return 20;
	}

	public int getFireSpreadSpeed() {
		return 30;
	}
}
