package twilightforest.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import twilightforest.TwilightForestMod;

public class HydraChopTrigger extends SimpleCriterionTrigger<HydraChopTrigger.Instance> {

	public static final ResourceLocation ID = TwilightForestMod.prefix("consume_hydra_chop_on_low_hunger");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext ctx) {
		return new HydraChopTrigger.Instance(player);
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, (instance) -> true);
	}

	public static class Instance extends AbstractCriterionTriggerInstance {
		public Instance(ContextAwarePredicate player) {
			super(HydraChopTrigger.ID, player);
		}

		public static Instance eatChop() {
			return new Instance(ContextAwarePredicate.ANY);
		}
	}
}
