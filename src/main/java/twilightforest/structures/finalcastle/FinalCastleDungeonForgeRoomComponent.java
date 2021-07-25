package twilightforest.structures.finalcastle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import twilightforest.TFFeature;
import twilightforest.structures.TFStructureComponentOld;

import java.util.Random;

public class FinalCastleDungeonForgeRoomComponent extends TFStructureComponentOld {

	public FinalCastleDungeonForgeRoomComponent(StructureManager manager, CompoundTag nbt) {
		super(FinalCastlePieces.TFFCDunBoR, nbt);
	}

	public FinalCastleDungeonForgeRoomComponent(TFFeature feature, Random rand, int i, int x, int y, int z, Direction direction) {
		super(FinalCastlePieces.TFFCDunBoR, feature, i);
		this.spawnListIndex = 3; // forge monsters
		this.setOrientation(direction);
		this.boundingBox = TFStructureComponentOld.getComponentToAddBoundingBox2(x, y, z, -15, 0, -15, 50, 30, 50, direction);
	}

	@Override
	public boolean postProcess(WorldGenLevel world, StructureFeatureManager manager, ChunkGenerator generator, Random rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		this.generateAirBox(world, sbb, 0, 0, 0, 50, 30, 50);

		// sign
		this.placeSignAtCurrentPosition(world, 25, 0, 25, "Mini-boss 2", "Gives talisman", sbb);

		return true;
	}
}
