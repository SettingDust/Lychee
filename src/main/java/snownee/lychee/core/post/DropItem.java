package snownee.lychee.core.post;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.LycheeTags;
import snownee.lychee.PostActionTypes;
import snownee.lychee.block_crushing.BlockCrushingRecipe;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.mixin.ItemEntityAccess;
import snownee.lychee.util.LUtil;
import snownee.lychee.util.json.JsonPointer;

public class DropItem extends PostAction {

	public final ItemStack stack;

	public DropItem(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.DROP_ITEM;
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		if (recipe instanceof BlockCrushingRecipe) {
			BlockState landingBlock = ctx.getParam(LootContextParams.BLOCK_STATE);
			if (landingBlock.is(LycheeTags.EXTEND_BOX)) {
				pos = Vec3.atCenterOf(ctx.getParam(LycheeLootContextParams.BLOCK_POS));
			}
		}
		ItemStack stack;
		if (path == null) {
			stack = this.stack.copy();
		} else {
			stack = ItemStack.of(LUtil.jsonToTag(new JsonPointer(path).find(ctx.json)));
		}
		stack.setCount(stack.getCount() * times);
		if (ctx.getClass() == BlockExplodingContext.class) {
			ctx.itemHolders.tempList.add(stack);
		} else {
			LUtil.dropItemStack(ctx.getLevel(), pos.x, pos.y, pos.z, stack, $ -> {
				((ItemEntityAccess) $).setHealth(80);
			});
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Component> getBaseTooltips() {
		return stack.getTooltipLines(null, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		GuiGameElement.of(stack).render(poseStack, x, y);
	}

	@Override
	public Component getDisplayName() {
		return stack.getHoverName();
	}

	@Override
	public List<ItemStack> getItemOutputs() {
		return List.of(stack);
	}

	@Override
	public JsonElement provideJsonInfo(ILycheeRecipe<?> recipe, JsonPointer pointer, JsonObject recipeObject) {
		path = pointer.toString();
		return LUtil.tagToJson(stack.save(new CompoundTag()));
	}

	public static class Type extends PostActionType<DropItem> {

		@Override
		public DropItem fromJson(JsonObject o) {
			return new DropItem(ShapedRecipe.itemStackFromJson(o));
		}

		@Override
		public void toJson(DropItem action, JsonObject o) {
			LUtil.itemstackToJson(action.stack, o);
		}

		@Override
		public DropItem fromNetwork(FriendlyByteBuf buf) {
			return new DropItem(buf.readItem());
		}

		@Override
		public void toNetwork(DropItem action, FriendlyByteBuf buf) {
			buf.writeItem(action.stack);
		}

	}
}
