package snownee.lychee.compat.jei;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.KUtil;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.jei.category.AbstractLycheeCategory;
import snownee.lychee.compat.jei.category.CategoryProviders;
import snownee.lychee.compat.jei.category.CraftingRecipeCategoryExtension;
import snownee.lychee.compat.jei.category.IconProviders;
import snownee.lychee.compat.jei.category.WorkstationRegisters;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientHelper;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientRenderer;
import snownee.lychee.recipes.ShapedCraftingRecipe;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class LycheeJEIPlugin implements IModPlugin {
	public static final ResourceLocation ID = Lychee.id("main");
	public static final IIngredientType<PostAction> POST_ACTION = () -> PostAction.class;
	private static final Map<AllGuiTextures, IDrawable> elementMap = Maps.newIdentityHashMap();
	private final Multimap<ResourceLocation, CategoryHolder> categories = LinkedHashMultimap.create();
	public static IJeiRuntime runtime;

	public static IDrawable slot(SlotType slotType) {
		return slotType.element;
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		categories.clear();
		for (var recipeType : RecipeTypes.ALL) {
			if (!recipeType.hasStandaloneCategory) {
				continue;
			}

			var generatedCategories = JEIREI.generateCategories(recipeType, $ -> new RecipeType<>($, recipeType.clazz));

			var categoryProvider = CategoryProviders.get(recipeType);

			if (categoryProvider == null) {
				Lychee.LOGGER.error("Missing category provider for {}", recipeType);
				continue;
			}

			var guiHelper = registry.getJeiHelpers().getGuiHelper();

			generatedCategories.forEach((id, recipes) -> {
				var category = categoryProvider.get(
						(RecipeType) id,
						Objects.requireNonNull(IconProviders.get(recipeType), recipeType::toString).get(guiHelper, recipes),
						(List) recipes,
						guiHelper);
				categories.put(recipeType.categoryId, new CategoryHolder(category, (List) recipes));
				registry.addRecipeCategories(category);
			});
		}
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		registration.getCraftingCategory().addExtension(
				ShapedCraftingRecipe.class,
				new CraftingRecipeCategoryExtension());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registry) {
		categories.asMap().forEach((id, categories) -> {
			categories.forEach(it -> {
				registry.addRecipes((RecipeType) it.category.getRecipeType(), it.recipes);
			});
		});

		try {
			var recipes = KUtil.getRecipes(RecipeTypes.ANVIL_CRAFTING)
					.stream()
					.map(RecipeHolder::value)
					.filter($ -> !$.output().isEmpty() && !$.isSpecial() && !$.hideInRecipeViewer())
					.map($ -> {
						var right = Stream.of($.input().getSecond().getItems())
								.map(ItemStack::copy)
								.peek($$ -> $$.setCount($.materialCost()))
								.toList();
						return registry.getVanillaRecipeFactory().createAnvilRecipe(
								List.of($.input().getFirst().getItems()),
								right,
								List.of($.output()));
					})
					.toList();
			registry.addRecipes(mezz.jei.api.constants.RecipeTypes.ANVIL, recipes);
		} catch (Throwable e) {
			Lychee.LOGGER.error("Error when registering anvil crafting recipes", e);
		}
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		registration.register(POST_ACTION, List.of(), new PostActionIngredientHelper(), PostActionIngredientRenderer.INSTANCE);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		for (var recipeType : RecipeTypes.ALL) {
			if (!recipeType.hasStandaloneCategory) {
				continue;
			}

			var generatedCategories = JEIREI.generateCategories(recipeType, $ -> new RecipeType<>($, recipeType.clazz));

			var categoryProvider = CategoryProviders.get(recipeType);

			if (categoryProvider == null) {
				Lychee.LOGGER.error("Missing category provider for {}", recipeType);
				continue;
			}

			var guiHelper = registry.getJeiHelpers().getGuiHelper();

			generatedCategories.forEach((id, recipes) -> {
				var category = categoryProvider.get(
						(RecipeType) id,
						Objects.requireNonNull(IconProviders.get(recipeType), recipeType::toString)
								.get(guiHelper, recipes),
						(List) recipes,
						guiHelper);
				categories.put(recipeType.categoryId, new CategoryHolder(category, (List) recipes));
				var workstationRegister = WorkstationRegisters.get(recipeType);
				if (workstationRegister != null) {
					workstationRegister.consume(registry, category, (List) recipes);
				}
			});
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
		Minecraft.getInstance().execute(() -> {
			var recipes = KUtil.getRecipes(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream()
					.filter($ -> $.value() instanceof ILycheeRecipe<?> recipe && recipe.hideInRecipeViewer())
					.toList();
			jeiRuntime.getRecipeManager().hideRecipes(mezz.jei.api.constants.RecipeTypes.CRAFTING, recipes);
		});
	}


	public enum SlotType {
		NORMAL(AllGuiTextures.JEI_SLOT),
		CHANCE(AllGuiTextures.JEI_CHANCE_SLOT),
		CATALYST(AllGuiTextures.JEI_CATALYST_SLOT);

		final IDrawable element;

		SlotType(AllGuiTextures element) {
			this.element = elementMap.computeIfAbsent(element, ScreenElementWrapper::new);
		}
	}

	public record CategoryHolder(
			AbstractLycheeCategory<?> category,
			List<RecipeHolder<ILycheeRecipe<LycheeContext>>> recipes) {}
}
