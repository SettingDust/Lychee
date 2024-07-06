package snownee.lychee.compat.jei.category;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipeType;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public final class BlockCrushingRecipeCategory extends AbstractLycheeCategory<BlockCrushingRecipe> {

	public static final Rect2i FALLING_BLOCK_RECT = new Rect2i(0, -35, 20, 35);
	public static final Rect2i LANDING_BLOCK_RECT = new Rect2i(0, 0, 20, 20);

	public BlockCrushingRecipeCategory(RecipeType<BlockCrushingRecipe> recipeType, IDrawable icon, IGuiHelper guiHelper) {
		super(recipeType, icon, guiHelper);
	}

	@Override
	public int contentWidth() {
		return WIDTH + 50;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, BlockCrushingRecipe recipe, IFocusGroup focuses) {
		var xCenter = getWidth() / 2;
		var y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		ingredientGroup(builder, recipe, xCenter - 45, y);
		actionGroup(builder, recipe, xCenter + 50, y);
		LycheeCategory.addBlockIngredients(builder, recipe);
	}

	@Override
	public void draw(
			BlockCrushingRecipe recipe,
			IRecipeSlotsView recipeSlotsView,
			GuiGraphics guiGraphics,
			double mouseX,
			double mouseY
	) {
		drawInfoBadgeIfNeeded(guiGraphics, recipe, mouseX, mouseY);

		var fallingBlock = getFallingBlock(recipe);
		var landingBlock = getLandingBlock(recipe);

		var x = recipe.getIngredients().isEmpty() ? 41 : 77;
		var anyLandingBlock = BlockPredicateExtensions.isAny(recipe.landingBlock());
		var y = anyLandingBlock ? 45 : 36;

		var ticks = (System.currentTimeMillis() % 2000) / 1000F;
		ticks = Math.min(1, ticks);
		ticks = ticks * ticks * ticks * ticks;

		var matrixStack = guiGraphics.pose();
		if (landingBlock.getLightEmission() < 5) {
			matrixStack.pushPose();
			matrixStack.translate(x + 10.5, y + (anyLandingBlock ? 1 : 16), 0);
			var shadow = 0.6F;
			if (anyLandingBlock) {
				shadow = 0.2F + ticks * 0.2F;
			}
			matrixStack.scale(shadow, shadow, shadow);
			matrixStack.translate(-26, -5.5, 0);
			AllGuiTextures.JEI_SHADOW.render(guiGraphics, 0, 0);
			matrixStack.popPose();
		}

		matrixStack.pushPose();
		matrixStack.translate(x, y - 13, 0);
		GuiGameElement.of(fallingBlock)
				.scale(15)
				.atLocal(0, ticks * 1.3 - 1.3, 0)
				.rotateBlock(20, 225, 0)
				.lighting(JEIREI.BLOCK_LIGHTING)
				.at(0, 0, 300)
				.render(guiGraphics);
		if (!landingBlock.isAir()) {
			GuiGameElement.of(landingBlock).scale(15).atLocal(0, 1, 0).rotateBlock(20, 225, 0).lighting(JEIREI.BLOCK_LIGHTING).render(
					guiGraphics);
		}
		matrixStack.popPose();
	}

	@Override
	public List<Component> getTooltipStrings(BlockCrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		var x = recipe.getIngredients().isEmpty() ? 41 : 77;
		var anyLandingBlock = BlockPredicateExtensions.isAny(recipe.landingBlock());
		var y = anyLandingBlock ? 45 : 36;
		x = (int) mouseX - x;
		y = (int) mouseY - y;
		if (FALLING_BLOCK_RECT.contains(x, y)) {
			var fallingBlock = CommonProxy.getCycledItem(
					BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
					Blocks.ANVIL.defaultBlockState(),
					2000);
			return BlockPredicateExtensions.getTooltips(fallingBlock, recipe.blockPredicate());
		}
		if (!anyLandingBlock && LANDING_BLOCK_RECT.contains(x, y)) {
			var landingBlock = CommonProxy.getCycledItem(
					BlockPredicateExtensions.getShowcaseBlockStates(recipe.landingBlock()),
					Blocks.AIR.defaultBlockState(),
					2000);
			return BlockPredicateExtensions.getTooltips(landingBlock, recipe.landingBlock());
		}
		return super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
	}

	@Override
	public boolean handleInput(BlockCrushingRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
		if (input.getType() == InputConstants.Type.MOUSE) {
			var x = recipe.getIngredients().isEmpty() ? 41 : 77;
			var anyLandingBlock = BlockPredicateExtensions.isAny(recipe.landingBlock());
			var y = anyLandingBlock ? 45 : 36;
			x = (int) mouseX - x;
			y = (int) mouseY - y;
			if (FALLING_BLOCK_RECT.contains(x, y)) {
				BlockState fallingBlock = CommonProxy.getCycledItem(
						BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
						Blocks.ANVIL.defaultBlockState(),
						2000);
				return clickBlock(fallingBlock, input);
			}
			if (!anyLandingBlock && LANDING_BLOCK_RECT.contains(x, y)) {
				BlockState landingBlock = CommonProxy.getCycledItem(
						BlockPredicateExtensions.getShowcaseBlockStates(recipe.landingBlock()),
						Blocks.AIR.defaultBlockState(),
						2000);
				return clickBlock(landingBlock, input);
			}
		}
		return super.handleInput(recipe, mouseX, mouseY, input);
	}

	private BlockState getFallingBlock(BlockCrushingRecipe recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
				Blocks.AIR.defaultBlockState(),
				2000);
	}

	private BlockState getLandingBlock(BlockCrushingRecipe recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.landingBlock()),
				Blocks.AIR.defaultBlockState(),
				2000);
	}

	@Override
	public BlockCrushingRecipeType recipeType() {
		return RecipeTypes.BLOCK_CRUSHING;
	}
}
