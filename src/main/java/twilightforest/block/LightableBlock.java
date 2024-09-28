package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFParticleType;

import java.util.Locale;

//all the common lighting/extinguishing methods for the candelabra and skull candles are here to reduce clutter
//it may also be handy if we decide to add more candle-based blocks in the future
public interface LightableBlock {

	EnumProperty<Lighting> LIGHTING = EnumProperty.create("lighting", Lighting.class);


	default InteractionResult lightCandles(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		if (player.getAbilities().mayBuild && player.getItemInHand(hand).isEmpty() && state.getValue(LIGHTING) != Lighting.NONE) {
			extinguish(player, state, level, pos);
			return InteractionResult.sidedSuccess(level.isClientSide());

		} else if (this.canBeLit(state)) {
			if (player.getItemInHand(hand).is(Items.FLINT_AND_STEEL)) {
				setLit(level, state, pos, true);
				level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (!player.getAbilities().instabuild)
					player.getItemInHand(hand).hurtAndBreak(1, player, (res) -> res.broadcastBreakEvent(hand));
				return InteractionResult.sidedSuccess(level.isClientSide());
			} else if (player.getItemInHand(hand).is(Items.FIRE_CHARGE)) {
				setLit(level, state, pos, true);
				level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
				return InteractionResult.sidedSuccess(level.isClientSide());
			}
		}
		return InteractionResult.PASS;
	}

	default void lightCandlesWithProjectile(Level level, BlockState state, BlockHitResult result, Projectile projectile) {
		if (!level.isClientSide() && projectile.isOnFire() && this.canBeLit(state)) {
			this.setLit(level, state, result.getBlockPos(), true);
		}
	}

	default boolean canBeLit(BlockState state) {
		return state.getValue(LIGHTING) == Lighting.NONE;
	}

	Iterable<Vec3> getParticleOffsets(BlockState state, LevelAccessor level, BlockPos pos);

	// Original methods used Vec3 but here we can avoid creation of extraneous vectors
	default void addParticlesAndSound(Level level, BlockPos pos, double xFraction, double yFraction, double zFraction, RandomSource rand, boolean ominous) {
		addParticlesAndSound(level, pos.getX() + xFraction, pos.getY() + yFraction, pos.getZ() + zFraction, rand, ominous);
	}

	default void addParticlesAndSound(Level level, double x, double y, double z, RandomSource rand, boolean ominous) {
		float var3 = rand.nextFloat();
		if (var3 < 0.3F) {
			if (!ominous) level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
			if (var3 < 0.17F) {
				level.playLocalSound(x + 0.5D, y + 0.5D, z + 0.5D, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
			}
		}

		level.addParticle(ominous ? TFParticleType.OMINOUS_FLAME.get() : ParticleTypes.SMALL_FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
	}

	//still, we should include the vector method because im too lazy to convert :P
	default void addParticlesAndSound(Level level, Vec3 vec, RandomSource rand, boolean ominous) {
		float var3 = rand.nextFloat();
		if (var3 < 0.3F) {
			if (!ominous) level.addParticle(ParticleTypes.SMOKE, vec.x(), vec.y(), vec.z(), 0.0D, 0.0D, 0.0D);
			if (var3 < 0.17F) {
				level.playLocalSound(vec.x() + 0.5D, vec.y() + 0.5D, vec.z() + 0.5D, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
			}
		}

		level.addParticle(ominous ? TFParticleType.OMINOUS_FLAME.get() : ParticleTypes.SMALL_FLAME, vec.x, vec.y, vec.z, 0.0D, 0.0D, 0.0D);
	}

	default void extinguish(@Nullable Player player, BlockState state, LevelAccessor accessor, BlockPos pos) {
		setLit(accessor, state, pos, false);

		if (state.getBlock() instanceof LightableBlock lightableBlock) {
			lightableBlock.getParticleOffsets(state, accessor, pos).forEach((vec3) ->
					accessor.addParticle(ParticleTypes.SMOKE, (double) pos.getX() + vec3.x, (double) pos.getY() + vec3.y, (double) pos.getZ() + vec3.z, 0.0D, 0.025D, 0.0D));
		}

		accessor.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
		accessor.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	default void setLit(LevelAccessor accessor, BlockState state, BlockPos pos, boolean lit) {
		accessor.setBlock(pos, state.setValue(LIGHTING, lit ? Lighting.NORMAL : Lighting.NONE), 11);
	}

	enum Lighting implements StringRepresentable {
		NONE,
		NORMAL,
		OMINOUS;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}
