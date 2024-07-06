package snownee.lychee.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.RecipeTypes;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.util.ClientProxy;

public class ItemInsideRecipeCategory extends ItemAndBlockBaseCategory<ItemInsideRecipe> {

	public ItemInsideRecipeCategory(
			RecipeType<ItemInsideRecipe> recipeType,
			IDrawable icon,
			IGuiHelper guiHelper
	) {
		super(recipeType, icon, guiHelper, RecipeTypes.ITEM_INSIDE);
		infoRect.setPosition(0, 25);
		inputBlockRect.setX(80);
		methodRect.setX(77);
	}

	@Override
	public void drawExtra(ItemInsideRecipe recipe, GuiGraphics graphics, double mouseX, double mouseY, int centerX) {
		super.drawExtra(recipe, graphics, mouseX, mouseY, centerX);
		if (recipe.time() > 0) {
			var component = ClientProxy.format("tip.lychee.sec", recipe.time());
			var font = Minecraft.getInstance().font;
			graphics.drawCenteredString(font, component, methodRect.getX() + 10, methodRect.getY() - 8, 0x666666);
		}
	}

	@Override
	public int contentWidth() {
		return WIDTH + 50;
	}

	@Override
	protected void renderIngredientGroup(IRecipeLayoutBuilder builder, ItemInsideRecipe recipe, int y) {
		ingredientGroup(builder, recipe, 40, y);
	}
}
