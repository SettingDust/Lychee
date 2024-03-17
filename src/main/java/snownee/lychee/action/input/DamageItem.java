package snownee.lychee.action.input;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.Reference;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record DamageItem(PostActionCommonProperties commonProperties, int damage, Reference target) implements PostAction {

	@Override
	public PostActionType<DamageItem> type() {
		return PostActionTypes.DAMAGE_ITEM;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var indexes = recipe.getItemIndexes(target);
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var thisEntity = lootParamsContext.get(LootContextParams.THIS_ENTITY);
		var itemStackHolders = context.get(LycheeContextKey.ITEM);
		for (var index : indexes) {
			var stackHolder = itemStackHolders.get(index);
			if (!stackHolder.get().isDamageableItem()) {
				return;
			}
			stackHolder.setIgnoreConsumption(true);
			var itemStack = itemStackHolders.split(index, 1).get();

			// Forge hook
			//		if (thisEntity instanceof LivingEntity) {
			//			damage = stack.getItem().damageItem(stack, damage, (LivingEntity) thisEntity, onBroken);
			//		}

			itemStack.hurtAndBreak(
					damage,
					context.get(LycheeContextKey.RANDOM),
					thisEntity instanceof ServerPlayer player ? player : null, () -> {
						if (thisEntity instanceof LivingEntity livingEntity) {
							EquipmentSlot hand = null;
							if (livingEntity.getMainHandItem() == itemStack) {
								hand = EquipmentSlot.MAINHAND;
							} else if (livingEntity.getOffhandItem() == itemStack) {
								hand = EquipmentSlot.OFFHAND;
							}
							if (hand != null) {
								livingEntity.broadcastBreakEvent(hand);
							}
						}
						var item = itemStack.getItem();
						itemStack.shrink(1);
						if (thisEntity instanceof Player player) {
							player.awardStat(Stats.ITEM_BROKEN.get(item));
						}
						itemStack.setDamageValue(0);
					});
		}
	}

	@Override
	public boolean hidden() {
		return true;
	}

	@Override
	public boolean repeatable() {
		return false;
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		Preconditions.checkArgument(!recipe.getItemIndexes(target).isEmpty(), "No target found for %s", target);
	}

	@Override
	public <T extends ILycheeRecipe<?>> void loadCatalystsInfo(@Nullable T recipe, List<IngredientInfo> ingredients) {
		var key = CommonProxy.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(type()));
		var component = Component.translatable(key, damage).withStyle(ChatFormatting.YELLOW);
		recipe.getItemIndexes(target).forEach(i -> {
			var info = ingredients.get(i);
			info.addTooltip(component);
			info.isCatalyst = true;
		});
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return 0;
	}

	public static class Type implements PostActionType<DamageItem> {
		public static final Codec<DamageItem> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(DamageItem::commonProperties),
						ExtraCodecs.strictOptionalField(Codec.INT, "damage", 1).forGetter(DamageItem::damage),
						Reference.CODEC.fieldOf("target").forGetter(DamageItem::target)
				).apply(instance, DamageItem::new));

		@Override
		public @NotNull Codec<DamageItem> codec() {
			return CODEC;
		}
	}
}
