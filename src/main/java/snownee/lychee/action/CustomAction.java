package snownee.lychee.action;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class CustomAction implements PostAction {
	private final PostActionCommonProperties commonProperties;
	public final String id;
	public final JsonObject data;
	public boolean repeatable;
	@Nullable
	public Apply applyFunc;

	public CustomAction(PostActionCommonProperties commonProperties, String id, JsonObject json, boolean repeatable) {
		this.commonProperties = commonProperties;
		this.id = id;
		this.data = json;
		this.repeatable = repeatable;
	}

	@Override
	public PostActionType<CustomAction> type() {
		return PostActionTypes.CUSTOM;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		if (applyFunc != null) {
			applyFunc.apply(recipe, context, times);
		}
	}

	@Override
	public boolean preventSync() {
		return true;
	}

	@Override
	public boolean repeatable() {
		return repeatable;
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe) {
		CommonProxy.postCustomActionEvent(id, this, recipe);
	}

	@Override
	public PostActionCommonProperties commonProperties() {
		return commonProperties;
	}

	public String id() {
		return id;
	}

	public JsonObject data() {
		return data;
	}

	@FunctionalInterface
	public interface Apply {
		void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times);
	}

	public record Data(String id, JsonObject data) {
		public static final MapCodec<Data> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(Data::id),
				ExtraCodecs.JSON.comapFlatMap(
						it -> {
							try {
								return DataResult.success(it.getAsJsonObject());
							} catch (Exception e) {
								return DataResult.error(e::getMessage);
							}
						}, Function.identity()).fieldOf("data").forGetter(Data::data)
		).apply(instance, Data::new));
	}

	public static class Type implements PostActionType<CustomAction> {
		public static final MapCodec<CustomAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(CustomAction::commonProperties),
				Codec.STRING.fieldOf("id").forGetter(CustomAction::id),
				ExtraCodecs.JSON.comapFlatMap(
						it -> {
							try {
								return DataResult.success(it.getAsJsonObject());
							} catch (Exception e) {
								return DataResult.error(e::getMessage);
							}
						}, Function.identity()).optionalFieldOf("data", new JsonObject()).forGetter(CustomAction::data),
				Codec.BOOL.optionalFieldOf("repeatable", true).forGetter(CustomAction::repeatable)
		).apply(instance, CustomAction::new));

		@Override
		public @NotNull MapCodec<CustomAction> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CustomAction> streamCodec() {
			throw new UnsupportedOperationException();
		}
	}
}
