package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.*;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.data.tags.BiomeTagGenerator;
import twilightforest.init.TFEntities;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.HydraLairComponent;
import twilightforest.world.components.structures.util.ProgressionStructure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HydraLairStructure extends ProgressionStructure {
	public static final Codec<HydraLairStructure> CODEC = RecordCodecBuilder.create(instance ->
			progressionCodec(instance).apply(instance, HydraLairStructure::new)
	);

	public HydraLairStructure(AdvancementLockConfig advancementLockConfig, HintConfig hintConfig, DecorationConfig decorationConfig, StructureSettings structureSettings) {
		super(advancementLockConfig, hintConfig, decorationConfig, structureSettings);
	}

	@Override
	protected @Nullable StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
		return new HydraLairComponent(0, x - 7, y, z - 7);
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.HYDRA_LAIR.get();
	}

	public static HydraLairStructure buildHydraLairConfig(BootstapContext<Structure> context) {
		return new HydraLairStructure(
				new AdvancementLockConfig(List.of(TwilightForestMod.prefix("progress_labyrinth"))),
				new HintConfig(HintConfig.book("hydralair", 4), TFEntities.KOBOLD.get()),
				new DecorationConfig(2, false, false, false),
				new StructureSettings(
						context.lookup(Registries.BIOME).getOrThrow(BiomeTagGenerator.VALID_HYDRA_LAIR_BIOMES),
						Arrays.stream(MobCategory.values()).collect(Collectors.toMap(category -> category, category -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create()))), // Landmarks have Controlled Mob spawning
						GenerationStep.Decoration.SURFACE_STRUCTURES,
						TerrainAdjustment.NONE
				)
		);
	}
}
