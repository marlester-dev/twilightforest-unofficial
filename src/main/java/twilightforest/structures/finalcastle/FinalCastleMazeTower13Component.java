package twilightforest.structures.finalcastle;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import twilightforest.block.TFBlocks;
import twilightforest.structures.TFStructureComponentOld;
import twilightforest.structures.lichtower.TowerWingComponent;
import twilightforest.util.RotationUtil;

import java.util.List;
import java.util.Random;

public class FinalCastleMazeTower13Component extends TowerWingComponent {

	public static final int LOWEST_DOOR = 144;
	public static final int HIGHEST_DOOR = 222;

	public BlockState color;

	public FinalCastleMazeTower13Component(StructurePieceType piece, CompoundTag nbt) {
		super(piece, nbt);
		color = NbtUtils.readBlockState(nbt.getCompound("color"));
	}

	public FinalCastleMazeTower13Component(StructureManager manager, CompoundTag nbt) {
		this(FinalCastlePieces.TFFCSiTo, nbt);
	}

	public FinalCastleMazeTower13Component(StructurePieceType piece, TFFeature feature, Random rand, int i, int x, int y, int z, BlockState color, Direction direction) {
		super(piece, feature, i);
		this.setOrientation(direction);
		this.color = color;
		this.size = 13;

		// decide a number of floors, 2-5
		int floors = rand.nextInt(3) + 2;

		this.height = floors * 8 + 1;

		// entrance should be on a random floor
		int entranceFloor = rand.nextInt(floors);

		// rationalize entrance floor if the tower is going to be too low, put the entrance floor at bottom.  Too high, put it at top
		if ((y - (entranceFloor * 8)) < LOWEST_DOOR) {
			//TwilightForestMod.LOGGER.info("Tower at " + x + ", " + z + " is getting too low, setting entrance to bottom floor.");
			entranceFloor = 0;
		}
		if ((y + ((floors - entranceFloor) * 8)) > HIGHEST_DOOR) {
			//TwilightForestMod.LOGGER.info("Tower at " + x + ", " + z + " is getting too high, setting entrance to floor " +  (floors - 1) + ".");
			entranceFloor = floors - 1;
		}

		this.boundingBox = feature.getComponentToAddBoundingBox(x, y, z, -6, -(entranceFloor * 8), -6, this.size - 1, this.height, this.size - 1, Direction.SOUTH);

		// we should have a door where we started
		addOpening(0, entranceFloor * 8 + 1, size / 2, Rotation.CLOCKWISE_180);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tagCompound) {
		super.addAdditionalSaveData(tagCompound);
		tagCompound.put("color", NbtUtils.writeBlockState(color));
	}

	//TODO: Parameter "rand" is unused. Remove?
	public FinalCastleMazeTower13Component(StructurePieceType piece, TFFeature feature, Random rand, int i, int x, int y, int z, int floors, int entranceFloor, BlockState color, Direction direction) {
		super(piece, feature, i);
		this.setOrientation(direction);
		this.color = color;
		this.size = 13;
		this.height = floors * 8 + 1;
		this.boundingBox = feature.getComponentToAddBoundingBox(x, y, z, -6, -(entranceFloor * 8), -6, this.size - 1, this.height, this.size - 1, Direction.SOUTH);
		addOpening(0, entranceFloor * 8 + 1, size / 2, Rotation.CLOCKWISE_180);
	}

	@Override
	public void addChildren(StructurePiece parent, List<StructurePiece> list, Random rand) {
		if (parent != null && parent instanceof TFStructureComponentOld) {
			this.deco = ((TFStructureComponentOld) parent).deco;
		}

		// add foundation
		FinalCastleFoundation13Component foundation = new FinalCastleFoundation13Component(FinalCastlePieces.TFFCToF13, getFeatureType(), rand, 4, this);
		list.add(foundation);
		foundation.addChildren(this, list, rand);

		// add roof
		TFStructureComponentOld roof = rand.nextBoolean() ? new FinalCastleRoof13ConicalComponent(getFeatureType(), rand, 4, this) : new FinalCastleRoof13CrenellatedComponent(getFeatureType(), rand, 4, this);
		list.add(roof);
		roof.addChildren(this, list, rand);
	}

	/**
	 * Build more components towards the destination
	 */
	public void buildTowards(StructurePiece parent, List<StructurePiece> list, Random rand, BlockPos dest) {
		// regular building first, adds roof/foundation
		this.addChildren(parent, list, rand);

		if (this.getGenDepth() < 20) {

			// are we there?
			if (this.isWithinRange(dest.getX(), dest.getZ(), this.boundingBox.x0 + 6, this.boundingBox.z0 + 6, 30)) {
				//TwilightForestMod.LOGGER.debug("We are within range of our destination, building final tower");
				int howFar = 20;
				if (!buildEndTowerTowards(list, rand, dest, this.findBestDirectionTowards(dest), howFar)) {
					if (!buildEndTowerTowards(list, rand, dest, this.findSecondDirectionTowards(dest), howFar)) {
						if (!buildEndTowerTowards(list, rand, dest, this.findThirdDirectionTowards(dest), howFar)) {
							TwilightForestMod.LOGGER.info("Could not build final tower");
						}
					}
				}
			} else {

				int howFar = 14 + rand.nextInt(24);
				Direction facing = this.findBestDirectionTowards(dest);

				// build left or right, not straight if we can help it
				if (facing == this.getOrientation() || !buildContinueTowerTowards(list, rand, dest, facing, howFar)) {
					facing = this.findSecondDirectionTowards(dest);
					if (facing == this.getOrientation() || !buildContinueTowerTowards(list, rand, dest, facing, howFar)) {
						facing = this.findThirdDirectionTowards(dest);
						if (facing == this.getOrientation() || !buildContinueTowerTowards(list, rand, dest, facing, howFar)) {
							// fine, just go straight
							if (!buildContinueTowerTowards(list, rand, dest, this.getOrientation(), howFar)) {
								TwilightForestMod.LOGGER.info("Could not build tower randomly");
							}
						}
					}
				}
			}
		} else {
			TwilightForestMod.LOGGER.info("Built 15 towers without reaching destination");
		}

		// finally, now that the critical path is built, let's add some other towers for atmosphere and complication
		this.buildNonCriticalTowers(parent, list, rand);
	}

	//TODO: Parameter "parent" is unused. Remove?
	protected void buildNonCriticalTowers(StructurePiece parent, List<StructurePiece> list, Random rand) {
		// pick a random direction
		Direction dir = RotationUtil.getRandomFacing(rand);
		Rotation relativeRotation = RotationUtil.getRelativeRotation(this.getOrientation(), dir);

		// if there isn't something in that direction, check if we can add a wrecked tower
		if (this.openingTowards[relativeRotation.ordinal()] == false) {
			if (!buildDamagedTower(list, rand, dir)) {
				dir = RotationUtil.getRandomFacing(rand);
				if (!buildDamagedTower(list, rand, dir)) {
					// maybe just a balcony?
					//buildBalconyTowards(list, rand, dir);
				}
			}
		}
	}

	private Direction findBestDirectionTowards(BlockPos dest) {

		// center of tower
		int cx = this.boundingBox.x0 + 6;
		int cz = this.boundingBox.z0 + 6;

		// difference
		int dx = cx - dest.getX();
		int dz = cz - dest.getZ();

		Direction absoluteDir;
		if (Math.abs(dx) > Math.abs(dz)) {
			absoluteDir = (dx >= 0) ? Direction.EAST : Direction.WEST;
		} else {
			absoluteDir = (dz >= 0) ? Direction.SOUTH : Direction.NORTH;
		}

		//TwilightForestMod.LOGGER.debug("Determining best direction!  center is at {}, {} and dest is at {}. offset is {}, {} so the best absolute direction is {}", cx, cz, dest, dx, dz, absoluteDir);

		return absoluteDir;
	}

	private Direction findSecondDirectionTowards(BlockPos dest) {

		// center of tower
		int cx = this.boundingBox.x0 + 6;
		int cz = this.boundingBox.z0 + 6;

		// difference
		int dx = cx - dest.getX();
		int dz = cz - dest.getZ();

		Direction absoluteDir;
		if (Math.abs(dx) < Math.abs(dz)) {  // reversed from findBestDirectionTowards
			absoluteDir = (dx >= 0) ? Direction.EAST : Direction.WEST;
		} else {
			absoluteDir = (dz >= 0) ? Direction.SOUTH : Direction.NORTH;
		}

		//TwilightForestMod.LOGGER.debug("Determining second direction!  center is at {}, {} and dest is at {}. offset is {}, {} so the best absolute direction is {}", cx, cz, dest, dx, dz, absoluteDir);

		return absoluteDir;
	}

	private Direction findThirdDirectionTowards(BlockPos dest) {
		Direction first = this.findBestDirectionTowards(dest);
		Direction second = this.findSecondDirectionTowards(dest);

		Direction[] cardinals = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

		//TwilightForestMod.LOGGER.debug("Determining third direction!  first is {}, and second is {}", first, second);

		for (Direction f : cardinals) {
			if (f != first && f != second && f != Rotation.CLOCKWISE_180.rotate(this.getOrientation())) {
				return f;
			}
		}

		// should not get here
		return this.getOrientation();
	}

	private boolean buildContinueTowerTowards(List<StructurePiece> list, Random rand, BlockPos dest, Direction facing, int howFar) {
		BlockPos opening = this.getValidOpeningCC(rand, facing);

		// adjust opening towards dest.getY() if we are getting close to dest
		int adjustmentRange = 60;
		if (this.isWithinRange(dest.getX(), dest.getZ(), this.boundingBox.x0 + 6, this.boundingBox.z0 + 6, adjustmentRange)) {
			opening = new BlockPos(
					opening.getX(),
					this.adjustOpening(opening.getY(), dest),
					opening.getZ()
			);
		}

		//TwilightForestMod.LOGGER.debug("original direction is {}", facing);

		// build towards
		BlockPos tc = this.offsetTowerCCoords(opening.getX(), opening.getY(), opening.getZ(), howFar, facing);
		//TwilightForestMod.LOGGER.debug("Our coord mode is {}, and direction is {}, so our door is going to be at {} and the new tower will appear at {}", this.getCoordBaseMode(), facing, opening, tc);

		// find start
		StructurePiece start = list.get(0);

		int centerX = ((start.getBoundingBox().x0 + 128) >> 8) << 8;
		int centerZ = ((start.getBoundingBox().z0 + 128) >> 8) << 8;

		//TwilightForestMod.LOGGER.debug("Testing range, uncorrected center is at {}, {}", centerX, centerZ);

		if (isWithinRange(centerX, centerZ, tc.getX(), tc.getZ(), 128)) {

			FinalCastleMazeTower13Component sTower = new FinalCastleMazeTower13Component(FinalCastlePieces.TFFCSiTo, getFeatureType(), rand, this.getGenDepth() + 1, tc.getX(), tc.getY(), tc.getZ(), this.color, facing);

			BoundingBox largerBB = new BoundingBox(sTower.getBoundingBox());

			largerBB.x0 -= 6;
			largerBB.z0 -= 6;
			largerBB.x1 += 6;
			largerBB.z1 += 6;
			largerBB.y0 = 0;
			largerBB.y1 = 255;

			StructurePiece intersect = StructurePiece.findCollisionPiece(list, largerBB);

			if (intersect == null) {
				//TwilightForestMod.LOGGER.debug("tower success!");
				list.add(sTower);
				sTower.buildTowards(this, list, rand, dest);

				// add bridge
				BlockPos bc = this.offsetTowerCCoords(opening.getX(), opening.getY(), opening.getZ(), 1, facing);
				FinalCastleBridgeComponent bridge = new FinalCastleBridgeComponent(getFeatureType(), this.getGenDepth() + 1, bc.getX(), bc.getY(), bc.getZ(), howFar - 7, facing);
				list.add(bridge);
				bridge.addChildren(this, list, rand);

				// opening
				addOpening(opening.getX(), opening.getY() + 1, opening.getZ(), facing);

				return true;
			} else {
				TwilightForestMod.LOGGER.info("tower blocked by {}", intersect);
				return false;
			}
		} else {
			TwilightForestMod.LOGGER.info("tower out of range");
			return false;
		}
	}

	protected boolean buildDamagedTower(List<StructurePiece> list, Random rand, Direction facing) {
		BlockPos opening = this.getValidOpeningCC(rand, facing);

		int howFar = 14 + rand.nextInt(24);
		// build towards
		BlockPos tc = this.offsetTowerCCoords(opening.getX(), opening.getY(), opening.getZ(), howFar, facing);

		// what color of tower?
		FinalCastleMazeTower13Component eTower = makeNewDamagedTower(rand, facing, tc);

		BoundingBox largerBB = new BoundingBox(eTower.getBoundingBox());

		largerBB.x0 -= 6;
		largerBB.z0 -= 6;
		largerBB.x1 += 6;
		largerBB.z1 += 6;

		StructurePiece intersect = StructurePiece.findCollisionPiece(list, largerBB);

		if (intersect == null) {
			//TwilightForestMod.LOGGER.info("wreck tower success!  tower is at " + tc.getX() + ", " + tc.getY() + ", " + tc.getZ());
			list.add(eTower);
			eTower.addChildren(this, list, rand);
			// add bridge
			BlockPos bc = this.offsetTowerCCoords(opening.getX(), opening.getY(), opening.getZ(), 1, facing);
			FinalCastleBridgeComponent bridge = new FinalCastleBridgeComponent(getFeatureType(), this.getGenDepth() + 1, bc.getX(), bc.getY(), bc.getZ(), howFar - 7, facing);
			list.add(bridge);
			bridge.addChildren(this, list, rand);

			// opening
			addOpening(opening.getX(), opening.getY() + 1, opening.getZ(), facing);

			return true;
		} else {
			//TwilightForestMod.LOGGER.info("damaged tower blocked");
			return false;
		}
	}

	protected FinalCastleMazeTower13Component makeNewDamagedTower(Random rand, Direction facing, BlockPos tc) {
		return new FinalCastleDamagedTowerComponent(FinalCastlePieces.TFFCDamT, getFeatureType(), rand, this.getGenDepth() + 1, tc.getX(), tc.getY(), tc.getZ(), facing);
	}

	private int adjustOpening(int posY, BlockPos dest) {
		int openY = posY;

		int realOpeningY = this.getWorldY(openY);
		if (realOpeningY - dest.getY() < 12) {
			// if it is too low, move it to the top floor
			openY = this.height - 9;
		} else if (dest.getY() - realOpeningY < 12) {
			// if the opening is too high, move it to the bottom floor
			openY = 0;
		}

		return openY;
	}

	private boolean buildEndTowerTowards(List<StructurePiece> list, Random rand, BlockPos dest, Direction facing, int howFar) {
		BlockPos opening = this.getValidOpeningCC(rand, facing);
		opening = new BlockPos(
				opening.getX(),
				// adjust opening towards dest.getY()
				this.adjustOpening(opening.getY(), dest),
				opening.getZ()
		);

		// build towards
		BlockPos tc = this.offsetTowerCCoords(opening.getX(), opening.getY(), opening.getZ(), howFar, facing);

		//TwilightForestMod.LOGGER.info("Our coord mode is " + this.getCoordBaseMode() + ", and direction is " + direction + ", so our door is going to be at " + opening + " and the new tower will appear at " + tc);

		// what color of tower?
		FinalCastleMazeTower13Component eTower;
		if (this.color == TFBlocks.castle_rune_brick_pink.get().defaultBlockState()) {
			eTower = new FinalCastleEntranceTowerComponent(getFeatureType(), rand, this.getGenDepth() + 1, tc.getX(), tc.getY(), tc.getZ(), facing);
		} else {
			eTower = new FinalCastleBellTower21Component(getFeatureType(), rand, this.getGenDepth() + 1, tc.getX(), tc.getY(), tc.getZ(), facing);
		}

		BoundingBox largerBB = new BoundingBox(eTower.getBoundingBox());

		largerBB.x0 -= 6;
		largerBB.z0 -= 6;
		largerBB.x1 += 6;
		largerBB.z1 += 6;

		StructurePiece intersect = StructurePiece.findCollisionPiece(list, largerBB);

		if (intersect == null) {
			//TwilightForestMod.LOGGER.info("entrance tower success!  tower is at " + tc.getX() + ", " + tc.getY() + ", " + tc.getZ() + " and dest is " + dest.getX() + ", " + dest.getY() + ", " + dest.getZ());
			list.add(eTower);
			eTower.addChildren(this, list, rand);
			// add bridge
			BlockPos bc = this.offsetTowerCCoords(opening.getX(), opening.getY(), opening.getZ(), 1, facing);
			FinalCastleBridgeComponent bridge = new FinalCastleBridgeComponent(getFeatureType(), this.getGenDepth() + 1, bc.getX(), bc.getY(), bc.getZ(), howFar - 7, facing);
			list.add(bridge);
			bridge.addChildren(this, list, rand);

			// opening
			addOpening(opening.getX(), opening.getY() + 1, opening.getZ(), facing);

			return true;
		} else {
			//TwilightForestMod.LOGGER.info("end tower blocked");
			return false;
		}
	}

	private boolean isWithinRange(int centerX, int centerZ, int posX, int posZ, int range) {
		boolean inRange = Math.abs(centerX - posX) < range && Math.abs(centerZ - posZ) < range;

		if (!inRange) {
//				TwilightForestMod.LOGGER.info("Tested range, center is at " + centerX + ", " + centerZ + " and tower is " + posX + ", " + posZ + " so distance is " + Math.max(Math.abs(centerX - posX),  Math.abs(centerZ - posZ)));
		}

		return inRange;
	}

	/**
	 * Gets a random position in the specified direction that connects to a floor currently in the tower.
	 */
	public BlockPos getValidOpeningCC(Random rand, Direction facing) {
		Rotation relative = RotationUtil.getRelativeRotation(this.getOrientation(), facing);
		int floors = (this.height / 8);

		// for directions 0 or 2, the wall lies along the z axis
		if (relative == Rotation.NONE || relative == Rotation.CLOCKWISE_180) {
			int rx = relative == Rotation.NONE ? 12 : 0;
			int rz = 6;
			int ry = rand.nextInt(floors) * 8;

			return new BlockPos(rx, ry, rz);
		}

		// for directions 1 or 3, the wall lies along the x axis
		if (relative == Rotation.CLOCKWISE_90 || relative == Rotation.COUNTERCLOCKWISE_90) {
			int rx = 6;
			int rz = relative == Rotation.CLOCKWISE_90 ? 12 : 0;
			int ry = rand.nextInt(floors) * 8;

			return new BlockPos(rx, ry, rz);
		}

		return new BlockPos(0, 0, 0);
	}

	/**
	 * Provides coordinates to make a tower such that it will open into the parent tower at the provided coordinates.
	 */
	@Override
	protected BlockPos offsetTowerCCoords(int x, int y, int z, int howFar, Direction direction) {

		int dx = getWorldX(x, z);
		int dy = getWorldY(y);
		int dz = getWorldZ(x, z);

		switch (direction) {
			case SOUTH:
				dx += howFar;
				break;
			case WEST:
				dz += howFar;
				break;
			case NORTH:
				dx -= howFar;
				break;
			case EAST:
				dz -= howFar;
				break;
			default:
				break;
		}

		// ugh?
		return new BlockPos(dx, dy, dz);
	}

	@Override
	public boolean postProcess(WorldGenLevel worldIn, StructureFeatureManager manager, ChunkGenerator generator, Random rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		Random decoRNG = new Random(worldIn.getSeed() + (this.boundingBox.x0 * 321534781) ^ (this.boundingBox.z0 * 756839));

		// walls
		generateBox(worldIn, sbb, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1, false, rand, deco.randomBlocks);

		// stone to ground
		for (int x = 0; x < this.size; x++) {
			for (int z = 0; z < this.size; z++) {
				this.fillColumnDown(worldIn, deco.blockState, x, -1, z, sbb);
			}
		}

		// add branching runes
		int numBranches = 2 + decoRNG.nextInt(4) + +decoRNG.nextInt(3);
		for (int i = 0; i < numBranches; i++) {
			makeGlyphBranches(worldIn, decoRNG, this.getGlyphColour(), sbb);
		}

		// floors
		addFloors(worldIn, decoRNG, sbb);

		// openings
		makeOpenings(worldIn, sbb);

		return true;
	}

	public BlockState getGlyphColour() {
		if (color == null) {
			TwilightForestMod.LOGGER.warn("Final Castle tower has null for glyph color, this is a bug.");
			return TFBlocks.castle_rune_brick_blue.get().defaultBlockState();
		} else {
			return color;
		}
	}

	//TODO: Parameter "rand" is unused. Remove?
	private void addFloors(WorldGenLevel world, Random rand, BoundingBox sbb) {
		// only add floors up to highest opening
		int floors = (this.highestOpening / 8) + 1;

		Rotation rotation = Rotation.CLOCKWISE_90;
		for (int i = 1; i < floors; i++) {
			this.generateBox(world, sbb, 1, i * 8, 1, 11, i * 8, 11, deco.blockState, deco.blockState, false);
			rotation = rotation.getRotated(Rotation.CLOCKWISE_180);
			// stairs
			addStairsDown(world, sbb, rotation, i * 8);
		}

		if (hasAccessibleRoof()) {
			// add stairs to roof
			addStairsDown(world, sbb, RotationUtil.ROTATIONS[(floors + 2) & 3], this.height - 1);
		}
	}

	protected boolean hasAccessibleRoof() {
		return this.height - this.highestOpening < 9;
	}

	private void addStairsDown(WorldGenLevel world, BoundingBox sbb, Rotation rotation, int y) {
		// top flight
		for (int i = 0; i < 4; i++) {
			int sx = 8 - i;
			int sy = y - i;
			int sz = 9;

			this.setBlockStateRotated(world, getStairState(deco.stairState, Direction.WEST, false), sx, sy, sz, rotation, sbb);
			this.setBlockStateRotated(world, deco.blockState, sx, sy - 1, sz, rotation, sbb);
			this.setBlockStateRotated(world, getStairState(deco.stairState, Direction.WEST, false), sx, sy, sz - 1, rotation, sbb);
			this.setBlockStateRotated(world, deco.blockState, sx, sy - 1, sz - 1, rotation, sbb);
			this.fillAirRotated(world, sbb, sx, sy + 1, sz - 1, sx, sy + 3, sz, rotation);
		}
		// landing
		this.fillBlocksRotated(world, sbb, 3, y - 4, 8, 4, y - 4, 9, deco.blockState, rotation);

		// bottom flight
		for (int i = 0; i < 4; i++) {
			int sx = 4;
			int sy = y - i - 4;
			int sz = 7 - i;

			this.setBlockStateRotated(world, getStairState(deco.stairState, Direction.NORTH, false), sx, sy, sz, rotation, sbb);
			this.setBlockStateRotated(world, deco.blockState, sx, sy - 1, sz, rotation, sbb);
			this.setBlockStateRotated(world, getStairState(deco.stairState, Direction.NORTH, false), sx - 1, sy, sz, rotation, sbb);
			this.setBlockStateRotated(world, deco.blockState, sx - 1, sy - 1, sz, rotation, sbb);
			this.fillAirRotated(world, sbb, sx, sy + 1, sz, sx - 1, sy + 3, sz, rotation);
		}
	}

	/**
	 * Make an opening in this tower for a door.
	 */
	@Override
	protected void makeDoorOpening(WorldGenLevel world, int dx, int dy, int dz, BoundingBox sbb) {
		// nullify sky light
		//nullifySkyLightAtCurrentPosition(world, dx - 3, dy - 1, dz - 3, dx + 3, dy + 3, dz + 3);

		final BlockState doorState = doorColor();

		// clear the door
		if (dx == 0 || dx == size - 1) {
			this.generateBox(world, sbb, dx, dy - 1, dz - 2, dx, dy + 4, dz + 2, deco.accentState, AIR, false);
			//this.fillWithAir(world, sbb, dx, dy, dz - 1, dx, dy + 3, dz + 1);
			this.generateBox(world, sbb, dx, dy, dz - 1, dx, dy + 3, dz + 1, doorState, AIR, false);
		}
		if (dz == 0 || dz == size - 1) {
			this.generateBox(world, sbb, dx - 2, dy - 1, dz, dx + 2, dy + 4, dz, deco.accentState, AIR, false);
			//this.fillWithAir(world, sbb, dx - 1, dy, dz, dx + 1, dy + 3, dz);
			this.generateBox(world, sbb, dx - 1, dy, dz, dx + 1, dy + 3, dz, doorState, AIR, false);
		}
	}

	public BlockState doorColor() {
		if (color == TFBlocks.castle_rune_brick_pink.get().getBlock().defaultBlockState()) {
			return TFBlocks.castle_door_pink.get().defaultBlockState();
		}
		if (color == TFBlocks.castle_rune_brick_blue.get().getBlock().defaultBlockState()) {
			return TFBlocks.castle_door_blue.get().defaultBlockState();
		}
		if (color == TFBlocks.castle_rune_brick_yellow.get().getBlock().defaultBlockState()) {
			return TFBlocks.castle_door_yellow.get().defaultBlockState();
		}
		if (color == TFBlocks.castle_rune_brick_purple.get().getBlock().defaultBlockState()) {
			return TFBlocks.castle_door_purple.get().defaultBlockState();
		}
		TwilightForestMod.LOGGER.warn("Couldn't add door to tower, rune color couldn't be read");
		return Blocks.AIR.defaultBlockState();
	}
}
