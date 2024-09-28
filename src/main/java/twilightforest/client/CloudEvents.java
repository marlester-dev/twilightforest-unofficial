package twilightforest.client;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;
import twilightforest.TFConfig;
import twilightforest.block.CloudBlock;
import twilightforest.client.renderer.TFWeatherRenderer;
import twilightforest.util.Vec2i;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CloudEvents {
	private static final List<PrecipitationRenderHelper> RENDER_HELPER = new ArrayList<>();

	record PrecipitationRenderHelper(BlockPos cloudPos, Biome.Precipitation precipitation, float precipitationLevel,
									 int rainOnY) {

	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(CloudEvents::clientTick);
		//Is this the correct event to bind?
		WorldRenderEvents.AFTER_SETUP.register(CloudEvents::renderCloudBlockPrecipitation);
	}

	public static void clientTick(Minecraft mc) {
		if (!mc.isPaused()) {
			if (mc.level != null && TFConfig.getClientCloudBlockPrecipitationDistance() > 0) { // Semi vanilla copy of the weather tick, but made to work with cloud blocks instead
				Vec3 vec3 = mc.gameRenderer.getMainCamera().getPosition();
				if (mc.level.getGameTime() % 10L == 0L) {
					RENDER_HELPER.clear();

					double camX = vec3.x();
					double camY = vec3.y();
					double camZ = vec3.z();

					int floorX = Mth.floor(camX);
					int floorY = Mth.floor(camY);
					int floorZ = Mth.floor(camZ);

					int renderDistance = Minecraft.useFancyGraphics() ? 10 : 5;
					int precipitationDistance = TFConfig.getClientCloudBlockPrecipitationDistance();

					for (int roofZ = floorZ - renderDistance; roofZ <= floorZ + renderDistance; ++roofZ) {
						for (int roofX = floorX - renderDistance; roofX <= floorX + renderDistance; ++roofX) {
							int lastBadYLevel = Integer.MIN_VALUE;
							for (int roofY = floorY - renderDistance; roofY < floorY + precipitationDistance + renderDistance; roofY++) {
								boolean skipLoop = roofY == lastBadYLevel + 1; // Cloud can't rain if there is an invalid blockState right below it, so might as well skip the loop
								BlockPos pos = new BlockPos(roofX, roofY, roofZ);
								if (Heightmap.Types.MOTION_BLOCKING.isOpaque().test(mc.level.getBlockState(pos)))
									lastBadYLevel = roofY; // Check if we skip next loop
								if (skipLoop) continue;

								if (mc.level.getBlockState(pos).getBlock() instanceof CloudBlock cloudBlock) {
									Pair<Biome.Precipitation, Float> precipitationRainLevelPair = cloudBlock.getCurrentPrecipitation(pos, mc.level, mc.level.getRainLevel(1.0F));
									if (precipitationRainLevelPair.getLeft() == Biome.Precipitation.NONE)
										continue; // No rain no gain

									int highestRainyBlock = roofY;
									for (int y = roofY - 1; y > roofY - precipitationDistance; y--) {
										if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(mc.level.getBlockState(pos.atY(y))))
											highestRainyBlock = y;
										else break;
									}
									if (highestRainyBlock == roofY) continue;

									RENDER_HELPER.add(new PrecipitationRenderHelper(pos, precipitationRainLevelPair.getLeft(), precipitationRainLevelPair.getRight(), highestRainyBlock));
								}
							}
						}
					}
				}

				RandomSource randomsource = RandomSource.create((long) mc.levelRenderer.ticks * 312987231L);
				BlockPos particlePos = null;
				int particleCount = 100 / (mc.options.particles().get() == ParticleStatus.DECREASED ? 2 : 1);

				boolean yetToMakeASound = true;
				BlockPos camPos = BlockPos.containing(vec3);

				List<Vec2i> particleChecks = new ArrayList<>();
				for (int i = 0; i < particleCount; ++i) {
					particleChecks.add(new Vec2i(randomsource.nextInt(21) - 10 + camPos.getX(), randomsource.nextInt(21) - 10 + camPos.getZ()));
				}

				for (PrecipitationRenderHelper helper : RENDER_HELPER) {
					if (helper.precipitation() == Biome.Precipitation.RAIN) {
						for (Vec2i vec2 : particleChecks) {
							if (vec2.x == helper.cloudPos().getX() && vec2.z == helper.cloudPos().getZ()) {
								BlockPos highestRainyPos = helper.cloudPos().atY(helper.rainOnY());
								if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(mc.level.getBlockState(highestRainyPos.below())))
									continue;

								if (yetToMakeASound && particlePos != null && randomsource.nextInt(3) < mc.levelRenderer.rainSoundTime++) {
									mc.levelRenderer.rainSoundTime = 0;
									if (particlePos.getY() > camPos.getY() + 1 && mc.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, camPos).getY() > Mth.floor((float) camPos.getY())) {
										mc.level.playLocalSound(particlePos, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
									} else {
										mc.level.playLocalSound(particlePos, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
									}
									yetToMakeASound = false;
								}

								if (highestRainyPos.getY() > mc.level.getMinBuildHeight() && highestRainyPos.getY() <= camPos.getY() + 10 && highestRainyPos.getY() >= camPos.getY() - 10) {
									particlePos = highestRainyPos.below();
									if (mc.options.particles().get() == ParticleStatus.MINIMAL) break;

									double particleX = randomsource.nextDouble();
									double particleZ = randomsource.nextDouble();
									BlockState blockstate = mc.level.getBlockState(particlePos);
									FluidState fluidstate = mc.level.getFluidState(particlePos);
									VoxelShape voxelshape = blockstate.getCollisionShape(mc.level, particlePos);
									double voxelMax = voxelshape.max(Direction.Axis.Y, particleX, particleZ);
									double fluidMax = fluidstate.getHeight(mc.level, particlePos);
									double particleY = Math.max(voxelMax, fluidMax);
									ParticleOptions particleoptions = !fluidstate.is(FluidTags.LAVA) && !blockstate.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
									mc.level.addParticle(particleoptions, (double) particlePos.getX() + particleX, (double) particlePos.getY() + particleY, (double) particlePos.getZ() + particleZ, 0.0D, 0.0D, 0.0D);
								}
							}
						}
					}
				}
			}
		}
	}

	public static void renderCloudBlockPrecipitation(WorldRenderContext context) {
		if (TFConfig.getClientCloudBlockPrecipitationDistance() > 0 && !RENDER_HELPER.isEmpty()) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.level == null) return;
			float partialTick = context.tickDelta();
			LightTexture lightTexture = minecraft.gameRenderer.lightTexture();
			int ticks = minecraft.levelRenderer.ticks;
			lightTexture.turnOnLightLayer();

			Vec3 vec3 = context.camera().getPosition();
			double camX = vec3.x();
			double camY = vec3.y();
			double camZ = vec3.z();

			int floorX = Mth.floor(camX);
			int floorY = Mth.floor(camY);
			int floorZ = Mth.floor(camZ);

			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(Minecraft.useShaderTransparency());

			int renderDistance = Minecraft.useFancyGraphics() ? 10 : 5;

			int tesselatorCheck = -1;
			float fullTick = (float) ticks + partialTick;
			RenderSystem.setShader(GameRenderer::getParticleShader);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (PrecipitationRenderHelper helper : RENDER_HELPER) {
				BlockPos pos = helper.cloudPos();
				int roofX = pos.getX();
				int roofZ = pos.getZ();

				int botY = Math.max(helper.rainOnY(), floorY - renderDistance);
				int topY = Math.min(pos.getY(), floorY + renderDistance);
				if (topY - botY <= 0) continue;

				// Due to the fact the positions only update once every 10 ticks, a fast player can get far enough from the blocks to cause a block difference too large
				int rainS = Mth.clamp((roofZ - floorZ + 16) * 32 + roofX - floorX + 16, 0, 1023); // Array size is 1024, so we make sure it doesn't fail
				double rainX = (double) TFWeatherRenderer.rainxs[rainS] * 0.5D;
				double rainZ = (double) TFWeatherRenderer.rainzs[rainS] * 0.5D;
				mutableBlockPos.set(roofX, camY, roofZ);

				RandomSource random = RandomSource.create((long) roofX * roofX * 3121 + roofX * 45238971L ^ (long) roofZ * roofZ * 418711 + roofZ * 13761L);
				if (helper.precipitation() == Biome.Precipitation.RAIN) {
					if (tesselatorCheck != 0) {
						if (tesselatorCheck >= 0) tesselator.end();

						tesselatorCheck = 0;
						RenderSystem.setShaderTexture(0, TFWeatherRenderer.RAIN_TEXTURES);
						bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
					}

					int offset = ticks + roofX * roofX * 3121 + roofX * 45238971 + roofZ * roofZ * 418711 + roofZ * 13761 & 31;
					float uvOffset = -((float) offset + partialTick) / 32.0F * (3.0F + random.nextFloat());
					double xDiff = (double) roofX + 0.5D - camX;
					double zDiff = (double) roofZ + 0.5D - camZ;
					float distance = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff) / (float) renderDistance;
					float alpha = ((1.0F - distance * distance) * 0.5F + 0.5F) * helper.precipitationLevel();
					mutableBlockPos.set(roofX, Math.max(helper.rainOnY(), floorY), roofZ);
					int lightColor = LevelRenderer.getLightColor(minecraft.level, mutableBlockPos);

					bufferbuilder.vertex((double) roofX - camX - rainX + 0.5D, (double) topY - camY, (double) roofZ - camZ - rainZ + 0.5D).uv(0.0F, (float) botY * 0.25F + uvOffset).color(1.0F, 1.0F, 1.0F, alpha).uv2(lightColor).endVertex();
					bufferbuilder.vertex((double) roofX - camX + rainX + 0.5D, (double) topY - camY, (double) roofZ - camZ + rainZ + 0.5D).uv(1.0F, (float) botY * 0.25F + uvOffset).color(1.0F, 1.0F, 1.0F, alpha).uv2(lightColor).endVertex();
					bufferbuilder.vertex((double) roofX - camX + rainX + 0.5D, (double) botY - camY, (double) roofZ - camZ + rainZ + 0.5D).uv(1.0F, (float) topY * 0.25F + uvOffset).color(1.0F, 1.0F, 1.0F, alpha).uv2(lightColor).endVertex();
					bufferbuilder.vertex((double) roofX - camX - rainX + 0.5D, (double) botY - camY, (double) roofZ - camZ - rainZ + 0.5D).uv(0.0F, (float) topY * 0.25F + uvOffset).color(1.0F, 1.0F, 1.0F, alpha).uv2(lightColor).endVertex();
				} else if (helper.precipitation() == Biome.Precipitation.SNOW) {
					if (tesselatorCheck != 1) {
						if (tesselatorCheck == 0) tesselator.end();

						tesselatorCheck = 1;
						RenderSystem.setShaderTexture(0, TFWeatherRenderer.SNOW_TEXTURES);
						bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
					}

					float offset = -((float) (ticks & 511) + partialTick) / 512.0F;
					float uOffset = (float) (random.nextDouble() + (double) fullTick * 0.01D * (double) ((float) random.nextGaussian()));
					float vOffset = (float) (random.nextDouble() + (double) (fullTick * (float) random.nextGaussian()) * 0.001D);
					double xDiff = (double) roofX + 0.5D - camX;
					double zDiff = (double) roofZ + 0.5D - camZ;
					float distance = (float) Math.sqrt(xDiff * xDiff + zDiff * zDiff) / (float) renderDistance;
					float alpha = ((1.0F - distance * distance) * 0.3F + 0.5F) * helper.precipitationLevel();
					mutableBlockPos.set(roofX, Math.max(helper.rainOnY(), floorY), roofZ);

					int lightColor = LevelRenderer.getLightColor(minecraft.level, mutableBlockPos);
					int v = lightColor >> 16 & '\uffff';
					int u = lightColor & '\uffff';
					v = (v * 3 + 240) / 4;
					u = (u * 3 + 240) / 4;

					bufferbuilder.vertex((double) roofX - camX - rainX + 0.5D, (double) topY - camY, (double) roofZ - camZ - rainZ + 0.5D).uv(0.0F + uOffset, (float) botY * 0.25F + offset + vOffset).color(1.0F, 1.0F, 1.0F, alpha).uv2(u, v).endVertex();
					bufferbuilder.vertex((double) roofX - camX + rainX + 0.5D, (double) topY - camY, (double) roofZ - camZ + rainZ + 0.5D).uv(1.0F + uOffset, (float) botY * 0.25F + offset + vOffset).color(1.0F, 1.0F, 1.0F, alpha).uv2(u, v).endVertex();
					bufferbuilder.vertex((double) roofX - camX + rainX + 0.5D, (double) botY - camY, (double) roofZ - camZ + rainZ + 0.5D).uv(1.0F + uOffset, (float) topY * 0.25F + offset + vOffset).color(1.0F, 1.0F, 1.0F, alpha).uv2(u, v).endVertex();
					bufferbuilder.vertex((double) roofX - camX - rainX + 0.5D, (double) botY - camY, (double) roofZ - camZ - rainZ + 0.5D).uv(0.0F + uOffset, (float) topY * 0.25F + offset + vOffset).color(1.0F, 1.0F, 1.0F, alpha).uv2(u, v).endVertex();
				}
			}

			if (tesselatorCheck >= 0) tesselator.end();

			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			lightTexture.turnOffLightLayer();
		}
	}
}
