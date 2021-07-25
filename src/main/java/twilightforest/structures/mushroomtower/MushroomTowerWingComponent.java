package twilightforest.structures.mushroomtower;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
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
import twilightforest.TwilightForestMod;
import twilightforest.structures.TFStructureComponentOld;
import twilightforest.structures.lichtower.TowerRoofComponent;
import twilightforest.structures.lichtower.TowerWingComponent;
import twilightforest.util.RotationUtil;

import java.util.List;
import java.util.Random;

public class MushroomTowerWingComponent extends TowerWingComponent {

	private static final int RANGE = 200;
	protected static final int FLOOR_HEIGHT = 4;
	protected static final int MAIN_SIZE = 15;

	boolean hasBase = false;
	public boolean isAscender = false;

	public MushroomTowerWingComponent(StructureManager manager, CompoundTag nbt) {
		this(MushroomTowerPieces.TFMTWin, nbt);
	}

	public MushroomTowerWingComponent(StructurePieceType piece, CompoundTag nbt) {
		super(piece, nbt);
		this.hasBase = nbt.getBoolean("hasBase");
		this.isAscender = nbt.getBoolean("isAscender");
	}

	protected MushroomTowerWingComponent(StructurePieceType piece, TFFeature feature, int i, int x, int y, int z, int pSize, int pHeight, Direction direction) {
		super(piece, feature, i, x, y, z, pSize, pHeight, direction);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tagCompound) {
		super.addAdditionalSaveData(tagCompound);

		tagCompound.putBoolean("hasBase", this.hasBase);
		tagCompound.putBoolean("isAscender", this.isAscender);
	}

	@Override
	public void addChildren(StructurePiece parent, List<StructurePiece> list, Random rand) {
		if (parent != null && parent instanceof TFStructureComponentOld) {
			this.deco = ((TFStructureComponentOld) parent).deco;
		}

		// we should have a door where we started
		addOpening(0, 1, size / 2, Rotation.CLOCKWISE_180);

		// should we build a base?
		this.hasBase = size > 3;

		// if we are an ascender, build the destination tower
		if (this.isAscender) {

			int[] dest = getValidOpening(rand, Rotation.CLOCKWISE_180);
			dest[1] = this.height - 3;
			int childHeight = (rand.nextInt(3) + rand.nextInt(3) + 2) * FLOOR_HEIGHT + 1;
			boolean madeIt = makeMainBridge(list, rand, this.getGenDepth() + 1, dest[0], dest[1], dest[2], childHeight, Rotation.CLOCKWISE_180);

			if (!madeIt) {
				TwilightForestMod.LOGGER.info("Did not make bridge back to new main");
			} else {
				TwilightForestMod.LOGGER.debug("Made bridge back to new main");
			}
		}

		// limit sprawl to a reasonable amount
		if (this.getGenDepth() < 5 && this.size > 6) {
			// make sub towers
			//for (int i = 0; i < 4; i++) {
			for (Rotation i : RotationUtil.ROTATIONS) {

				if (this.size < MAIN_SIZE && i == Rotation.CLOCKWISE_180) {
					continue;
				}

				int[] dest = getValidOpening(rand, i);

				int childHeight = (rand.nextInt(2) + rand.nextInt(2) + 2) * FLOOR_HEIGHT + 1;

				makeBridge(list, rand, this.getGenDepth() + 1, dest[0], dest[1], dest[2], size - 4, childHeight, i);
			}
		}

		if (this.isHighest(this.boundingBox, this.size, list) || !this.hasBase) {
			// add a roof?
			makeARoof(parent, list, rand);
		}
	}

	/**
	 * Have we strayed more than range blocks away from the center?
	 */
	private boolean isOutOfRange(StructurePiece parent, int nx, int nz, int range) {
		final BoundingBox sbb = parent.getBoundingBox();
		final int centerX = sbb.x0 + (sbb.x1 - sbb.x0 + 1) / 2;
		final int centerZ = sbb.z0 + (sbb.z1 - sbb.z0 + 1) / 2;
		return Math.abs(nx - centerX) > range
				|| Math.abs(nz - centerZ) > range;
	}

	/**
	 * Make a new wing
	 */
	@Override
	public boolean makeTowerWing(List<StructurePiece> list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, Rotation rotation) {

		Direction direction = getStructureRelativeRotation(rotation);
		int[] dx = offsetTowerCoords(x, y, z, wingSize, direction);

		// stop if out of range
		if (isOutOfRange(list.get(0), dx[0], dx[2], RANGE)) {
			return false;
		}

		// adjust coordinates to fit an existing tower
		if (wingSize > 3) {
			dx = adjustCoordinates(dx[0], dx[1], dx[2], wingSize, direction, list);
		}

		MushroomTowerWingComponent wing = new MushroomTowerWingComponent(MushroomTowerPieces.TFMTWin, getFeatureType(), index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
		// check to see if it intersects something already there
		StructurePiece intersect = StructurePiece.findCollisionPiece(list, wing.getBoundingBox());
		if (intersect == null || intersect == this || intersect instanceof TowerRoofMushroomComponent) {

			// if we are coming from an ascender bridge, mark the destination component
			if (this instanceof MushroomTowerBridgeComponent && this.isAscender) {
				wing.isAscender = true;
			}

			list.add(wing);
			wing.addChildren(list.get(0), list, rand);
			addOpening(x, y, z, rotation);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Adjust the coordinates for this tower to link up with any others within 3
	 */
	protected int[] adjustCoordinates(int x, int y, int z, int wingSize, Direction direction, List<StructurePiece> list) {

		// go through list.  if there are any same size towers within wingSize, return their xyz instead

		for (Object obj : list) {
			if (obj instanceof TowerWingComponent && !(obj instanceof MushroomTowerBridgeComponent)) {
				TowerWingComponent otherWing = (TowerWingComponent) obj;

				if (wingSize == otherWing.size && otherWing.getBoundingBox().intersects(x - 3, z - 3, x + 3, z + 3)) {
					switch (direction) {
						case SOUTH:
							return new int[]{otherWing.getBoundingBox().x0, y, otherWing.getBoundingBox().z0};
						case WEST:
							return new int[]{otherWing.getBoundingBox().x1, y, otherWing.getBoundingBox().z0};
						case NORTH:
							return new int[]{otherWing.getBoundingBox().x1, y, otherWing.getBoundingBox().z1};
						case EAST:
							return new int[]{otherWing.getBoundingBox().x0, y, otherWing.getBoundingBox().z1};
						default:
							return new int[]{x, y, z};
					}
				}
			}
		}

		return new int[]{x, y, z};
	}

	/**
	 * Are there (not) any other towers below this bounding box?
	 */
	private boolean isHighest(BoundingBox boundingBox, int size, List<StructurePiece> list) {
		// go through list.  if there are any same size towers within wingSize, return their xyz instead

		BoundingBox boxAbove = new BoundingBox(boundingBox);

		boxAbove.y1 = 256;

		for (Object obj : list) {
			if (this != obj && obj instanceof TowerWingComponent && !(obj instanceof MushroomTowerBridgeComponent)) {
				TowerWingComponent otherWing = (TowerWingComponent) obj;

				if (size == otherWing.size && otherWing.getBoundingBox().intersects(boxAbove)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Make a mushroom roof!
	 */
	@Override
	public void makeARoof(StructurePiece parent, List<StructurePiece> list, Random rand) {

		TowerRoofComponent roof = new TowerRoofMushroomComponent(getFeatureType(), this.getGenDepth() + 1, this, 1.6F);
		if (!(StructurePiece.findCollisionPiece(list, roof.getBoundingBox()) instanceof TowerRoofMushroomComponent)) {
			roof = new TowerRoofMushroomComponent(getFeatureType(), this.getGenDepth() + 1, this, 1.0F);
			if (!(StructurePiece.findCollisionPiece(list, roof.getBoundingBox()) instanceof TowerRoofMushroomComponent)) {
				roof = new TowerRoofMushroomComponent(getFeatureType(), this.getGenDepth() + 1, this, 0.6F);
			}
		}
		list.add(roof);
		roof.addChildren(this, list, rand);
	}

	@Override
	protected boolean makeBridge(List<StructurePiece> list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, Rotation rotation) {
		return this.makeBridge(list, rand, index, x, y, z, wingSize, wingHeight, rotation, false);
	}

	protected boolean makeBridge(List<StructurePiece> list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, Rotation rotation, boolean ascender) {
		// bridges are size  always
		Direction direction = getStructureRelativeRotation(rotation);
		int[] dx = offsetTowerCoords(x, y, z, 3, direction);

		// adjust height for those stupid little things
		if (wingSize == 3) {
			wingHeight = 4;
		}

		MushroomTowerBridgeComponent bridge = new MushroomTowerBridgeComponent(MushroomTowerPieces.TFMTBri, getFeatureType(), index, dx[0], dx[1], dx[2], wingSize, wingHeight, direction);
		bridge.isAscender = ascender;
		// check to see if it intersects something already there
		StructurePiece intersect = StructurePiece.findCollisionPiece(list, bridge.getBoundingBox());
		if (intersect == null || intersect == this) {
			intersect = StructurePiece.findCollisionPiece(list, bridge.getWingBB());
		} else {
			return false;
		}
		// okay, I think we can actually make one, as long as we're not still intersecting something.
		if (intersect == null || intersect == this) {
			list.add(bridge);
			bridge.addChildren(this, list, rand);
			addOpening(x, y, z, rotation);
			return true;
		} else {
			return false;
		}
	}

	private boolean makeMainBridge(List<StructurePiece> list, Random rand, int index, int x, int y, int z, int wingHeight, Rotation rotation) {

		Direction direction = getStructureRelativeRotation(rotation);
		int[] dx = offsetTowerCoords(x, y, z, 3, direction);

		MushroomTowerMainBridgeComponent bridge = new MushroomTowerMainBridgeComponent(getFeatureType(), index, dx[0], dx[1], dx[2], wingHeight, direction);

		list.add(bridge);
		bridge.addChildren(this, list, rand);
		addOpening(x, y, z, rotation);
		return true;
	}

	/**
	 * Gets a random position in the specified direction that connects to stairs currently in the tower.
	 */
	@Override
	public int[] getValidOpening(Random rand, Rotation direction) {
		// variables!
		int wLength = Math.min(size / 3, 3); // wall length
		int offset = (size - wLength) / 2; // wall thickness

		// for directions 0 or 2, the wall lies along the z axis
		if (direction == Rotation.NONE || direction == Rotation.CLOCKWISE_180) {
			int rx = direction == Rotation.NONE ? size - 1 : 0;
			int rz = offset + rand.nextInt(wLength);
			int ry = getYByStairs(rz, rand, direction);

			return new int[]{rx, ry, rz};
		}

		// for directions 1 or 3, the wall lies along the x axis
		if (direction == Rotation.CLOCKWISE_90 || direction == Rotation.COUNTERCLOCKWISE_90) {
			int rx = offset + rand.nextInt(wLength);
			int rz = direction == Rotation.CLOCKWISE_90 ? size - 1 : 0;
			int ry = getYByStairs(rx, rand, direction);

			return new int[]{rx, ry, rz};
		}

		return new int[]{0, 0, 0};
	}

	/**
	 * Gets a Y value where the stairs meet the specified X coordinate.
	 * Also works for Z coordinates.
	 */
	@Override
	protected int getYByStairs(int rx, Random rand, Rotation direction) {

		int floors = this.height / FLOOR_HEIGHT;

		return FLOOR_HEIGHT + 1 + (rand.nextInt(floors - 1) * FLOOR_HEIGHT);
	}

	@Override
	public boolean postProcess(WorldGenLevel world, StructureFeatureManager manager, ChunkGenerator generator, Random rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {

		makeTrunk(world, sbb);

		// make floors
		makeFloorsForTower(world, sbb);

		// openings
		makeOpenings(world, sbb);

		return true;
	}

	private void makeTrunk(WorldGenLevel world, BoundingBox sbb) {
		int diameter = this.size / 2;
		int hollow = (int) (diameter * 0.8);

		// build the trunk upwards
		for (int dx = -diameter; dx <= diameter; dx++) {
			for (int dz = -diameter; dz <= diameter; dz++) {
				// determine how far we are from the center.
				int ax = Math.abs(dx);
				int az = Math.abs(dz);
				int dist = (int) (Math.max(ax, az) + (Math.min(ax, az) * 0.4));

				// make a trunk!
				if (dist <= diameter) {
					// make floor/ceiling
					placeBlock(world, deco.floorState, dx + diameter, 0, dz + diameter, sbb);
					placeBlock(world, deco.floorState, dx + diameter, height, dz + diameter, sbb);

					// make walls
					if (dist > hollow) {
						for (int dy = 0; dy <= height; dy++) {
							placeBlock(world, deco.blockState, dx + diameter, dy, dz + diameter, sbb);
						}
					} else {
						for (int dy = 1; dy <= height - 1; dy++) {
							placeBlock(world, AIR, dx + diameter, dy, dz + diameter, sbb);
						}
					}

					// make base
					if (this.hasBase) {
						this.fillColumnDown(world, deco.blockState, dx + diameter, -1, dz + diameter, sbb);
					}
				}
			}
		}
	}

	private void makeFloorsForTower(WorldGenLevel world, BoundingBox sbb) {
		int floors = this.height / FLOOR_HEIGHT;

		// divide the tower into floors
		for (int i = 0; i < floors; i++) {
			placeFloor(world, i * FLOOR_HEIGHT, sbb);
		}
	}

	private void placeFloor(WorldGenLevel world, int dy, BoundingBox sbb) {
		int diameter = this.size / 2;
		int hollow = (int) (diameter * 0.8);

		// build the trunk upwards
		for (int dx = -diameter; dx <= diameter; dx++) {
			for (int dz = -diameter; dz <= diameter; dz++) {
				// determine how far we are from the center.
				int ax = Math.abs(dx);
				int az = Math.abs(dz);
				int dist = (int) (Math.max(ax, az) + (Math.min(ax, az) * 0.4));

				// make a floor!
				if (dist <= hollow) { {
						placeBlock(world, this.isAscender ? Blocks.JUNGLE_PLANKS.defaultBlockState() : deco.floorState, dx + diameter, dy, dz + diameter, sbb);
					}
				}
			}
		}
	}

	/**
	 * Make an opening in this tower for a door.  This now only makes one opening, so you need two
	 */
	@Override
	protected void makeDoorOpening(WorldGenLevel world, int dx, int dy, int dz, BoundingBox sbb) {
		super.makeDoorOpening(world, dx, dy, dz, sbb);

		if (getBlock(world, dx, dy + 2, dz, sbb).getBlock() != Blocks.AIR) {
			placeBlock(world, deco.accentState, dx, dy + 2, dz, sbb);
		}
	}

	/**
	 * Called to decorate each floor.  This is responsible for adding a ladder up, the stub of the ladder going down, then picking a theme for each floor and executing it.
	 *
	 * @param floor
	 * @param bottom
	 * @param top
	 * @param ladderUpDir
	 * @param ladderDownDir
	 */
	@Override
	protected void decorateFloor(WorldGenLevel world, Random rand, int floor, int bottom, int top, Rotation ladderUpDir, Rotation ladderDownDir, BoundingBox sbb) {
		//decorateWraparoundWallSteps(world, rand, bottom, top, ladderUpDir, ladderDownDir, sbb);
	}
}
