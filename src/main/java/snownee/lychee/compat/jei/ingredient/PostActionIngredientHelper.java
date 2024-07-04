package snownee.lychee.compat.jei.ingredient;

import java.util.Objects;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.compat.jei.LycheeJEIPlugin;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;

public class PostActionIngredientHelper implements IIngredientHelper<PostAction> {

	@Override
	public PostAction copyIngredient(PostAction postAction) {
		return postAction;
	}

	@Override
	public String getDisplayName(PostAction postAction) {
		return postAction.getDisplayName().getString();
	}

	@Override
	public String getErrorInfo(PostAction postAction) {
		return Objects.toString(postAction);
	}

	@Override
	public IIngredientType<PostAction> getIngredientType() {
		return LycheeJEIPlugin.POST_ACTION;
	}

	@Override
	public String getDisplayModId(PostAction postAction) {
		var modid = getResourceLocation(postAction).getNamespace();
		return CommonProxy.wrapNamespace(modid);
	}

	@Override
	public ResourceLocation getResourceLocation(PostAction postAction) {
		return LycheeRegistries.POST_ACTION.getKey(postAction.type());
	}

	@Override
	public String getUniqueId(PostAction postAction, UidContext arg1) {
		return getResourceLocation(postAction).toString() + postAction;
	}
}