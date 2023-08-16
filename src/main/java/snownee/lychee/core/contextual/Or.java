package snownee.lychee.core.contextual;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class Or extends ContextualHolder implements ContextualCondition {

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.OR;
	}

	@Override
	public int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (ContextualCondition condition : getConditions()) {
			int result = condition.test(recipe, ctx, times);
			if (result > 0) {
				return result;
			}
		}
		return 0;
	}

	@Override
	public InteractionResult testInTooltips(Level level, @Nullable Player player) {
		boolean allFailed = true;
		for (ContextualCondition condition : getConditions()) {
			InteractionResult result = condition.testInTooltips(level, player);
			if (result == InteractionResult.SUCCESS) {
				return result;
			}
			if (result != InteractionResult.FAIL) {
				allFailed = false;
			}
		}
		return allFailed ? InteractionResult.FAIL : InteractionResult.PASS;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(makeDescriptionId(inverted));
	}

	@Override
	public void appendTooltips(List<Component> tooltips, Level level, @Nullable Player player, int indent, boolean inverted) {
		ContextualCondition.super.appendTooltips(tooltips, level, player, indent, inverted);
		for (ContextualCondition condition : getConditions()) {
			condition.appendTooltips(tooltips, level, player, indent + 1, false);
		}
	}

	public static class Type extends ContextualConditionType<Or> {

		@Override
		public Or fromJson(JsonObject o) {
			Or or = new Or();
			or.parseConditions(o.get("contextual"));
			return or;
		}

		@Override
		public void toJson(Or condition, JsonObject o) {
			o.add("contextual", condition.rawConditionsToJson());
		}

		@Override
		public Or fromNetwork(FriendlyByteBuf buf) {
			Or or = new Or();
			or.conditionsFromNetwork(buf);
			return or;
		}

		@Override
		public void toNetwork(Or condition, FriendlyByteBuf buf) {
			condition.conditionsToNetwork(buf);
		}

	}

}
