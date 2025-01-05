package snownee.lychee.compat.kubejs;

import java.util.Map;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.MapJS;
import snownee.lychee.action.CustomAction;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class CustomActionEventJS implements KubeEvent {

	public final String id;
	public final CustomAction action;
	public final ILycheeRecipe<?> recipe;
	public final Map<?, ?> data;

	public CustomActionEventJS(String id, CustomAction action, ILycheeRecipe<?> recipe) {
		this.id = id;
		this.action = action;
		this.recipe = recipe;
		this.data = MapJS.of(action.data());
	}

}
