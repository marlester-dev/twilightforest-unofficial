package twilightforest;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import twilightforest.util.PlayerHelper;
import twilightforest.world.components.feature.BlockSpikeFeature;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TFConfig {

	public static Common COMMON_CONFIG;
	public static Client CLIENT_CONFIG;

	public static class Common {

		public Common(ForgeConfigSpec.Builder builder) {
			builder.
					comment("Settings that are not reversible without consequences.").
					push("Dimension Settings");
			{
				DIMENSION.newPlayersSpawnInTF = builder.
						translation(config + "spawn_in_tf").
						comment("If true, players spawning for the first time will spawn in the Twilight Forest.").
						define("newPlayersSpawnInTF", false);
				DIMENSION.portalForNewPlayerSpawn = builder.
						translation(config + "portal_for_new_player").
						comment("If true, the return portal will spawn for new players that were sent to the TF if `spawn_in_tf` is true.").
						define("portalForNewPlayer", false);
				builder.pop().
						comment("""
								Defines custom stalactites generated in hollow hills.
								Format is "modid:block size maxLength minHeight weight", where the properties are:
								Size - the maximum length of the stalactite relative to the space between hill floor and ceiling,
								Max length - maximum length of a stalactite in blocks,
								Min height - minimum space between the hill floor and the stalactite to let it generate,
								Weight - how often it generates.

								For example: "minecraft:iron_ore 0.7 8 1 24" would add a stalactite equal to the default iron ore stalactite.""").
						push("Custom Hollow Hill Stalactites");
				{
					DIMENSION.hollowHillStalactites.largeHill = builder.
							translation(config + "large_hill").
							worldRestart().
							comment("Blocks generating as stalactites in large hills only").
							defineList("largeHill", new ArrayList<>(), s -> s instanceof String);
					DIMENSION.hollowHillStalactites.mediumHill = builder.
							translation(config + "medium_hill").
							worldRestart().
							comment("Blocks generating as stalactites in medium and large hills").
							defineList("mediumHill", new ArrayList<>(), s -> s instanceof String);
					DIMENSION.hollowHillStalactites.smallHill = builder.
							translation(config + "small_hill").
							worldRestart().
							comment("Blocks generating as stalactites in all hills").
							defineList("smallHill", new ArrayList<>(), s -> s instanceof String);
					DIMENSION.hollowHillStalactites.useConfigOnly = builder.
							translation(config + "stalactite_config_only").
							worldRestart().
							comment("If true, default stalactites and stalactites defined by other mods will not be used.").
							define("useConfigOnly", false);
				}
			}
			builder.pop();
			originDimension = builder.
					translation(config + "origin_dimension").
					comment("The dimension you can always travel to the Twilight Forest from, as well as the dimension you will return to. Defaults to the overworld. (domain:regname).").
					define("originDimension", "minecraft:overworld");
			allowPortalsInOtherDimensions = builder.
					translation(config + "portals_in_other_dimensions").
					comment("Allow portals to the Twilight Forest to be made outside of the 'origin' dimension. May be considered an exploit.").
					define("allowPortalsInOtherDimensions", false);
			adminOnlyPortals = builder.
					translation(config + "admin_portals").
					comment("Allow portals only for admins (Operators). This severely reduces the range in which the mod usually scans for valid portal conditions, and it scans near ops only.").
					define("adminOnlyPortals", false);
			disablePortalCreation = builder.
					translation(config + "portals").
					comment("Disable Twilight Forest portal creation entirely. Provided for server operators looking to restrict action to the dimension.").
					define("disablePortalCreation", false);
			checkPortalDestination = builder.
					translation(config + "check_portal_destination").
					comment("Determines if new portals should be pre-checked for safety. If enabled, portals will fail to form rather than redirect to a safe alternate destination." +

							"\nNote that enabling this also reduces the rate at which portal formation checks are performed.").
					define("checkPortalDestination", false);
			portalLightning = builder.
					translation(config + "portal_lighting").
					comment("Set this true if you want the lightning that zaps the portal to not set things on fire. For those who don't like fun.").
					define("portalLightning", false);
			shouldReturnPortalBeUsable = builder.
					translation(config + "portal_return").
					comment("If false, the return portal will require the activation item.").
					define("shouldReturnPortalBeUsable", true);
			portalAdvancementLock = builder.
					translation(config + "portal_unlocked_by_advancement").
					comment("Use a valid advancement resource location as a string. For example, using the string \"minecraft:story/mine_diamond\" will lock the portal behind the \"Diamonds!\" advancement. Invalid/Empty Advancement resource IDs will leave the portal entirely unlocked.").
					define("portalUnlockedByAdvancement", "");
			maxPortalSize = builder.
					translation(config + "max_portal_size").
					comment("The max amount of water spaces the mod will check for when creating a portal. Very high numbers may cause issues.").
					defineInRange("maxPortalSize", 64, 4, Integer.MAX_VALUE);
			casketUUIDLocking = builder.
					worldRestart().
					translation(config + "casket_uuid_locking").
					comment("If true, Keepsake Caskets that are spawned when a player dies will not be accessible by other players. Use this if you dont want people taking from other people's death caskets. NOTE: server operators will still be able to open locked caskets.")
					.define("uuid_locking", false);
			disableSkullCandles = builder.
					translation(config + "disable_skull_candles").
					comment("If true, disables the ability to make Skull Candles by right clicking a vanilla skull with a candle. Turn this on if you're having mod conflict issues for some reason.").
					define("skull_candles", false);

			builder.
					comment("Settings for all things related to the uncrafting table.").
					push("Uncrafting Table");
			{
				UNCRAFTING_STUFFS.disableUncraftingXpCost = builder.
						worldRestart().
						translation(config + "uncrafting_xp_cost").
						comment("""
								Setting this to true will make it so you dont have to pay XP to uncraft stuff. This will only affect uncrafting.
								If you want to disable the xp cost for repairing and recrafting, see the below option.""").
						define("disableUncraftingXpCost", false);
				UNCRAFTING_STUFFS.disableRepairingXpCost = builder.
						worldRestart().
						translation(config + "repairing_xp_cost").
						comment("""
								Setting this to true will make it so you dont have to pay XP to repair and recraft stuff in the uncrafting table. This wont affect uncrafting cost.
								If you're confused about what repairing and recrafting are, you can read about them here: http://benimatic.com/tfwiki/index.php?title=Uncrafting_Table
								If you want to disable the xp cost for uncrafting, see the above option.""").
						define("disableRepairingXpCost", false);
				UNCRAFTING_STUFFS.disableUncraftingRecipes = builder.
						worldRestart().
						translation(config + "uncrafting_recipes").
						comment("""
								If you don't want to disable uncrafting altogether, and would rather disable certain recipes, this is for you.
								To add a recipe, add the mod id followed by the name of the recipe. You can check this in things like JEI.
								Example: "twilightforest:firefly_particle_spawner" will disable uncrafting the particle spawner into a firefly jar, firefly, and poppy.
								If an item has multiple crafting recipes and you wish to disable them all, add the item to the "twilightforest:banned_uncraftables" item tag.
								If you have a problematic ingredient, like infested towerwood for example, add the item to the "twilightforest:banned_uncrafting_ingredients" item tag.""").
						defineList("disableUncraftingRecipes", List.of("twilightforest:giant_log_to_oak_planks"), s -> s instanceof String);
				UNCRAFTING_STUFFS.reverseRecipeBlacklist = builder.
						worldRestart().
						translation(config + "uncrafting_recipes_flip").
						comment("If true, this will invert the above uncrafting recipe list from a blacklist to a whitelist.").
						define("flipRecipeList", false);
				UNCRAFTING_STUFFS.blacklistedUncraftingModIds = builder.
						worldRestart().
						translation(config + "uncrafting_mod_ids").
						comment("""
								Here, you can disable all items from certain mods from being uncrafted.
								Input a valid mod id to disable all uncrafting recipes from that mod.
								Example: "twilightforest" will disable all uncrafting recipes from this mod.""").
						defineList("blacklistedUncraftingModIds", new ArrayList<>(), s -> s instanceof String);
				UNCRAFTING_STUFFS.flipUncraftingModIdList = builder.
						worldRestart().
						translation(config + "uncrafting_mod_id_flip").
						comment("If true, this will invert the above option from a blacklist to a whitelist.").
						define("flipIdList", false);
				UNCRAFTING_STUFFS.disableUncrafting = builder.
						worldRestart().
						translation(config + "uncrafting").
						comment("Disable the uncrafting function of the uncrafting table. Recommended as a last resort if there's too many things to change about its behavior.").
						define("disableUncrafting", false);
			}
			builder.pop();

			builder.
					comment("Settings for all things related to the magic trees.").
					push("Magic Trees");
			{
				MAGIC_TREES.disableTime = builder.
						worldRestart().
						translation(config + "disable_time").
						comment("If true, prevents the Timewood Core from functioning.").
						define("disableTimeCore", false);

				MAGIC_TREES.timeRange = builder.
						worldRestart().
						translation(config + "time_range").
						comment("Defines the radius at which the Timewood Core works. Can be a number anywhere between 1 and 128.")
						.defineInRange("timeCoreRange", 16, 1, 128);

				MAGIC_TREES.disableTransformation = builder.
						worldRestart().
						translation(config + "disable_transformation").
						comment("If true, prevents the Transformation Core from functioning.").
						define("disableTransformationCore", false);

				MAGIC_TREES.transformationRange = builder.
						worldRestart().
						translation(config + "transformation_range").
						comment("Defines the radius at which the Transformation Core works. Can be a number anywhere between 1 and 128.")
						.defineInRange("transformationCoreRange", 16, 1, 128);

				MAGIC_TREES.disableMining = builder.
						worldRestart().
						translation(config + "disable_mining").
						comment("If true, prevents the Minewood Core from functioning.").
						define("disableMiningCore", false);

				MAGIC_TREES.miningRange = builder.
						worldRestart().
						translation(config + "mining_range").
						comment("Defines the radius at which the Minewood Core works. Can be a number anywhere between 1 and 128.")
						.defineInRange("miningCoreRange", 16, 1, 128);

				MAGIC_TREES.disableSorting = builder.
						worldRestart().
						translation(config + "disable_sorting").
						comment("If true, prevents the Sortingwood Core from functioning.").
						define("disableSortingCore", false);

				MAGIC_TREES.sortingRange = builder.
						worldRestart().
						translation(config + "sorting_range").
						comment("Defines the radius at which the Sortingwood Core works. Can be a number anywhere between 1 and 128.")
						.defineInRange("sortingCoreRange", 16, 1, 128);
			}
			builder.pop();

			builder.
					comment("We recommend downloading the Shield Parry mod for parrying, but these controls remain for without.").
					push("Shield Parrying");
			{
				SHIELD_INTERACTIONS.parryNonTwilightAttacks = builder.
						translation(config + "parry_non_twilight").
						comment("Set to true to parry non-Twilight projectiles.").
						define("parryNonTwilightAttacks", false);
				SHIELD_INTERACTIONS.shieldParryTicks = builder.
						translation(config + "parry_window").
						comment("The amount of ticks after raising a shield that makes it OK to parry a projectile.").
						defineInRange("shieldParryTicksArrow", 40, 0, Integer.MAX_VALUE);
			}
			builder.pop();
		}

		public Dimension DIMENSION = new Dimension();

		public static class Dimension {

			public ForgeConfigSpec.BooleanValue newPlayersSpawnInTF;
			public ForgeConfigSpec.BooleanValue portalForNewPlayerSpawn;

			public HollowHillStalactites hollowHillStalactites = new HollowHillStalactites();

			public static class HollowHillStalactites {

				public ForgeConfigSpec.ConfigValue<List<? extends String>> largeHill;
				public ForgeConfigSpec.ConfigValue<List<? extends String>> mediumHill;
				public ForgeConfigSpec.ConfigValue<List<? extends String>> smallHill;
				public ForgeConfigSpec.BooleanValue useConfigOnly;

				public void load() {
					registerHill(smallHill.get(), 1);
					registerHill(mediumHill.get(), 2);
					registerHill(largeHill.get(), 3);
				}

				private void registerHill(List<? extends String> definitions, int tier) {
					for (String definition : definitions) {
						if (!parseStalactite(definition, tier)) {
							TwilightForestMod.LOGGER.warn("Invalid hollow hill stalactite definition: {}", definition);
						}
					}
				}

				private boolean parseStalactite(String definition, int tier) {
					String[] split = definition.split(" ");
					if (split.length != 5) return false;

					Optional<Block> block = parseBlock(split[0]);
					if (block.isEmpty()) return false;

					try {
						BlockSpikeFeature.registerStalactite(tier, block.get().defaultBlockState(),
								Float.parseFloat(split[1]),
								Integer.parseInt(split[2]),
								Integer.parseInt(split[3]),
								Integer.parseInt(split[4])
						);
					} catch (NumberFormatException e) {
						return false;
					}
					return true;
				}
			}
		}

		public ForgeConfigSpec.ConfigValue<String> originDimension;
		public ForgeConfigSpec.BooleanValue allowPortalsInOtherDimensions;
		public ForgeConfigSpec.BooleanValue adminOnlyPortals;
		public ForgeConfigSpec.BooleanValue disablePortalCreation;
		public ForgeConfigSpec.BooleanValue checkPortalDestination;
		public ForgeConfigSpec.BooleanValue portalLightning;
		public ForgeConfigSpec.BooleanValue shouldReturnPortalBeUsable;
		public ForgeConfigSpec.ConfigValue<String> portalAdvancementLock;
		public ForgeConfigSpec.IntValue maxPortalSize;
		public ForgeConfigSpec.BooleanValue casketUUIDLocking;
		public ForgeConfigSpec.BooleanValue disableSkullCandles;

		public MagicTrees MAGIC_TREES = new MagicTrees();

		public static class MagicTrees {
			public ForgeConfigSpec.BooleanValue disableTime;
			public ForgeConfigSpec.IntValue timeRange;
			public ForgeConfigSpec.BooleanValue disableTransformation;
			public ForgeConfigSpec.IntValue transformationRange;
			public ForgeConfigSpec.BooleanValue disableMining;
			public ForgeConfigSpec.IntValue miningRange;
			public ForgeConfigSpec.BooleanValue disableSorting;
			public ForgeConfigSpec.IntValue sortingRange;
		}

		public UncraftingStuff UNCRAFTING_STUFFS = new UncraftingStuff();

		public static class UncraftingStuff {
			public ForgeConfigSpec.BooleanValue disableUncraftingXpCost;
			public ForgeConfigSpec.BooleanValue disableRepairingXpCost;
			public ForgeConfigSpec.BooleanValue disableUncrafting;
			public ForgeConfigSpec.ConfigValue<List<? extends String>> disableUncraftingRecipes;
			public ForgeConfigSpec.BooleanValue reverseRecipeBlacklist;
			public ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistedUncraftingModIds;
			public ForgeConfigSpec.BooleanValue flipUncraftingModIdList;
		}

		public ShieldInteractions SHIELD_INTERACTIONS = new ShieldInteractions();
		@Nullable
		public ResourceLocation portalLockingAdvancement;

		public static class ShieldInteractions {
			public ForgeConfigSpec.BooleanValue parryNonTwilightAttacks;
			public ForgeConfigSpec.IntValue shieldParryTicks;
		}

	}

	public static class Client {

		public Client(ForgeConfigSpec.Builder builder) {
			silentCicadas = builder.
					translation(config + "silent_cicadas").
					comment("Make cicadas silent for those having sound library problems, or otherwise finding them annoying.").
					define("silentCicadas", false);
			firstPersonEffects = builder.
					translation(config + "first_person_effects").
					comment("Controls whether various effects from the mod are rendered while in first-person view. Turn this off if you find them distracting.").
					define("firstPersonEffects", true);
			rotateTrophyHeadsGui = builder.
					translation(config + "animate_trophyitem").
					comment("Rotate trophy heads on item model. Has no performance impact at all. For those who don't like fun.").
					define("rotateTrophyHeadsGui", true);
			disableOptifineNagScreen = builder.
					translation(config + "optifine").
					comment("Disable the nag screen when Optifine is installed.").
					define("disableOptifineNagScreen", false);
			disableLockedBiomeToasts = builder.
					translation(config + "locked_toasts").
					comment("Disables the toasts that appear when a biome is locked. Not recommended if you're not familiar with progression.").
					define("disableLockedBiomeToasts", false);
			showQuestRamCrosshairIndicator = builder.
					translation(config + "ram_indicator").
					comment("Renders a little check mark or x above your crosshair depending on if fed the Quest Ram that color of wool. Turn this off if you find it intrusive.").
					define("questRamWoolIndicator", true);
		}

		public ForgeConfigSpec.BooleanValue silentCicadas;
		public ForgeConfigSpec.BooleanValue firstPersonEffects;
		public ForgeConfigSpec.BooleanValue rotateTrophyHeadsGui;
		public ForgeConfigSpec.BooleanValue disableOptifineNagScreen;
		public ForgeConfigSpec.BooleanValue disableLockedBiomeToasts;
		public ForgeConfigSpec.BooleanValue showQuestRamCrosshairIndicator;
	}

	private static final String config = TwilightForestMod.ID + ".config.";

	@Nullable
	public static ResourceLocation getPortalLockingAdvancement(Player player) {
		//only run assigning logic if the config has an advancement set and the RL is null
		if (!COMMON_CONFIG.portalAdvancementLock.get().isEmpty() && COMMON_CONFIG.portalLockingAdvancement == null) {

			if (!ResourceLocation.isValidResourceLocation(COMMON_CONFIG.portalAdvancementLock.get()) || PlayerHelper.getAdvancement(player, ResourceLocation.tryParse(COMMON_CONFIG.portalAdvancementLock.get())) == null) {
				//if the RL is not a valid advancement fail us
				TwilightForestMod.LOGGER.fatal("The portal locking advancement is not a valid advancement! Setting to null!");
				COMMON_CONFIG.portalAdvancementLock.set("");
			} else {
				COMMON_CONFIG.portalLockingAdvancement = ResourceLocation.tryParse(COMMON_CONFIG.portalAdvancementLock.get());
				TwilightForestMod.LOGGER.debug("Portal Locking Advancement reloaded.");
			}
		}
		//always return the RL, even if its null. We can use this to run logic less often
		return COMMON_CONFIG.portalLockingAdvancement;
	}

	private static Optional<Block> parseBlock(String string) {
		ResourceLocation id = ResourceLocation.tryParse(string);
		if (id == null || !Registry.BLOCK.containsKey(id)) {
			return Optional.empty();
		} else {
			return Optional.ofNullable(Registry.BLOCK.get(id));
		}
	}
}
