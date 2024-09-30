package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFBlocks;

public class WroughtIronFenceBlock extends Block implements SimpleWaterloggedBlock {

	public static final BooleanProperty POST = BooleanProperty.create("post");
	public static final EnumProperty<FenceSide> EAST_FENCE = EnumProperty.create("east_fence", FenceSide.class);
	public static final EnumProperty<FenceSide> NORTH_FENCE = EnumProperty.create("north_fence", FenceSide.class);
	public static final EnumProperty<FenceSide> SOUTH_FENCE = EnumProperty.create("south_fence", FenceSide.class);
	public static final EnumProperty<FenceSide> WEST_FENCE = EnumProperty.create("west_fence", FenceSide.class);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShape POST_SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);
	private static final VoxelShape NORTH_SHAPE = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 8.0D);
	private static final VoxelShape SOUTH_SHAPE = Block.box(7.0D, 0.0D, 8.0D, 9.0D, 16.0D, 16.0D);
	private static final VoxelShape EAST_SHAPE = Block.box(8.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);
	private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 0.0D, 7.0D, 8.0D, 16.0D, 9.0D);

	public WroughtIronFenceBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.getStateDefinition().any().setValue(POST, true).setValue(EAST_FENCE, FenceSide.NONE).setValue(NORTH_FENCE, FenceSide.NONE).setValue(SOUTH_FENCE, FenceSide.NONE).setValue(WEST_FENCE, FenceSide.NONE).setValue(WATERLOGGED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
		VoxelShape finalShape = Shapes.empty();
		if (state.getValue(POST)) {
			finalShape = POST_SHAPE;
		}
		if (state.getValue(NORTH_FENCE) != FenceSide.NONE) {
			finalShape = Shapes.or(finalShape, NORTH_SHAPE);
		}
		if (state.getValue(SOUTH_FENCE) != FenceSide.NONE) {
			finalShape = Shapes.or(finalShape, SOUTH_SHAPE);
		}
		if (state.getValue(WEST_FENCE) != FenceSide.NONE) {
			finalShape = Shapes.or(finalShape, WEST_SHAPE);
		}
		if (state.getValue(EAST_FENCE) != FenceSide.NONE) {
			finalShape = Shapes.or(finalShape, EAST_SHAPE);
		}
		//make it a full block if for some reason theres not a single piece of fence showing. That way people can still interact with the block
		if (finalShape.isEmpty()) finalShape = Shapes.block();
		return finalShape;
	}

	@Override
	@Deprecated
	public boolean isPathfindable(BlockState state, BlockGetter getter, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		LevelReader level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		//Sides
		BlockState north = level.getBlockState(pos.north());
		BlockState east = level.getBlockState(pos.east());
		BlockState south = level.getBlockState(pos.south());
		BlockState west = level.getBlockState(pos.west());
		BlockState above = level.getBlockState(pos.above());
		boolean northFace = this.connectsTo(north, north.isFaceSturdy(level, pos.north(), Direction.SOUTH), Direction.SOUTH);
		boolean eastFace = this.connectsTo(east, east.isFaceSturdy(level, pos.east(), Direction.WEST), Direction.WEST);
		boolean southFace = this.connectsTo(south, south.isFaceSturdy(level, pos.south(), Direction.NORTH), Direction.NORTH);
		boolean westFace = this.connectsTo(west, west.isFaceSturdy(level, pos.west(), Direction.EAST), Direction.EAST);
		//Waterlogged
		FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
		BlockState state = this.defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
		return this.fenceShape(level, state, pos.above(), above, northFace, eastFace, southFace, westFace);
	}

	private boolean connectsTo(BlockState state, boolean solid, Direction direction) {
		return state.is(this) || !isExceptionForConnection(state) && solid;
	}

	@Override
	@Deprecated
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		if (state.getValue(WATERLOGGED)) {
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		return direction == Direction.UP ? this.updateTop(level, state, neighborPos, neighbor) : this.updateSide(level, pos, state, neighborPos, neighbor, direction);
	}

	private BlockState updateSide(LevelReader level, BlockPos firstPos, BlockState firstState, BlockPos secondPos, BlockState secondState, Direction direction) {
		Direction opposite = direction.getOpposite();
		boolean north = direction == Direction.NORTH ? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, opposite), opposite) : isConnected(firstState, NORTH_FENCE);
		boolean east = direction == Direction.EAST ? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, opposite), opposite) : isConnected(firstState, EAST_FENCE);
		boolean south = direction == Direction.SOUTH ? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, opposite), opposite) : isConnected(firstState, SOUTH_FENCE);
		boolean west = direction == Direction.WEST ? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, opposite), opposite) : isConnected(firstState, WEST_FENCE);
		BlockPos above = firstPos.above();
		return this.fenceShape(level, firstState, above, level.getBlockState(above), north, east, south, west);
	}

	private BlockState updateTop(LevelReader level, BlockState state, BlockPos pos, BlockState facing) {
		boolean north = isConnected(state, NORTH_FENCE);
		boolean east = isConnected(state, EAST_FENCE);
		boolean south = isConnected(state, SOUTH_FENCE);
		boolean west = isConnected(state, WEST_FENCE);
		return this.fenceShape(level, state, pos, facing, north, east, south, west);
	}

	private static boolean isConnected(BlockState state, Property<FenceSide> side) {
		return state.getValue(side) != FenceSide.NONE;
	}

	private BlockState fenceShape(LevelReader level, BlockState state, BlockPos pos, BlockState neighbor, boolean north, boolean east, boolean south, boolean west) {
		//huh?
		BlockState above = level.getBlockState(pos);
		BlockState below = level.getBlockState(pos.below(2));
		BlockState blockstate = this.updateSides(state, above, below, north, east, south, west);
		return blockstate.setValue(POST, makePost(blockstate, neighbor));
	}

	private boolean makePost(BlockState state, BlockState neighbor) {
		boolean flag = neighbor.is(this) && neighbor.getValue(POST) || neighbor.is(TFBlocks.WROUGHT_IRON_FINIAL.get());
		if (flag) {
			return true;
		} else {
			//get sides
			FenceSide nSide = state.getValue(NORTH_FENCE);
			FenceSide sSide = state.getValue(SOUTH_FENCE);
			FenceSide eSide = state.getValue(EAST_FENCE);
			FenceSide wSide = state.getValue(WEST_FENCE);
			boolean north = nSide == FenceSide.NONE;
			boolean south = sSide == FenceSide.NONE;
			boolean east = eSide == FenceSide.NONE;
			boolean west = wSide == FenceSide.NONE;

			return north && south && east && west || north != south || east != west;
		}
	}

	private BlockState updateSides(BlockState state, BlockState above, BlockState below, boolean north, boolean east, boolean south, boolean west) {
		return state.setValue(NORTH_FENCE, makeFenceState(north, NORTH_FENCE, above, below))
				.setValue(EAST_FENCE, makeFenceState(east, EAST_FENCE, above, below))
				.setValue(SOUTH_FENCE, makeFenceState(south, SOUTH_FENCE, above, below))
				.setValue(WEST_FENCE, makeFenceState(west, WEST_FENCE, above, below));
	}

	private FenceSide makeFenceState(boolean connect, EnumProperty<FenceSide> property, BlockState above, BlockState below) {
		boolean flagA = false;
		boolean flagB = false;

		if (above.is(this)) {
			if (above.getValue(property) != FenceSide.NONE) {
				flagA = true;
			}
		}
		if (below.is(this)) {
			if (below.getValue(property) != FenceSide.NONE) {
				flagB = true;
			}
		}
		if (connect) {
			if (flagA && flagB) return FenceSide.MIDDLE;
			if (!flagA && flagB) return FenceSide.TOP;
			if (flagA && !flagB) return FenceSide.BOTTOM;
			return FenceSide.FULL;
		} else {
			return FenceSide.NONE;
		}
	}

	@Override
	@Deprecated
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return !pState.getValue(WATERLOGGED);
	}

	@Override
	@Deprecated
	public BlockState rotate(BlockState state, Rotation rotation) {
		return switch (rotation) {
			case CLOCKWISE_180 ->
					state.setValue(NORTH_FENCE, state.getValue(SOUTH_FENCE)).setValue(EAST_FENCE, state.getValue(WEST_FENCE)).setValue(SOUTH_FENCE, state.getValue(NORTH_FENCE)).setValue(WEST_FENCE, state.getValue(WEST_FENCE));
			case COUNTERCLOCKWISE_90 ->
					state.setValue(NORTH_FENCE, state.getValue(EAST_FENCE)).setValue(EAST_FENCE, state.getValue(SOUTH_FENCE)).setValue(SOUTH_FENCE, state.getValue(WEST_FENCE)).setValue(WEST_FENCE, state.getValue(NORTH_FENCE));
			case CLOCKWISE_90 ->
					state.setValue(NORTH_FENCE, state.getValue(WEST_FENCE)).setValue(EAST_FENCE, state.getValue(NORTH_FENCE)).setValue(SOUTH_FENCE, state.getValue(EAST_FENCE)).setValue(WEST_FENCE, state.getValue(SOUTH_FENCE));
			default -> state;
		};
	}

	@Override
	@Deprecated
	public BlockState mirror(BlockState state, Mirror mirror) {
		return switch (mirror) {
			case LEFT_RIGHT ->
					state.setValue(NORTH_FENCE, state.getValue(SOUTH_FENCE)).setValue(SOUTH_FENCE, state.getValue(NORTH_FENCE));
			case FRONT_BACK ->
					state.setValue(EAST_FENCE, state.getValue(WEST_FENCE)).setValue(WEST_FENCE, state.getValue(WEST_FENCE));
			default -> super.mirror(state, mirror);
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POST, NORTH_FENCE, EAST_FENCE, WEST_FENCE, SOUTH_FENCE, WATERLOGGED);
	}

	public enum FenceSide implements StringRepresentable {
		NONE("none"),
		MIDDLE("middle"),
		BOTTOM("bottom"),
		TOP("top"),
		FULL("full");

		private final String name;

		FenceSide(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.getSerializedName();
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
