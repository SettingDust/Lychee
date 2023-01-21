package snownee.lychee.core.post.input;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.ShapedRecipe;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.Reference;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.LUtil;
import snownee.lychee.util.json.JsonPatch;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.json.JsonSchema;

public class SetItem extends PostAction {

	public final ItemStack stack;
	public final Reference target;

	public SetItem(ItemStack stack, Reference target) {
		this.stack = stack;
		this.target = target;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.SET_ITEM;
	}

	@Override
	public void preApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (path != null) {
			JsonPatch.replace(ctx.json, new JsonPointer(path), LUtil.tagToJson(stack.save(new CompoundTag())));
		}
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		IntList indexes = recipe.getItemIndexes(target);
		for (var index : indexes) {
			CompoundTag tag = ctx.getItem(index).getTag();
			ItemStack stack;
			if (path == null) {
				stack = this.stack.copy();
			} else {
				stack = ItemStack.of(LUtil.jsonToTag(new JsonPointer(path).find(ctx.json).getAsJsonObject()));
			}
			ctx.setItem(index, stack);
			if (tag != null && !stack.isEmpty()) {
				ctx.getItem(index).getOrCreateTag().merge(tag);
			}
			ctx.itemHolders.ignoreConsumptionFlags.set(index);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public List<Component> getBaseTooltips() {
		return stack.getTooltipLines(null, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
	}

	@Override
	@Environment(EnvType.CLIENT)
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
	public boolean canRepeat() {
		return false;
	}

	@Override
	public boolean validate(ILycheeRecipe<?> recipe) {
		return !recipe.getItemIndexes(target).isEmpty();
	}

	@Override
	public @Nullable JsonSchema.Node generateSchema() {
		JsonSchema.Anchor anchor = new JsonSchema.Anchor("item", path);
		anchor.override = LUtil.tagToJson(stack.save(new CompoundTag()));
		return anchor;
	}

	public static class Type extends PostActionType<SetItem> {

		@Override
		public SetItem fromJson(JsonObject o) {
			ItemStack stack;
			if ("minecraft:air".equals(Objects.toString(ResourceLocation.tryParse(o.get("item").getAsString())))) {
				stack = ItemStack.EMPTY;
			} else {
				stack = ShapedRecipe.itemStackFromJson(o);
			}
			return new SetItem(stack, Reference.fromJson(o, "target"));
		}

		@Override
		public void toJson(SetItem action, JsonObject o) {
			LUtil.itemstackToJson(action.stack, o);
			Reference.toJson(action.target, o, "target");
		}

		@Override
		public SetItem fromNetwork(FriendlyByteBuf buf) {
			return new SetItem(buf.readItem(), Reference.fromNetwork(buf));
		}

		@Override
		public void toNetwork(SetItem action, FriendlyByteBuf buf) {
			buf.writeItem(action.stack);
			Reference.toNetwork(action.target, buf);
		}

	}

}
