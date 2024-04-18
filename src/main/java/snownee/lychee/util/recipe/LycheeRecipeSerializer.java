package snownee.lychee.util.recipe;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.util.SerializableType;

public interface LycheeRecipeSerializer<T extends ILycheeRecipe<?>> extends RecipeSerializer<T>, SerializableType<T> {
	@Override
	@NotNull
	MapCodec<T> codec();

	@Override
	@NotNull
	default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return ByteBufCodecs.fromCodecWithRegistries(codec().codec());
	}
}
