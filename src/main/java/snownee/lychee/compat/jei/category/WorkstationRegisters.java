package snownee.lychee.compat.jei.category;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.recipes.LightningChannelingRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface WorkstationRegisters {
	Map<ResourceLocation, WorkstationRegister<?>> ALL = Maps.newHashMap();

	WorkstationRegister<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			RecipeTypes.BLOCK_CRUSHING,
			(registry, category, recipes) -> {
				recipes.stream()
						.<BlockPredicate>mapMulti((recipe, consumer) -> {
							var blockPredicate = recipe.value().blockPredicate();
							if (!BlockPredicateExtensions.isAny(blockPredicate)) {
								consumer.accept(blockPredicate);
							}
						}).distinct()
						.flatMap(it -> BlockPredicateExtensions.matchedBlocks(it).stream()).distinct()
						.forEach((block) -> {
							var item = block.asItem();
							if (!item.equals(Items.AIR)) {
								registry.addRecipeCatalyst(item.getDefaultInstance(), category.getRecipeType());
							}
						});
			});

	WorkstationRegister<BlockExplodingRecipe> BLOCK_EXPLODING = register(
			RecipeTypes.BLOCK_EXPLODING,
			(registry, category, recipes) -> {
				for (Item item : CommonProxy.tagElements(BuiltInRegistries.ITEM, LycheeTags.BLOCK_EXPLODING_CATALYSTS)) {
					registry.addRecipeCatalyst(item.getDefaultInstance(), category.getRecipeType());
				}
			}
	);

	WorkstationRegister<DripstoneRecipe> DRIPSTONE = register(
			RecipeTypes.DRIPSTONE_DRIPPING,
			(registry, category, recipes) -> registry.addRecipeCatalyst(
					Items.POINTED_DRIPSTONE.getDefaultInstance(),
					category.getRecipeType())
	);

	WorkstationRegister<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			RecipeTypes.LIGHTNING_CHANNELING,
			(registry, category, recipes) -> registry.addRecipeCatalyst(
					Items.LIGHTNING_ROD.getDefaultInstance(),
					category.getRecipeType())
	);

	WorkstationRegister<ItemExplodingRecipe> ITEM_EXPLODING = register(
			RecipeTypes.ITEM_EXPLODING,
			(registry, category, recipes) -> {
				for (Item item : CommonProxy.tagElements(BuiltInRegistries.ITEM, LycheeTags.ITEM_EXPLODING_CATALYSTS)) {
					registry.addRecipeCatalyst(item.getDefaultInstance(), category.getRecipeType());
				}
			}
	);

	static <R extends ILycheeRecipe<LycheeContext>> WorkstationRegister<R> get(LycheeRecipeType<R> type) {
		return (WorkstationRegister<R>) ALL.get(type.categoryId);
	}

	static <R extends ILycheeRecipe<LycheeContext>> WorkstationRegister<R> register(
			LycheeRecipeType<R> type,
			WorkstationRegister<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	@FunctionalInterface
	interface WorkstationRegister<R extends ILycheeRecipe<LycheeContext>> {
		void consume(
				IRecipeCatalystRegistration registry,
				AbstractLycheeCategory<R> category,
				Collection<RecipeHolder<R>> recipes);
	}
}
