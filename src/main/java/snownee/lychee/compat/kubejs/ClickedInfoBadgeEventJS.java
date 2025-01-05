package snownee.lychee.compat.kubejs;

import org.jetbrains.annotations.Nullable;

import dev.latvian.mods.kubejs.client.ClientKubeEvent;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ClickedInfoBadgeEventJS implements ClientKubeEvent {

	public final ILycheeRecipe<?> recipe;
	@Nullable
	public final ResourceLocation recipeId;
	public final int button;

	public ClickedInfoBadgeEventJS(ILycheeRecipe<?> recipe, @Nullable ResourceLocation recipeId, int button) {
		this.recipe = recipe;
		this.recipeId = recipeId;
		this.button = button;
	}

}
