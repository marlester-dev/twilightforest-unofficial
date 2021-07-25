package twilightforest.structures.finalcastle;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import twilightforest.TFFeature;
import twilightforest.block.TFBlocks;
import twilightforest.structures.TFStructureComponentOld;
import twilightforest.util.RotationUtil;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class FinalCastleFoundation13Component extends TFStructureComponentOld {

	protected int groundLevel = -1;

	public FinalCastleFoundation13Component(StructureManager manager, CompoundTag nbt) {
		this(FinalCastlePieces.TFFCToF13, nbt);
	}

	public FinalCastleFoundation13Component(StructurePieceType piece, CompoundTag nbt) {
		super(piece, nbt);
	}

	public FinalCastleFoundation13Component(StructurePieceType type, TFFeature feature, Random rand, int i, TFStructureComponentOld sideTower) {
		super(type, feature, i);

		this.setOrientation(sideTower.getOrientation());
		this.boundingBox = new BoundingBox(sideTower.getBoundingBox().x0 - 2, sideTower.getBoundingBox().y0 - 1, sideTower.getBoundingBox().z0 - 2, sideTower.getBoundingBox().x1 + 2, sideTower.getBoundingBox().y0, sideTower.getBoundingBox().z1 + 2);
	}

	@Override
	public void addChildren(StructurePiece parent, List<StructurePiece> list, Random rand) {
		if (parent instanceof TFStructureComponentOld) {
			this.deco = ((TFStructureComponentOld) parent).deco;
		}
	}

	@Override
	public boolean postProcess(WorldGenLevel world, StructureFeatureManager manager, ChunkGenerator generator, Random randomIn, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		// offset bounding box to average ground level
		if (this.groundLevel < 0) {
			this.groundLevel = this.findGroundLevel(world, sbb, 150, isDeadrock); // is 150 a good place to start? :)

			if (this.groundLevel < 0) {
				return true;
			}
		}

		// how tall are we
		int height = this.boundingBox.y1 - this.groundLevel;
		int mid = height / 2;

		// assume square
		int size = this.boundingBox.x1 - this.boundingBox.x0;

		for (Rotation rotation : RotationUtil.ROTATIONS) {
			// do corner
			this.replaceAirAndLiquidDownwardsRotated(world, deco.blockState, 1, -1, 1, rotation, sbb);
			this.replaceAirAndLiquidDownwardsRotated(world, deco.blockState, 2, -1, 1, rotation, sbb);
			this.replaceAirAndLiquidDownwardsRotated(world, deco.blockState, 2, -mid, 0, rotation, sbb);
			this.replaceAirAndLiquidDownwardsRotated(world, deco.blockState, 1, -1, 2, rotation, sbb);
			this.replaceAirAndLiquidDownwardsRotated(world, deco.blockState, 0, -mid, 2, rotation, sbb);

			for (int x = 6; x < (size - 3); x += 4) {
				this.replaceAirAndLiquidDownwardsRotated(world, deco.blockState, x, -1, 1, rotation, sbb);
				this.replaceAirAndLiquidDownwardsRotated(world, deco.blockState, x, -mid, 0, rotation, sbb);
			}
		}

		return true;
	}

	protected static final Predicate<BlockState> isDeadrock = state -> state.getBlock() == TFBlocks.deadrock.get();
}
