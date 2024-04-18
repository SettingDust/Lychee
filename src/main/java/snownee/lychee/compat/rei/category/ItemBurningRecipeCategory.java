package snownee.lychee.compat.rei.category;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public class ItemBurningRecipeCategory extends ItemAndBlockBaseCategory<ItemBurningRecipe> {


	public ItemBurningRecipeCategory(
			CategoryIdentifier<? extends LycheeDisplay<ItemBurningRecipe>> id,
			Renderer icon
	) {
		super(id, icon, RecipeTypes.ITEM_BURNING);
		methodRect.setX(27);
	}

	@Override
	public BlockPredicate getInputBlock(ItemBurningRecipe recipe) {
		return BlockPredicateExtensions.ANY;
	}

	@Override
	public BlockState getRenderingBlock(ItemBurningRecipe recipe) {
		return Blocks.FIRE.defaultBlockState();
	}

	@Override
	protected boolean needRenderInputBlock(ItemBurningRecipe recipe) {
		return false;
	}
}
