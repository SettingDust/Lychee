package snownee.lychee.util.codec;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class LycheeCodecs {
	public static final Codec<Ingredient> OPTIONAL_INGREDIENT_CODEC =
			ExtraCodecs.optionalEmptyMap(Ingredient.CODEC)
					.xmap(it -> it.orElse(Ingredient.EMPTY), Optional::of);

	public static final Codec<Pair<Ingredient, Ingredient>> PAIR_INGREDIENT_CODEC =
			Codec.withAlternative(
					LycheeCodecs.OPTIONAL_INGREDIENT_CODEC.sizeLimitedListOf(2)
							.xmap(
									it -> Pair.of(it.get(0), it.size() > 1 ? it.get(1) : Ingredient.EMPTY),
									it -> Util.make(Lists.newArrayList(it.getFirst()), (list) -> {
										if (!it.getSecond().isEmpty()) {
											list.add(it.getSecond());
										}
									})),
					LycheeCodecs.OPTIONAL_INGREDIENT_CODEC.xmap(it -> Pair.of(it, Ingredient.EMPTY), Pair::getFirst)
			);

	public static final Codec<ItemStack> PLAIN_ITEM_STACK_CODEC = Codec.withAlternative(
			ItemStack.OPTIONAL_CODEC,
			BuiltInRegistries.ITEM.holderByNameCodec().xmap(ItemStack::new, ItemStack::getItemHolder));

	public static final MapCodec<ItemStack> FLAT_ITEM_STACK_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
							BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(ItemStack::getItemHolder),
							ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
							DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
									.forGetter(ItemStack::getComponentsPatch)
					)
					.apply(instance, ItemStack::new)
	);

	public static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(posInstance -> posInstance.group(
			Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX),
			Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY),
			Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ)
	).apply(posInstance, (x, y, z) -> {
		if (x == 0 && y == 0 && z == 0) {
			return BlockPos.ZERO;
		}
		return new BlockPos(x, y, z);
	}));

	//TODO move to Kiwi
	public static <T> Codec<List<T>> compactList(Codec<T> elementCodec) {
		return Codec.withAlternative(elementCodec.listOf(), elementCodec, List::of);
	}
}
