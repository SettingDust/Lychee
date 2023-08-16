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

public class And extends ContextualHolder implements ContextualCondition {

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.AND;
	}

	@Override
	public int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return checkConditions(recipe, ctx, times);
	}

	@Override
	public InteractionResult testInTooltips(Level level, @Nullable Player player) {
		boolean allSuccess = true;
		for (ContextualCondition condition : getConditions()) {
			InteractionResult result = condition.testInTooltips(level, player);
			if (result == InteractionResult.FAIL) {
				return result;
			}
			if (result != InteractionResult.SUCCESS) {
				allSuccess = false;
			}
		}
		return allSuccess ? InteractionResult.SUCCESS : InteractionResult.PASS;
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

	@Override
	public int showingCount() {
		return showingConditionsCount();
	}

	public static class Type extends ContextualConditionType<And> {

		@Override
		public And fromJson(JsonObject o) {
			And and = new And();
			and.parseConditions(o.get("contextual"));
			return and;
		}

		@Override
		public void toJson(And condition, JsonObject o) {
			o.add("contextual", condition.rawConditionsToJson());
		}

		@Override
		public And fromNetwork(FriendlyByteBuf buf) {
			And and = new And();
			and.conditionsFromNetwork(buf);
			return and;
		}

		@Override
		public void toNetwork(And condition, FriendlyByteBuf buf) {
			condition.conditionsToNetwork(buf);
		}

	}

}
