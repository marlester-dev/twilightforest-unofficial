package twilightforest.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class SmokeScaleParticle extends SmokeParticle {

	public SmokeScaleParticle(ClientLevel level, double x, double y, double z, double velX, double velY, double velZ, float scale, SpriteSet sprite) {
		super(level, x, y, z, velX, velY, velZ, scale, sprite);
	}

	@Environment(EnvType.CLIENT)
	public record Factory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new SmokeScaleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, 4.0F, this.sprite);
		}
	}
}
