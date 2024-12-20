package twilightforest.mixin;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.events.CharmEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
		super(level, pos, yRot, gameProfile);
	}
	@Inject(
			method = "die",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerPlayer;dropAllDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;)V"
			)
	)
	private void onDeathBeforeLoot(DamageSource damageSource, CallbackInfo ci) {
		CharmEvents.applyDeathItems(this);
	}
}