package twilightforest;

import com.mojang.authlib.EnvironmentParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.*;
import com.mojang.authlib.minecraft.client.ObjectMapper;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import io.github.fabricators_of_create.porting_lib.util.ServerLifecycleHooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.StringUtils;
import twilightforest.network.SyncUncraftingTableConfigPacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.util.PlayerHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
					comment("""
							Determines if new portals should be pre-checked for safety. If enabled, portals will fail to form rather than redirect to a safe alternate destination.
							Note that enabling this also reduces the rate at which portal formation checks are performed.""").
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
			defaultItemEnchants = builder.
					translation(config + "default_item_enchantments").
					worldRestart().
					comment("""
							If false, items that come enchanted when you craft them (such as ironwood or steeleaf gear) will not show this way in the creative inventory.
							Please note that this doesnt affect the crafting recipes themselves, you will need a datapack to change those.""").
					define("default_item_enchantments", true);

			bossDropChests = builder.
					translation(config + "boss_drop_chests").
					comment("""
							If true, Twilight Forest's bosses will put their drops inside of a chest where they originally spawned instead of dropping the loot directly.
							Note that the Knight Phantoms are not affected by this as their drops work differently.""").
					define("boss_drop_chests", true);

			cloudBlockPrecipitationDistanceCommon = builder.
					translation(config + "cloud_block_precipitation_distance_server").
					comment("""
							Dictates how many blocks down from a cloud block should the game logic check for handling weather related code.
							Lower if experiencing low tick rate. Set to 0 to turn all cloud precipitation logic off.""").
					defineInRange("cloudBlockPrecipitationDistance", 32, 0, Integer.MAX_VALUE);

			builder.
					comment("Settings for all things related to the uncrafting table.").
					push("Uncrafting Table");
			{
				UNCRAFTING_STUFFS.uncraftingXpCostMultiplier = builder.
						worldRestart().
						translation(config + "uncrafting_xp_cost").
						comment("""
								Multiplies the total XP cost of uncrafting an item and rounds up.
								Higher values means the recipe will cost more to uncraft, lower means less. Set to 0 to disable the cost altogether.
								Note that this only affects reversed crafting recipes, uncrafting recipes will still use the same cost as they normally would.""").
						defineInRange("uncraftingXpCostMultiplier", 1.0D, 0.0D, Double.MAX_VALUE);
				UNCRAFTING_STUFFS.repairingXpCostMultiplier = builder.
						worldRestart().
						translation(config + "repairing_xp_cost").
						comment("""
								Multiplies the total XP cost of repairing an item and rounds up.
								Higher values means the recipe will cost more to repair, lower means less. Set to 0 to disable the cost altogether.""").
						defineInRange("repairingXpCostMultiplier", 1.0D, 0.0D, Double.MAX_VALUE);
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
				UNCRAFTING_STUFFS.allowShapelessUncrafting = builder.
						worldRestart().
						translation(config + "shapeless_uncrafting").
						comment("""
								If true, the uncrafting table will also be allowed to uncraft shapeless recipes.
								The table was originally intended to only take shaped recipes, but this option remains for people who wish to keep the functionality.""").
						define("enableShapelessCrafting", false);
				UNCRAFTING_STUFFS.disableUncraftingOnly = builder.
						worldRestart().
						translation(config + "disable_uncrafting").
						comment("""
								Disables the uncrafting function of the uncrafting table. Recommended as a last resort if there's too many things to change about its behavior (or you're just lazy, I dont judge).
								Do note that special uncrafting recipes are not disabled as the mod relies on them for other things.""").
						define("disableUncrafting", false);

				UNCRAFTING_STUFFS.disableEntireTable = builder.
						worldRestart().
						translation(config + "disable_uncrafting_table").
						comment("""
								Disables any usage of the uncrafting table, as well as prevents it from showing up in loot or crafted.
								Please note that table has more uses than just uncrafting, you can read about them here! http://benimatic.com/tfwiki/index.php?title=Uncrafting_Table
								It is highly recommended to keep the table enabled as the mod has special uncrafting exclusive recipes, but the option remains for people that dont want the table to be functional at all.
								If you are looking to just prevent normal crafting recipes from being reversed, consider using the 'disableUncrafting' option instead.""").
						define("disableUncraftingTable", false);
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

		public final Dimension DIMENSION = new Dimension();

		public static class Dimension {

			public ForgeConfigSpec.BooleanValue newPlayersSpawnInTF;
			public ForgeConfigSpec.BooleanValue portalForNewPlayerSpawn;

		}

		public final ForgeConfigSpec.ConfigValue<String> originDimension;
		public final ForgeConfigSpec.BooleanValue allowPortalsInOtherDimensions;
		public final ForgeConfigSpec.BooleanValue adminOnlyPortals;
		public final ForgeConfigSpec.BooleanValue disablePortalCreation;
		public final ForgeConfigSpec.BooleanValue checkPortalDestination;
		public final ForgeConfigSpec.BooleanValue portalLightning;
		public final ForgeConfigSpec.BooleanValue shouldReturnPortalBeUsable;
		public final ForgeConfigSpec.ConfigValue<String> portalAdvancementLock;
		public final ForgeConfigSpec.IntValue maxPortalSize;
		public final ForgeConfigSpec.BooleanValue casketUUIDLocking;
		public final ForgeConfigSpec.BooleanValue disableSkullCandles;
		public final ForgeConfigSpec.BooleanValue defaultItemEnchants;
		public final ForgeConfigSpec.BooleanValue bossDropChests;
		public final ForgeConfigSpec.IntValue cloudBlockPrecipitationDistanceCommon;

		public final MagicTrees MAGIC_TREES = new MagicTrees();

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

		public final UncraftingStuff UNCRAFTING_STUFFS = new UncraftingStuff();

		public static class UncraftingStuff {
			public ForgeConfigSpec.DoubleValue uncraftingXpCostMultiplier;
			public ForgeConfigSpec.DoubleValue repairingXpCostMultiplier;
			public ForgeConfigSpec.BooleanValue allowShapelessUncrafting;
			public ForgeConfigSpec.BooleanValue disableUncraftingOnly;
			public ForgeConfigSpec.BooleanValue disableEntireTable;
			public ForgeConfigSpec.ConfigValue<List<? extends String>> disableUncraftingRecipes;
			public ForgeConfigSpec.BooleanValue reverseRecipeBlacklist;
			public ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistedUncraftingModIds;
			public ForgeConfigSpec.BooleanValue flipUncraftingModIdList;
		}

		public final ShieldInteractions SHIELD_INTERACTIONS = new ShieldInteractions();
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
			silentCicadasOnHead = builder.
					translation(config + "silent_cicadas_on_head").
					comment("Make cicadas silent when sitting on your head. If the above option is already true, this won't have any effect.").
					define("silentCicadasOnHead", false);
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
			cloudBlockPrecipitationDistanceClient = builder.
					translation(config + "cloud_block_precipitation_distance").
					comment("""
							Renders precipitation underneath cloud blocks. -1 sets it to be synced with the common config.
							Set this to a lower number if you're experiencing poor performance, or set it to 0 if you wish to turn it off""").
					defineInRange("cloudBlockPrecipitationDistance", -1, -1, Integer.MAX_VALUE);
			giantSkinUUIDs = builder.
					translation(config + "giant_skin_uuid_list").
					comment("""
							List of player UUIDs whose skins the giants of Twilight Forest should use.
							If left empty, the giants will appear the same as the player viewing them does.""").
					defineListAllowEmpty(List.of("giantSkinUUIDs"), ArrayList::new, s -> s instanceof String);

		}

		public final ForgeConfigSpec.BooleanValue silentCicadas;
		public final ForgeConfigSpec.BooleanValue silentCicadasOnHead;
		public final ForgeConfigSpec.BooleanValue firstPersonEffects;
		public final ForgeConfigSpec.BooleanValue rotateTrophyHeadsGui;
		public final ForgeConfigSpec.BooleanValue disableOptifineNagScreen;
		public final ForgeConfigSpec.BooleanValue disableLockedBiomeToasts;
		public final ForgeConfigSpec.BooleanValue showQuestRamCrosshairIndicator;
		public final ForgeConfigSpec.IntValue cloudBlockPrecipitationDistanceClient;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> giantSkinUUIDs;
	}

	private static final String config = "config." + TwilightForestMod.ID;

	public static int getClientCloudBlockPrecipitationDistance() {
		return (CLIENT_CONFIG.cloudBlockPrecipitationDistanceClient.get() == -1 ? COMMON_CONFIG.cloudBlockPrecipitationDistanceCommon : CLIENT_CONFIG.cloudBlockPrecipitationDistanceClient).get();
	}

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

	public static void onConfigReload(final ModConfig config) {
		//resends uncrafting settings to all players when the config is reloaded. This ensures all players have matching configs so things dont desync.
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null && server.isDedicatedServer()) {
			TFPacketHandler.CHANNEL.sendToClientsInServer(new SyncUncraftingTableConfigPacket(
					COMMON_CONFIG.UNCRAFTING_STUFFS.uncraftingXpCostMultiplier.get(),
					COMMON_CONFIG.UNCRAFTING_STUFFS.repairingXpCostMultiplier.get(),
					COMMON_CONFIG.UNCRAFTING_STUFFS.allowShapelessUncrafting.get(),
					COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingOnly.get(),
					COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get(),
					COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingRecipes.get(),
					COMMON_CONFIG.UNCRAFTING_STUFFS.reverseRecipeBlacklist.get(),
					COMMON_CONFIG.UNCRAFTING_STUFFS.blacklistedUncraftingModIds.get(),
					COMMON_CONFIG.UNCRAFTING_STUFFS.flipUncraftingModIdList.get()), server);
		}

		TFConfig.giantCheck(config);
		//sets cached portal locking advancement to null just in case it changed
		COMMON_CONFIG.portalLockingAdvancement = null;
	}

	//damn forge events
	public static class ConfigSync {
		//sends uncrafting settings to a player on a server when they log in. This prevents desyncs when the configs dont match up between the player and the server.
		public static void syncConfigOnLogin(ServerPlayer player) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null && server.isDedicatedServer()) {
				TFPacketHandler.CHANNEL.sendToClient(new SyncUncraftingTableConfigPacket(
						COMMON_CONFIG.UNCRAFTING_STUFFS.uncraftingXpCostMultiplier.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.repairingXpCostMultiplier.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.allowShapelessUncrafting.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingOnly.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingRecipes.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.reverseRecipeBlacklist.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.blacklistedUncraftingModIds.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.flipUncraftingModIdList.get()), player);
			}
		}
	}

	public static final List<GameProfile> GAME_PROFILES = new ArrayList<>();

	public static void giantCheck(final ModConfig config) {
		if (Objects.equals(config.getModId(), TwilightForestMod.ID) && config.getType().equals(ModConfig.Type.CLIENT)) {
			new Thread() {
				@Override
				public void run() {
					GAME_PROFILES.clear();

					YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
					String baseUrl = EnvironmentParser.getEnvironmentFromProperties().orElse(YggdrasilEnvironment.PROD.getEnvironment()).getSessionHost() + "/session/minecraft/";
					boolean requireSecure = false;
					for (String stringUUID : TFConfig.CLIENT_CONFIG.giantSkinUUIDs.get()) {
						try {
							UUID uuid = UUID.fromString(stringUUID);

							URL url = HttpAuthenticationService.constantURL(baseUrl + "profile/" + uuid);
							url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);

							final MinecraftProfilePropertiesResponse response = ObjectMapper.create().readValue(service.performGetRequest(url), MinecraftProfilePropertiesResponse.class);

							if (StringUtils.isNotBlank(response.getError())) {
								if ("UserMigratedException".equals(response.getCause())) {
									throw new UserMigratedException(response.getErrorMessage());
								} else if ("ForbiddenOperationException".equals(response.getError())) {
									throw new InvalidCredentialsException(response.getErrorMessage());
								} else if ("InsufficientPrivilegesException".equals(response.getError())) {
									throw new InsufficientPrivilegesException(response.getErrorMessage());
								} else if ("multiplayer.access.banned".equals(response.getError())) {
									throw new UserBannedException();
								} else {
									throw new AuthenticationException(response.getErrorMessage());
								}
							}

							if (response.getId() != null) {
								final GameProfile result = new GameProfile(response.getId(), response.getName());
								if (response.getProperties() != null)
									result.getProperties().putAll(response.getProperties());
								GAME_PROFILES.add(result);
							}
						} catch (IllegalArgumentException e) {
							TwilightForestMod.LOGGER.error("\"{}\" is not a valid UUID!", stringUUID);
						} catch (AuthenticationException | IOException e) {
							TwilightForestMod.LOGGER.error(e);
						}
					}
					super.run();
				}
			}.start();
		}
	}
}
