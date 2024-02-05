package snownee.lychee.util;

import java.text.MessageFormat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import snownee.kiwi.util.KEvent;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.DropXp;
import snownee.lychee.action.Execute;
import snownee.lychee.action.Explode;
import snownee.lychee.action.Hurt;
import snownee.lychee.action.input.SetItem;
import snownee.lychee.client.core.post.CycleStatePropertyPostActionRenderer;
import snownee.lychee.client.core.post.IfPostActionRenderer;
import snownee.lychee.client.core.post.ItemBasedPostActionRenderer;
import snownee.lychee.client.core.post.ItemStackPostActionRenderer;
import snownee.lychee.client.core.post.PlaceBlockPostActionRenderer;
import snownee.lychee.client.core.post.PostActionRenderer;
import snownee.lychee.recipes.dripstone_dripping.DripstoneRecipeMod;
import snownee.lychee.recipes.dripstone_dripping.client.ParticleFactories;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ClientProxy implements ClientModInitializer {

	private static final KEvent<RecipeViewerWidgetClickListener> RECIPE_VIEWER_WIDGET_CLICK_EVENT =
			KEvent.createArrayBacked(RecipeViewerWidgetClickListener.class, listeners -> (recipe, button) -> {
				for (RecipeViewerWidgetClickListener listener : listeners) {
					if (listener.onClick(recipe, button)) {
						return true;
					}
				}
				return false;
			});

	public static MutableComponent getDimensionDisplayName(ResourceKey<Level> dimension) {
		String key = Util.makeDescriptionId("dimension", dimension.location());
		if (I18n.exists(key)) {
			return Component.translatable(key);
		} else {
			return Component.literal(CommonProxy.capitaliseAllWords(dimension.location().getPath()));
		}
	}

	public static MutableComponent getStructureDisplayName(ResourceLocation rawName) {
		String key = Util.makeDescriptionId("structure", rawName);
		if (I18n.exists(key)) {
			return Component.translatable(key);
		} else {
			return Component.literal(CommonProxy.capitaliseAllWords(rawName.getPath()));
		}
	}

	public static MutableComponent format(String s, Object... objects) {
		try {
			return Component.literal(MessageFormat.format(I18n.get(s), objects));
		} catch (Exception e) {
			return Component.translatable(s, objects);
		}
	}

	public static void registerInfoBadgeClickListener(RecipeViewerWidgetClickListener listener) {
		RECIPE_VIEWER_WIDGET_CLICK_EVENT.register(listener);
	}

	public static boolean postInfoBadgeClickEvent(ILycheeRecipe recipe, int button) {
		return RECIPE_VIEWER_WIDGET_CLICK_EVENT.invoker().onClick(recipe, button);
	}

	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry.getInstance().register(
				DripstoneRecipeMod.DRIPSTONE_DRIPPING,
				$ -> new ParticleFactories.Dripping($)
		);
		ParticleFactoryRegistry.getInstance().register(
				DripstoneRecipeMod.DRIPSTONE_FALLING,
				$ -> new ParticleFactories.Falling($)
		);
		ParticleFactoryRegistry.getInstance().register(
				DripstoneRecipeMod.DRIPSTONE_SPLASH,
				$ -> new ParticleFactories.Splash($)
		);

		PostActionRenderer.register(
				PostActionTypes.DROP_ITEM,
				(ItemStackPostActionRenderer<DropItem>) action -> action.stack
		);
		PostActionRenderer.register(
				PostActionTypes.SET_ITEM,
				(ItemStackPostActionRenderer<SetItem>) action -> action.stack
		);
		PostActionRenderer.register(
				PostActionTypes.DROP_XP,
				(ItemBasedPostActionRenderer<DropXp>) action -> Items.EXPERIENCE_BOTTLE.getDefaultInstance()
		);
		PostActionRenderer.register(
				PostActionTypes.EXECUTE,
				(ItemBasedPostActionRenderer<Execute>) action -> Items.COMMAND_BLOCK.getDefaultInstance()
		);
		PostActionRenderer.register(
				PostActionTypes.EXPLODE,
				(ItemBasedPostActionRenderer<Explode>) action -> Items.TNT.getDefaultInstance()
		);
		PostActionRenderer.register(
				PostActionTypes.HURT,
				(ItemBasedPostActionRenderer<Hurt>) action -> Items.IRON_SWORD.getDefaultInstance()
		);
		PostActionRenderer.register(PostActionTypes.IF, new IfPostActionRenderer());
		PostActionRenderer.register(PostActionTypes.PLACE, new PlaceBlockPostActionRenderer());
		PostActionRenderer.register(PostActionTypes.CYCLE_STATE_PROPERTY, new CycleStatePropertyPostActionRenderer());
	}

	@FunctionalInterface
	public interface RecipeViewerWidgetClickListener {
		boolean onClick(ILycheeRecipe recipe, int button);
	}
}
