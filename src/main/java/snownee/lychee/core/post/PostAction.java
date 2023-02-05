package snownee.lychee.core.post;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.MutableTriple;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.LUtil;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.json.JsonSchema;

public abstract class PostAction extends ContextualHolder {

	@Nullable
	public String path;

	public abstract PostActionType<?> getType();

	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (int i = 0; i < times; i++) {
			apply(recipe, ctx, times);
		}
	}

	protected abstract void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times);

	public List<ItemStack> getOutputItems() {
		return List.of();
	}

	public static void parseActions(JsonElement element, Consumer<PostAction> consumer) {
		if (element == null) {
		} else if (element.isJsonObject()) {
			consumer.accept(parse(element.getAsJsonObject()));
		} else {
			JsonArray array = element.getAsJsonArray();
			for (int x = 0; x < array.size(); x++) {
				consumer.accept(parse(array.get(x).getAsJsonObject()));
			}
		}
	}

	public static PostAction parse(JsonObject o) {
		ResourceLocation key = new ResourceLocation(o.get("type").getAsString());
		PostActionType<?> type = LycheeRegistries.POST_ACTION.getValue(key);
		PostAction action = type.fromJson(o);
		action.parseConditions(o.get("contextual"));
		if (o.has("_path")) {
			action.path = GsonHelper.getAsString(o, "_path");
		}
		return action;
	}

	public Component getDisplayName() {
		return Component.translatable(LUtil.makeDescriptionId("postAction", getType().getRegistryName()));
	}

	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack poseStack, int x, int y) {
		AllGuiTextures.JEI_QUESTION_MARK.render(poseStack, x + 2, y + 1);
	}

	@OnlyIn(Dist.CLIENT)
	public List<Component> getBaseTooltips() {
		return Lists.newArrayList(getDisplayName());
	}

	@OnlyIn(Dist.CLIENT)
	public List<Component> getTooltips() {
		List<Component> list = getBaseTooltips();
		int c = showingConditionsCount();
		if (c > 0) {
			list.add(LUtil.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		getConditonTooltips(list, 0);
		return list;
	}

	public boolean isHidden() {
		return false;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	public boolean canRepeat() {
		return true;
	}

	public final JsonObject toJson() {
		JsonObject o = new JsonObject();
		o.addProperty("type", getType().getRegistryName().toString());
		if (!getConditions().isEmpty()) {
			o.add("contextual", rawConditionsToJson());
		}
		if (!Strings.isNullOrEmpty(path)) {
			o.addProperty("_path", path);
		}
		((PostActionType<PostAction>) getType()).toJson(this, o);
		return o;
	}

	public boolean validate(ILycheeRecipe<?> recipe) {
		return true;
	}

	public void loadCatalystsInfo(ILycheeRecipe<?> recipe, List<MutableTriple<Ingredient, Component, Integer>> ingredients) {
	}

	public void getUsedPointers(ILycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {
	}

	public @Nullable JsonSchema.Node generateSchema() {
		return null;
	}

	public void preApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
	}

}
