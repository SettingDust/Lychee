package snownee.lychee.compat.jei.display;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.compat.jei.category.LycheeDisplayCategory;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface DisplayRegisters {
	Map<ResourceLocation, DisplayRegister<?>> ALL = Maps.newHashMap();

	DisplayRegister<ILycheeRecipe<?>> DEFAULT = (registry, category, recipes) -> {
		registry.addRecipes(category.getRecipeType(), List.copyOf(recipes));
	};

	static <R extends ILycheeRecipe<?>> DisplayRegister<R> get(ResourceLocation id) {
		return (DisplayRegister<R>) ALL.getOrDefault(id, DEFAULT);
	}

	static <R extends ILycheeRecipe<LycheeContext>> DisplayRegister<R> register(
			LycheeRecipeType<R> type,
			DisplayRegister<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	@FunctionalInterface
	interface DisplayRegister<R extends ILycheeRecipe<?>> {
		void consume(
				IRecipeRegistration registry,
				LycheeDisplayCategory<? extends LycheeDisplay<R>> category,
				Collection<RecipeHolder<R>> recipes);
	}
}
