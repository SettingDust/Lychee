package snownee.lychee.util.action;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface PostActionRenderer<T extends PostAction> {

	Map<PostActionType<?>, PostActionRenderer<?>> RENDERERS = Maps.newHashMap();
	PostActionRenderer<PostAction> DEFAULT = new PostActionRenderer<>() {
	};

	static <T extends PostAction> PostActionRenderer<T> of(PostAction action) {
		return (PostActionRenderer<T>) Objects.requireNonNull(RENDERERS.getOrDefault(action.type(), DEFAULT));
	}

	static <T extends PostAction> void register(PostActionType<T> type, PostActionRenderer<T> renderer) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(renderer);
		RENDERERS.put(type, renderer);
	}

	static List<Component> getTooltipsFromRandom(RandomSelect randomSelect, PostAction child, @Nullable Player player) {
		var index = -1;
		for (int i = 0; i < randomSelect.entries.size(); i++) {
			if (randomSelect.entries.get(i).action().equals(child)) {
				index = i;
			}
		}
		var list = randomSelect.entries.size() == 1 && randomSelect.emptyWeight == 0 ? Lists.newArrayList(
				randomSelect.getDisplayName()) : PostActionRenderer.of(child).getBaseTooltips(child, player);
		if (index == -1) {
			return list; //TODO nested actions?
		}
		if (randomSelect.entries.size() > 1 || randomSelect.emptyWeight > 0) {
			var chance = CommonProxy.chance(randomSelect.entries.get(index).weight() / (float) randomSelect.totalWeight);
			if (randomSelect.rolls == BoundsExtensions.ONE) {
				list.add(Component.translatable("tip.lychee.randomChance.one", chance)
						.withStyle(ChatFormatting.YELLOW));
			} else {
				list.add(Component.translatable(
						"tip.lychee.randomChance",
						chance,
						BoundsExtensions.getDescription(randomSelect.rolls)
				).withStyle(ChatFormatting.YELLOW));
			}
		}
		var c = randomSelect.conditions().showingCount() + child.conditions().showingCount();
		if (c > 0) {
			list.add(ClientProxy.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		var mc = Minecraft.getInstance();
		randomSelect.conditions().appendToTooltips(list, mc.level, mc.player, 0);
		child.conditions().appendToTooltips(list, mc.level, mc.player, 0);
		return list;
	}

	default void render(T action, GuiGraphics graphics, int x, int y) {
	}

	default List<Component> getBaseTooltips(T action, @Nullable Player player) {
		return Lists.newArrayList(action.getDisplayName());
	}

	default List<Component> getTooltips(T action, @Nullable Player player) {
		var list = getBaseTooltips(action, player);
		var c = action.conditions().showingCount();
		if (c > 0) {
			list.add(ClientProxy.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		var mc = Minecraft.getInstance();
		action.conditions().appendToTooltips(list, mc.level, player, 0);
		return list;
	}

	default void loadCatalystsInfo(
			T action,
			ILycheeRecipe<?> recipe,
			List<IngredientInfo> ingredients) {}
}
