package snownee.lychee.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.util.CommonProxy;

public record Chance(float chance) implements ContextualCondition<Chance> {
	@Override
	public ContextualConditionType<Chance> type() {
		return ContextualConditionTypes.CHANCE;
	}

	@Override
	public int test(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		int n = 0;
		for (int i = 0; i < times; i++) {
			if (ctx.random().nextFloat() < chance) {
				++n;
			}
		}
		return n;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = getDescriptionId(inverted);
		return Component.translatable(key, CommonProxy.white(CommonProxy.chance(chance)));
	}

	public static class Type implements ContextualConditionType<Chance> {
		public static final Codec<Chance> CODEC = RecordCodecBuilder.create(
				instance -> instance.group(Codec.FLOAT.fieldOf("chance").forGetter(Chance::chance))
									.apply(instance, Chance::new));

		@Override
		public Codec<Chance> codec() {
			return CODEC;
		}
	}
}
