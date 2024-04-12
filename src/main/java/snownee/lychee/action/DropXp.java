package snownee.lychee.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record DropXp(PostActionCommonProperties commonProperties, int xp) implements PostAction {
	@Override
	public PostActionType<DropXp> type() {
		return PostActionTypes.DROP_XP;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParamsContext.get(LootContextParams.ORIGIN);
		ExperienceOrb.award((ServerLevel) context.level(), pos, xp * times);
	}

	@Override
	public Component getDisplayName() {
		return ClientProxy.format(CommonProxy.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(type())), xp);
	}

	public static class Type implements PostActionType<DropXp> {
		public static final MapCodec<DropXp> CODEC = RecordCodecBuilder.mapCodec(instance ->
				instance.group(
						PostActionCommonProperties.MAP_CODEC.forGetter(DropXp::commonProperties),
						ExtraCodecs.POSITIVE_INT.fieldOf("xp").forGetter(DropXp::xp)
				).apply(instance, DropXp::new));

		@Override
		public @NotNull MapCodec<DropXp> codec() {
			return CODEC;
		}
	}
}
