package snownee.lychee.compat.jei;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import dev.architectury.event.EventResult;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Arrow;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.category.extension.CategoryExtensionProvider;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.KUtil;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.jei.category.CategoryProviders;
import snownee.lychee.compat.jei.category.IconProviders;
import snownee.lychee.compat.jei.category.LycheeCategory;
import snownee.lychee.compat.jei.category.LycheeDisplayCategory;
import snownee.lychee.compat.jei.category.WorkstationRegisters;
import snownee.lychee.compat.jei.display.AnvilCraftingDisplay;
import snownee.lychee.compat.jei.display.DisplayRegisters;
import snownee.lychee.compat.jei.display.LycheeDisplay;
import snownee.lychee.compat.jei.ingredient.PostActionIngredientHelper;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class LycheeJEIPlugin implements IModPlugin {
	public static final ResourceLocation ID = Lychee.id("main");
	public static final IIngredientType<PostAction> POST_ACTION = () -> PostAction.class;
	private static final Map<AllGuiTextures, IDrawable> elementMap = Maps.newIdentityHashMap();
	private final Multimap<ResourceLocation, CategoryHolder> categories = LinkedHashMultimap.create();

	private static ResourceLocation composeCategoryIdentifier(ResourceLocation categoryId, ResourceLocation group) {
		return ResourceLocation.fromNamespaceAndPath(
				categoryId.getNamespace(),
				"%s/%s/%s".formatted(categoryId.getPath(), group.getNamespace(), group.getPath()));
	}

	private static ImmutableMultimap<CategoryIdentifier<? extends LycheeDisplay<?>>, RecipeHolder<? extends ILycheeRecipe<LycheeContext>>> generateCategories(
			LycheeRecipeType<? extends ILycheeRecipe<LycheeContext>> recipeType) {
		return recipeType
				.inViewerRecipes()
				.stream()
				.reduce(
						ImmutableMultimap.<CategoryIdentifier<? extends LycheeDisplay<?>>, RecipeHolder<? extends ILycheeRecipe<LycheeContext>>>builder(),
						(map, recipeHolder) -> {
							map.put(
									CategoryIdentifier.of(composeCategoryIdentifier(
											recipeType.categoryId,
											ResourceLocation.parse(recipeHolder.value().group()))),
									recipeHolder
							);
							return map;
						},
						(map, ignored) -> map)
				.build();
	}

	public static Rectangle offsetRect(Point startPoint, Rect2i rect) {
		return new Rectangle(startPoint.x + rect.getX(), startPoint.y + rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public static IDrawable slot(SlotType slotType) {
		return slotType.element;
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		var helpers = registry.getJeiHelpers();
		categories.clear();
		for (var recipeType : RecipeTypes.ALL) {
			if (!recipeType.hasStandaloneCategory) {
				continue;
			}

			var generatedCategories = generateCategories(recipeType);

			var categoryProvider = CategoryProviders.get(recipeType);

			if (categoryProvider == null) {
				Lychee.LOGGER.error("Missing category provider for {}", recipeType);
				continue;
			}

			generatedCategories.asMap().forEach((id, recipes) -> {
				var category = categoryProvider.get(
						(CategoryIdentifier) id,
						Objects.requireNonNull(IconProviders.get(recipeType), recipeType::toString).get(recipes),
						(Collection) recipes);
				categories.put(recipeType.categoryId, new CategoryHolder(category, (Collection) recipes));
				registry.addRecipeCategories(category);
				var workstationRegister = WorkstationRegisters.get(recipeType);
				if (workstationRegister != null) {
					workstationRegister.consume(registry, category, (Collection) recipes);
				}
			});
		}

		CategoryExtensionProvider<Display> extensionProvider = (display, category, lastView) -> {
			if (display instanceof LycheeDisplay<?> lycheeDisplay) {
				var recipe = lycheeDisplay.recipe();
				return new DisplayCategoryView<>() {
					@Override
					public DisplayRenderer getDisplayRenderer(Display display) {
						return lastView.getDisplayRenderer(display);
					}

					@Override
					public List<Widget> setupDisplay(Display display, Rectangle bounds) {
						List<Widget> widgets = lastView.setupDisplay(display, bounds);
						Rect2i rect = null;
						for (Widget widget : widgets) {
							if (widget instanceof Arrow arrow) {
								rect = new Rect2i(
										arrow.getBounds().getCenterX() - bounds.getX() - 4,
										Math.max(arrow.getY() - bounds.getY() - 9, 4),
										8,
										8);
								break;
							}
						}
						if (rect != null) {
							LycheeCategory.drawInfoBadgeIfNeeded(widgets, recipe, bounds.getLocation(), rect);
						}
						return widgets;
					}
				};
			}
			return lastView;
		};
		registry.get(CategoryIdentifier.of("minecraft", "plugins/crafting")).registerExtension(extensionProvider);
		registry.get(CategoryIdentifier.of("minecraft", "plugins/anvil")).registerExtension(extensionProvider);
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		categories.asMap().forEach((id, categories) -> {
			var displayRegister = DisplayRegisters.get(id);
			for (var category : categories) {
				displayRegister.consume(
						registry,
						(LycheeDisplayCategory) category.category,
						(Collection) category.recipes);
			}
		});

		var registryAccess = Minecraft.getInstance().level.registryAccess();
		try {
			KUtil.getRecipes(RecipeTypes.ANVIL_CRAFTING).stream()
					.filter(it ->
							!it.value().getResultItem(registryAccess).isEmpty() &&
									!it.value().isSpecial() && !it.value().hideInRecipeViewer())
					.map(AnvilCraftingDisplay::new)
					.forEach(registry::add);
		} catch (Throwable e) {
			Lychee.LOGGER.error("", e);
		}

		registry.registerVisibilityPredicate((DisplayCategory<?> category, Display display) -> {
			if (display instanceof LycheeDisplay<?> lycheeDisplay && lycheeDisplay.recipe().hideInRecipeViewer()) {
				return EventResult.interruptFalse();
			}
			return EventResult.pass();
		});
	}

	@Override
	public void registerEntryTypes(EntryTypeRegistry registration) {
		registration.register(POST_ACTION, new PostActionIngredientHelper());
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
			LycheeDisplayCategory<?> category,
			Collection<RecipeHolder<ILycheeRecipe<LycheeContext>>> recipes) {}
}
