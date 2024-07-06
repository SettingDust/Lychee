package snownee.lychee.compat.jei.category;

import java.util.List;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class AbstractLycheeCategory<T extends ILycheeRecipe<LycheeContext>> implements IRecipeCategory<T>, LycheeCategory<T> {
	protected Rect2i infoRect = new Rect2i(4, 25, 8, 8);
	public static final int WIDTH = 119;
	public static final int HEIGHT = 59;

	private final RecipeType<T> type;
	private final IDrawableStatic background;
	public IDrawable icon;

	public AbstractLycheeCategory(RecipeType<T> type, IDrawable icon, IDrawableStatic background) {
		this.background = background;
		this.icon = icon;
		this.type = type;
	}

	public AbstractLycheeCategory(RecipeType<T> type, IDrawable icon, IGuiHelper guiHelper) {
		this(type, icon, guiHelper.createBlankDrawable(WIDTH, HEIGHT + 8));
	}

	@Override
	public RecipeType<T> getRecipeType() {
		return type;
	}

	@Override
	public IDrawableStatic getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public Component getTitle() {
		return JEIREI.makeTitle(getRecipeType().getUid());
	}

	@Override
	public Rect2i infoRect() {
		return infoRect;
	}

	@Override
	public int getWidth() {
		return contentWidth();
	}

	@Override
	public List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		return getInfoBadgeTooltipStrings(recipe, mouseX, mouseY);
	}
}
