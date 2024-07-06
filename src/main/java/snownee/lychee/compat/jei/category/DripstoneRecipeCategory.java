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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class DripstoneRecipeCategory extends AbstractLycheeCategory<DripstoneRecipe> {

	private final Rect2i sourceBlockRect = new Rect2i(23, 1, 16, 16);
	private final Rect2i targetBlockRect = new Rect2i(23, 43, 16, 16);

	public DripstoneRecipeCategory(RecipeType<DripstoneRecipe> recipeType, IDrawable icon, IGuiHelper guiHelper) {
		super(recipeType, icon, guiHelper);
		infoRect.setX(-10);
	}


	private static void drawBlock(BlockState state, GuiGraphics graphics, double localX, double localY, double localZ) {
		GuiGameElement.of(state)
				.scale(12)
				.lighting(JEIREI.BLOCK_LIGHTING)
				.atLocal(localX, localY, localZ)
				.rotateBlock(12.5, -22.5, 0)
				.render(graphics);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DripstoneRecipe recipe, IFocusGroup focuses) {
		int y = recipe.conditions().showingCount() > 9 ? 26 : 28;
		actionGroup(builder, recipe, getWidth() - 28, y);
		LycheeCategory.addBlockIngredients(builder, recipe);
	}

	@Override
	public void draw(
			DripstoneRecipe recipe,
			IRecipeSlotsView recipeSlotsView,
			GuiGraphics graphics,
			double mouseX,
			double mouseY
	) {
		drawInfoBadgeIfNeeded(graphics, recipe, mouseX, mouseY);
		BlockState sourceBlock = CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.sourceBlock()),
				Blocks.AIR.defaultBlockState(),
				2000);
		BlockState targetBlock = CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
				Blocks.AIR.defaultBlockState(),
				2000);

		var matrixStack = graphics.pose();
		if (targetBlock.getLightEmission() < 5) {
			matrixStack.pushPose();
			matrixStack.translate(31, 56, 0);
			float shadow = 0.5F;
			matrixStack.scale(shadow, shadow, shadow);
			matrixStack.translate(-26, -5.5, 0);
			AllGuiTextures.JEI_SHADOW.render(graphics, 0, 0);
			matrixStack.popPose();
		}

		matrixStack.pushPose();
		matrixStack.translate(22, 24, 300);
		drawBlock(sourceBlock, graphics, 0, -2, 0);
		drawBlock(Blocks.DRIPSTONE_BLOCK.defaultBlockState(), graphics, 0, -1, 0);
		drawBlock(
				Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN),
				graphics,
				0,
				0,
				0);
		drawBlock(targetBlock, graphics, 0, 1.5, 0);
		matrixStack.popPose();
	}

	@Override
	public List<Component> getTooltipStrings(DripstoneRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		int x = (int) mouseX;
		int y = (int) mouseY;
		if (sourceBlockRect.contains(x, y)) {
			BlockState sourceBlock = CommonProxy.getCycledItem(
					BlockPredicateExtensions.getShowcaseBlockStates(recipe.sourceBlock()),
					Blocks.AIR.defaultBlockState(),
					2000);
			return BlockPredicateExtensions.getTooltips(sourceBlock, recipe.sourceBlock());
		}
		if (targetBlockRect.contains(x, y)) {
			BlockState targetBlock = CommonProxy.getCycledItem(
					BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
					Blocks.AIR.defaultBlockState(),
					2000);
			return BlockPredicateExtensions.getTooltips(targetBlock, recipe.blockPredicate());
		}
		return super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
	}

	@Override
	public boolean handleInput(DripstoneRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
		if (input.getType() == InputConstants.Type.MOUSE) {
			int x = (int) mouseX;
			int y = (int) mouseY;
			if (sourceBlockRect.contains(x, y)) {
				BlockState fallingBlock = CommonProxy.getCycledItem(
						BlockPredicateExtensions.getShowcaseBlockStates(recipe.sourceBlock()),
						Blocks.AIR.defaultBlockState(),
						2000);
				return clickBlock(fallingBlock, input);
			}
			if (targetBlockRect.contains(x, y)) {
				BlockState landingBlock = CommonProxy.getCycledItem(
						BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
						Blocks.AIR.defaultBlockState(),
						2000);
				return clickBlock(landingBlock, input);
			}
		}
		return super.handleInput(recipe, mouseX, mouseY, input);
	}

	private BlockState getSourceBlock(DripstoneRecipe recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.sourceBlock()),
				Blocks.AIR.defaultBlockState(),
				2000);
	}

	private BlockState getTargetBlock(DripstoneRecipe recipe) {
		return CommonProxy.getCycledItem(
				BlockPredicateExtensions.getShowcaseBlockStates(recipe.blockPredicate()),
				Blocks.AIR.defaultBlockState(),
				2000);
	}

	@Override
	public LycheeRecipeType<? extends DripstoneRecipe> recipeType() {
		return RecipeTypes.DRIPSTONE_DRIPPING;
	}
}
