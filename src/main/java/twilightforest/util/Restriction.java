package twilightforest.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import twilightforest.init.custom.Enforcement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @param hintStructureKey ResourceKey of a structure that extends the StructureHints interface, so that the correct hint book mob spawns
 * @param enforcement      ResourceKey of the Enforcement that gets used whenever a player is in a restricted biome
 * @param multiplier       A value dictating how adverse the negative effect of a restricted area should be
 * @param lockedBiomeToast Item that is used as an icon for the notification that tells the player that the area is locked
 * @param advancements     List of advancements that are required to make a biome no longer restricted
 */

public record Restriction(@Nullable ResourceKey<Structure> hintStructureKey, ResourceKey<Enforcement> enforcement,
						  float multiplier, @Nullable ItemStack lockedBiomeToast, List<ResourceLocation> advancements) {

	public static final Codec<Restriction> CODEC = RecordCodecBuilder.create((recordCodecBuilder) -> recordCodecBuilder.group(
			ResourceKey.codec(Registries.STRUCTURE).optionalFieldOf("structure_key").forGetter((restriction) -> Optional.ofNullable(restriction.hintStructureKey())),
			ResourceKey.codec(Enforcement.ENFORCEMENT_KEY).fieldOf("enforcement").forGetter(Restriction::enforcement),
			Codec.FLOAT.fieldOf("multiplier").forGetter(Restriction::multiplier),
			ItemStack.CODEC.optionalFieldOf("locked_biome_toast").forGetter((restriction) -> Optional.ofNullable(restriction.lockedBiomeToast())),
			ExtraCodecs.nonEmptyList(ResourceLocation.CODEC.listOf()).fieldOf("advancements").forGetter(Restriction::advancements)
	).apply(recordCodecBuilder, Restriction::create));

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // Vanilla does this too
	private static Restriction create(Optional<ResourceKey<Structure>> hintStructureKey, ResourceKey<Enforcement> enforcer, float multiplier, Optional<ItemStack> lockedBiomeToast, List<ResourceLocation> advancements) {
		return new Restriction(hintStructureKey.orElse(null), enforcer, multiplier, lockedBiomeToast.orElse(null), advancements);
	}
}
