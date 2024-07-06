package snownee.lychee.compat.jei.category;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.recipes.ShapedCraftingRecipe;

public class CraftingRecipeCategoryExtension implements ICraftingCategoryExtension<ShapedCraftingRecipe> {

	private static final Rect2i infoRect = new Rect2i(67, 11, 8, 8);

	@Override
	public int getWidth(RecipeHolder<ShapedCraftingRecipe> recipeHolder) {
		return recipeHolder.value().getWidth();
	}

	@Override
	public int getHeight(RecipeHolder<ShapedCraftingRecipe> recipeHolder) {
		return recipeHolder.value().getHeight();
	}

	@Override
	public void drawInfo(
			RecipeHolder<ShapedCraftingRecipe> recipe,
			int recipeWidth,
			int recipeHeight,
			GuiGraphics graphics,
			double mouseX,
			double mouseY) {
		LycheeCategory.drawInfoBadgeIfNeeded(graphics, recipe.value(), mouseX, mouseY, infoRect);
	}

	@Override
	public List<Component> getTooltipStrings(RecipeHolder<ShapedCraftingRecipe> recipe, double mouseX, double mouseY) {
		return LycheeCategory.getInfoBadgeTooltipStrings(recipe.value(), mouseX, mouseY, infoRect);
	}

	@Override
	public void setRecipe(
			RecipeHolder<ShapedCraftingRecipe> recipeHolder,
			IRecipeLayoutBuilder builder,
			ICraftingGridHelper craftingGridHelper,
			IFocusGroup focuses
	) {
		var craftingRecipe = recipeHolder.value();
		var inputs = craftingRecipe.getIngredients().stream().map(ingredient -> List.of(ingredient.getItems())).toList();
		var resultItem = craftingRecipe.getResultItem(Minecraft.getInstance().level.registryAccess());

		var width = getWidth(recipeHolder);
		var height = getHeight(recipeHolder);
		craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
		craftingGridHelper.createAndSetInputs(builder, inputs, width, height);
	}
}