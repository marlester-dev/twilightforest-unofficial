package twilightforest.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.events.CharmEvents;
import net.minecraft.world.damagesource.DamageSource;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void onDeathBeforeLoot(DamageSource damageSource, CallbackInfo ci) {
		CharmEvents.applyDeathItems((LivingEntity) (Object) this);
	}
}