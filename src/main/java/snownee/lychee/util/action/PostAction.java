package snownee.lychee.util.action;

import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualPredicate;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface PostAction<Action extends PostAction<Action>> extends PostActionDisplay, ContextualPredicate {
	Codec<PostAction<?>> CODEC = LycheeRegistries.POST_ACTION.byNameCodec().dispatch(
			PostAction::type,
			PostActionType::codec
	);

	Optional<String> getPath();

	void setPath(String path);

	PostActionType<Action> type();

	default void validate(@Nullable ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {}

	void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times);

	@Override
	default Component getDisplayName() {
		return Component.translatable(CommonProxy.makeDescriptionId(
				"postAction",
				LycheeRegistries.POST_ACTION.getKey(type())
		));
	}

	default boolean repeatable() {
		return true;
	}

	default void getUsedPointers(@Nullable ILycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {}

	default void onFailure(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {}

	@Override
	default String toJsonString() {
		return GsonHelper.toStableString(type().codec()
											   .encodeStart(JsonOps.INSTANCE, (Action) this)
											   .get()
											   .left()
											   .orElseThrow());
	}
}
