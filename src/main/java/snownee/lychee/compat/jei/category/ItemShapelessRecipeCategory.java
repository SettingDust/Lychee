package snownee.lychee.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class ItemShapelessRecipeCategory<T extends ILycheeRecipe<LycheeContext>> extends AbstractLycheeCategory<T> {
	private final LycheeRecipeType<T> recipeType;

	public ItemShapelessRecipeCategory(
			RecipeType<T> recipeType,
			IDrawable icon,
			IGuiHelper guiHelper,
			LycheeRecipeType<T> lycheeRecipeType) {
		super(recipeType, icon, guiHelper);
		this.recipeType = lycheeRecipeType;
		this.infoRect = new Rect2i(3, 25, 8, 8);
	}

	@Override
	public LycheeRecipeType<? extends T> recipeType() {
		return recipeType;
	}

	@Override
	public int contentWidth() {
		return WIDTH + 50;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		int xCenter = getWidth() / 2;
		int y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		ingredientGroup(builder, recipe, xCenter - 45, y);
		actionGroup(builder, recipe, xCenter + 50, y);
		LycheeCategory.addBlockIngredients(builder, recipe);
	}

	@Override
	public void draw(
			T recipe,
			IRecipeSlotsView recipeSlotsView,
			GuiGraphics graphics,
			double mouseX,
			double mouseY
	) {
		drawInfoBadgeIfNeeded(graphics, recipe, mouseX, mouseY);
		var matrixStack = graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(76, 16, 0);
		matrixStack.scale(1.5F, 1.5F, 1.5F);
		getIcon().draw(graphics);
		matrixStack.popPose();
	}
}
