package snownee.lychee.util.predicates;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.Pair;
import snownee.lychee.util.codec.BlockPredicateCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

public class BlockPredicateExtensions {
	private static final Cache<BlockPredicate, List<BlockState>> CACHE =
			CacheBuilder.newBuilder()
					.expireAfterAccess(10, TimeUnit.MINUTES)
					.build();
	public static final Set<Property<?>> ITERABLE_PROPERTIES = Sets.newConcurrentHashSet();
	public static final Codec<BlockPredicate> CODEC = new BlockPredicateCodec();

	static {
		ITERABLE_PROPERTIES.addAll(List.of(
				BlockStateProperties.AGE_1,
				BlockStateProperties.AGE_2,
				BlockStateProperties.AGE_3,
				BlockStateProperties.AGE_5,
				BlockStateProperties.AGE_7,
				BlockStateProperties.CANDLES,
				BlockStateProperties.BITES,
				BlockStateProperties.POWER,
				BlockStateProperties.POWERED,
				BlockStateProperties.LIT,
				BlockStateProperties.BERRIES,
				BlockStateProperties.OPEN,
				BlockStateProperties.DELAY,
				BlockStateProperties.DISTANCE,
				BlockStateProperties.LAYERS,
				BlockStateProperties.PICKLES,
				BlockStateProperties.LEVEL,
				BlockStateProperties.LEVEL_HONEY,
				BlockStateProperties.LEVEL_CAULDRON,
				BlockStateProperties.DRIPSTONE_THICKNESS
		));
	}

	public static Set<Block> matchedBlocks(BlockPredicate predicate) {
		final var blocks = Lists.<Holder<Block>>newArrayList();
		if (predicate.blocks().isPresent()) {
			blocks.addAll(predicate.blocks().orElseThrow().unwrap().swap().orThrow());
		}
		if (predicate.tag().isPresent()) {
			blocks.addAll(BuiltInRegistries.BLOCK.getOrCreateTag(predicate.tag().orElseThrow()).stream().toList());
		}
		return blocks.stream().map(Holder::value).collect(Collectors.toSet());
	}

	public static Set<Fluid> matchedFluids(BlockPredicate predicate) {
		/* off */
		return matchedBlocks(predicate).stream()
				.filter(LiquidBlock.class::isInstance)
				.map(it -> it.defaultBlockState().getFluidState())
				.filter(Predicate.not(FluidState::isEmpty))
				.map(FluidState::getType)
				.collect(Collectors.toSet());
		/* on */
	}

	public static List<ItemStack> matchedItemStacks(BlockPredicate predicate) {
		return matchedBlocks(predicate).stream()
				.map(Block::asItem)
				.filter(Predicate.not(Items.AIR::equals))
				.distinct()
				.map(Item::getDefaultInstance)
				.toList();
	}

	/**
	 * Optimized without get block state and block entity calls. And needn't pos loaded.
	 */
	public static boolean matches(BlockPredicate predicate, LycheeContext context) {
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		return unsafeMatches(
				predicate,
				lootParamsContext.get(LootContextParams.BLOCK_STATE),
				() -> lootParamsContext.getOrNull(LootContextParams.BLOCK_ENTITY)
		);
	}

	public static boolean unsafeMatches(
			BlockPredicate predicate,
			BlockState state,
			Supplier<BlockEntity> blockEntitySupplier
	) {
		if (predicate.tag().isPresent() && !state.is(predicate.tag().get())) {
			return false;
		}
		if (predicate.blocks().isPresent() && !state.is(predicate.blocks().get())) {
			return false;
		}
		if (predicate.properties().isPresent() && !predicate.properties().get().matches(state)) {
			return false;
		}

		if (predicate.nbt().isPresent()) {
			final var blockEntity = blockEntitySupplier.get();
			return blockEntity != null && predicate.nbt().get().matches(blockEntity.saveWithFullMetadata());
		}

		return true;
	}

	public static Optional<BlockPredicate> fromNetwork(FriendlyByteBuf buf) {
		return buf.readOptional((it) -> it.readWithCodecTrusted(NbtOps.INSTANCE, BlockPredicate.CODEC));
	}

	public static void toNetwork(Optional<BlockPredicate> predicate, FriendlyByteBuf buf) {
		buf.writeOptional(predicate, (it, obj) -> it.writeWithCodec(NbtOps.INSTANCE, BlockPredicate.CODEC, obj));
	}

	public static BlockState anyBlockState(BlockPredicate predicate) {
		return getShowcaseBlockStates(predicate).stream().findFirst().orElse(Blocks.AIR.defaultBlockState());
	}

	public static List<BlockState> getShowcaseBlockStates(BlockPredicate predicate) {
		try {
			return CACHE.get(predicate, () -> getShowcaseBlockStates(predicate, ITERABLE_PROPERTIES));
		} catch (ExecutionException e) {
			return List.of();
		}
	}

	public static List<BlockState> getShowcaseBlockStates(
			BlockPredicate predicate,
			Collection<Property<?>> iterableProperties
	) {
		final var blocks = matchedBlocks(predicate);
		if (blocks.isEmpty()) return List.of();

		final var states = Lists.<BlockState>newArrayList();
		final var propertiesPredicate = predicate.properties().orElseThrow();

		for (final var block : blocks) {
			final var state = block.defaultBlockState();
			final var propertyMap = ArrayListMultimap.<Property<?>, Comparable<?>>create();
			for (Property<? extends Comparable<?>> property : block.getStateDefinition().getProperties()) {
				final var name = property.getName();
				final var matcher = PropertiesPredicateExtensions.findMatcher(propertiesPredicate, name);
				if (matcher.isPresent()) {
					for (Comparable<?> object : property.getPossibleValues()) {
						if (matcher.get().match(
								block.getStateDefinition(),
								state.setValue((Property<?>) property, (Comparable) object)
						)) {
							propertyMap.put(property, object);
						}
					}
				} else if (iterableProperties.contains(property)) {
					propertyMap.putAll(property, property.getPossibleValues());
				}
			}
			var stream = Stream.of(state);
			for (final var e : propertyMap.asMap().entrySet()) {
				stream = stream.flatMap(
						$ -> e.getValue()
								.stream()
								.map(v -> $.setValue((Property<?>) e.getKey(), (Comparable) v))
				);
			}

			states.addAll(stream.toList());
		}
		return states;
	}

	@SuppressWarnings("rawtypes")
	public static List<Component> getTooltips(BlockState state, BlockPredicate predicate) {
		final var list = Lists.<Component>newArrayList(state.getBlock().getName());
		final var matchers = predicate.properties().orElseThrow().properties();
		for (final var matcher : matchers) {
			final var name = Component.literal(matcher.name() + "=").withStyle(ChatFormatting.GRAY);
			if (matcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher exactMatcher) {
				name.append(Component.literal(exactMatcher.value()).withStyle(ChatFormatting.WHITE));
			} else if (matcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher rangedMatcher) {
				final var definition = state.getBlock().getStateDefinition();
				final Property property = definition.getProperty(matcher.name());
				final var rangePair = Suppliers.memoize(() -> {
					final var sorted = property.getPossibleValues()
							.stream()
							.sorted()
							.toList();
					return Pair.of(sorted.get(0), sorted.get(sorted.size() - 1));
				});
				name.append(Component.literal(rangedMatcher.minValue().orElseGet(
						() -> property.getName((Comparable) rangePair.get().getFirst())
				)).withStyle(ChatFormatting.WHITE));
				name.append(Component.literal("~").withStyle(ChatFormatting.GRAY));
				name.append(Component.literal(rangedMatcher.minValue().orElseGet(
						() -> property.getName((Comparable) rangePair.get().getSecond())
				)).withStyle(ChatFormatting.WHITE));
			}
			list.add(name);
		}
		if (predicate.nbt().isPresent()) {
			list.add(Component.translatable("tip.lychee.nbtPredicate").withStyle(ChatFormatting.GRAY));
		}
		return list;
	}

}
