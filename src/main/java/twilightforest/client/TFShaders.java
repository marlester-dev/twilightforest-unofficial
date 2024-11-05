package twilightforest.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;

import java.io.IOException;

public class TFShaders {

	public static ShaderInstance RED_THREAD;
	public static PositionAwareShaderInstance AURORA;

	public static void init() {
		CoreShaderRegistrationCallback.EVENT.register(context -> {
			try {
				context.register(TwilightForestMod.prefix("red_thread/red_thread"), DefaultVertexFormat.BLOCK, shader -> RED_THREAD = shader);
				context.register(TwilightForestMod.prefix("aurora/aurora"), DefaultVertexFormat.POSITION_COLOR, shader -> AURORA = (PositionAwareShaderInstance) shader);
			} catch (IOException e) {
				TwilightForestMod.LOGGER.error(e);
			}
		});
	}
	public static class BindableShaderInstance extends ShaderInstance {

		private ShaderInstance last;

		public BindableShaderInstance(ResourceProvider p_173336_, ResourceLocation shaderLocation, VertexFormat p_173338_) throws IOException {
			super(p_173336_, shaderLocation.toString(), p_173338_);
		}

		ShaderInstance getSelf() {
			return this;
		}

		public final void bind(@Nullable Runnable exec) {
			last = RenderSystem.getShader();
			RenderSystem.setShader(this::getSelf);
			if (exec != null)
				exec.run();
			apply();
		}

		public final void runThenClear(Runnable exec) {
			exec.run();
			clear();
			RenderSystem.setShader(() -> last);
			last = null;
		}

		public final void invokeThenClear(@Nullable Runnable execBind, Runnable execPost) {
			bind(execBind);
			runThenClear(execPost);
		}

		public final void invokeThenClear(Runnable execPost) {
			invokeThenClear(null, execPost);
		}

		public final void invokeThenEndTesselator(@Nullable Runnable execBind) {
			invokeThenClear(execBind, () -> Tesselator.getInstance().end());
		}

		public final void invokeThenEndTesselator() {
			invokeThenClear(() -> Tesselator.getInstance().end());
		}

	}

	public static class PositionAwareShaderInstance extends BindableShaderInstance {

		@Nullable
		public final Uniform SEED;

		@Nullable
		public final Uniform POSITION;

		public PositionAwareShaderInstance(ResourceProvider p_173336_, ResourceLocation shaderLocation, VertexFormat p_173338_) throws IOException {
			super(p_173336_, shaderLocation, p_173338_);
			SEED = getUniform("SeedContext");
			POSITION = getUniform("PositionContext");
		}

		public final void setValue(int seed, float x, float y, float z) {
			if (SEED != null) {
				SEED.set(seed);
			}
			if (POSITION != null) {
				POSITION.set(x, y, z);
			}
		}

		public final void setValueBindApply(int seed, float x, float y, float z) {
			bind(() -> setValue(seed, x, y, z));
		}

		public final void reset() {
			setValue(0, 0, 0, 0);
		}

		public final void resetClear() {
			runThenClear(this::reset);
		}

		public final void invokeThenClear(int seed, float x, float y, float z, Runnable exec) {
			setValueBindApply(seed, x, y, z);
			exec.run();
			resetClear();
		}

		public final void invokeThenEndTesselator(int seed, float x, float y, float z) {
			invokeThenClear(seed, x, y, z, () -> Tesselator.getInstance().end());
		}

	}
}
