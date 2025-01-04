package snownee.lychee.compat.rei;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import dev.architectury.event.EventResult;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Arrow;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.extension.CategoryExtensionProvider;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.KUtil;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.rei.category.AbstractLycheeCategory;
import snownee.lychee.compat.rei.category.CategoryProviders;
import snownee.lychee.compat.rei.category.IconProviders;
import snownee.lychee.compat.rei.category.LycheeCategory;
import snownee.lychee.compat.rei.category.WorkstationRegisters;
import snownee.lychee.compat.rei.display.AnvilCraftingDisplay;
import snownee.lychee.compat.rei.display.DisplayRegisters;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.LEntryWidget;
import snownee.lychee.compat.rei.elements.ScreenElementWidget;
import snownee.lychee.compat.rei.ingredient.PostActionIngredientHelper;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

@REIPluginClient
public class LycheeREIPlugin implements REIClientPlugin {
	public static final ResourceLocation ID = Lychee.id("main");
	public static final EntryType<PostAction> POST_ACTION = EntryType.deferred(Lychee.id("post_action"));

	private final Multimap<AbstractLycheeCategory<?>, RecipeHolder<? extends ILycheeRecipe<LycheeContext>>> categories = LinkedHashMultimap.create();

	@Override
	public void registerCategories(CategoryRegistry registry) {
		categories.clear();
		for (var recipeType : RecipeTypes.ALL) {
			if (!recipeType.hasStandaloneCategory) {
				continue;
			}

			var generatedCategories = JEIREI.generateCategories(recipeType, CategoryIdentifier::of);

			var categoryProvider = CategoryProviders.get(recipeType);

			if (categoryProvider == null) {
				Lychee.LOGGER.error("Missing category provider for {}", recipeType);
				continue;
			}

			generatedCategories.forEach((id, recipes) -> {
				var registeredCategory = categories.keys().stream().filter(it -> it.getCategoryIdentifier().equals(id)).findAny();
				var category = registeredCategory.orElseGet(() -> categoryProvider.get(
						(CategoryIdentifier) id,
						Objects.requireNonNull(IconProviders.get(recipeType), recipeType::toString).get(recipes),
						(List) recipes));
				categories.putAll(category, recipes);
				if (registeredCategory.isEmpty()) {
					registry.add(category);
				}
				var workstationRegister = WorkstationRegisters.get(recipeType);
				if (workstationRegister != null) {
					workstationRegister.consume(registry, (AbstractLycheeCategory) category, (Collection) recipes);
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
		registry.get(CategoryIdentifier.of("plugins/crafting")).registerExtension(extensionProvider);
		registry.get(CategoryIdentifier.of("plugins/anvil")).registerExtension(extensionProvider);
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		categories.asMap().forEach((category, recipes) -> {
			var displayRegister = DisplayRegisters.get(category.recipeType().categoryId);
			displayRegister.consume(
					registry,
					(AbstractLycheeCategory) category,
					(Collection) recipes);
		});

		try {
			KUtil.getRecipes(RecipeTypes.ANVIL_CRAFTING).stream()
					.filter(it ->
							!it.value().output().isEmpty() &&
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

	public static Rectangle offsetRect(Point startPoint, Rect2i rect) {
		return new Rectangle(startPoint.x + rect.getX(), startPoint.y + rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public static LEntryWidget slot(Point startPoint, int x, int y, SlotType slotType) {
		LEntryWidget widget = new LEntryWidget(new Point(startPoint.x + x + 1, startPoint.y + y + 1));
		widget.background(slotType.element);
		return widget;
	}

	@Override
	public void registerEntryTypes(EntryTypeRegistry registration) {
		registration.register(POST_ACTION, new PostActionIngredientHelper());
	}

	public enum SlotType {
		NORMAL(AllGuiTextures.JEI_SLOT),
		CHANCE(AllGuiTextures.JEI_CHANCE_SLOT),
		CATALYST(AllGuiTextures.JEI_CATALYST_SLOT);

		final ScreenElement element;

		SlotType(AllGuiTextures element) {
			this.element = new ScreenElementWidget(element).element;
		}
	}
}
