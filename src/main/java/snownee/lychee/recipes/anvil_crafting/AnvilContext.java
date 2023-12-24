package snownee.lychee.recipes.anvil_crafting;

import java.util.Map;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.core.LycheeRecipeContext;

public class AnvilContext extends LycheeRecipeContext {
	/**
	 * The left side of the input
	 */
	public final ItemStack left;
	/**
	 * The right side of the input
	 */
	public final ItemStack right;
	public final String name; // The name to set the item, if the user specified one.
	public int levelCost; // The base cost, set this to change it if output != null
	public int materialCost = 1;
	// The number of items from the right slot to be consumed during the repair. Leave as 0 to consume the
	// entire stack.

	protected AnvilContext(
			RandomSource pRandom,
			Level level,
			Map<LootContextParam<?>, Object> pParams,
			ItemStack left,
			ItemStack right,
			String name
	) {
		super(pRandom, level, pParams);
		this.left = left;
		this.right = right;
		this.name = name;
	}

	public static class Builder extends LycheeRecipeContext.Builder<AnvilContext> {

		private final ItemStack left;
		private final ItemStack right;
		private final String name;

		public Builder(Level level, ItemStack left, ItemStack right, String name) {
			super(level);
			this.left = left;
			this.right = right;
			this.name = name;
		}

		@Override
		public AnvilContext create(LootContextParamSet pParameterSet) {
			beforeCreate(pParameterSet);
			return new AnvilContext(random, level, params, left, right, name);
		}

	}

}
