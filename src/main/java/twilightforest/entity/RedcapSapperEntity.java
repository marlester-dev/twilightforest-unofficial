package twilightforest.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;
import twilightforest.entity.ai.RedcapPlantTNTGoal;
import twilightforest.item.TFItems;

public class RedcapSapperEntity extends RedcapEntity {

	public RedcapSapperEntity(EntityType<? extends RedcapSapperEntity> type, Level world) {
		super(type, world);
		this.heldPick = new ItemStack(TFItems.ironwood_pickaxe.get());
		this.heldTNT.setCount(3);
	}

	@Override
	protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty) {
		super.populateDefaultEquipmentSlots(difficulty);
		this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TFItems.ironwood_boots.get()));
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(4, new RedcapPlantTNTGoal(this));
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return RedcapEntity.registerAttributes()
				.add(Attributes.MAX_HEALTH, 30.0D)
				.add(Attributes.ARMOR, 2.0D);
	}
}
