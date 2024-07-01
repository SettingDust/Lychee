package snownee.lychee.compat.jei.category;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import snownee.lychee.compat.DisplayUtils;
import snownee.lychee.compat.jei.display.LycheeDisplay;

public abstract class LycheeDisplayCategory<T extends LycheeDisplay<?>> implements IRecipeCategory<T> {
	public static final int WIDTH = 150;
	public static final int HEIGHT = 59;

	private final RecipeType<T> type;
	private final IDrawableStatic background;
	public IDrawable icon;

	public LycheeDisplayCategory(IGuiHelper guiHelper, RecipeType<T> type, IDrawable icon, IDrawableStatic background) {
		this.background = background;
		this.icon = icon;
		this.type = type;
	}

	public LycheeDisplayCategory(IGuiHelper guiHelper, RecipeType<T> type, IDrawable icon) {
		this(guiHelper, type, icon, guiHelper.createBlankDrawable(WIDTH, HEIGHT + 8));
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
		return DisplayUtils.makeTitle(getRecipeType().getUid());
	}
}
