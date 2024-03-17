package snownee.lychee.action;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.json.JsonPointer;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RandomSelect implements CompoundAction, PostAction {
	public final List<PostAction> entries;
	public final List<Integer> weights;
	private final PostActionCommonProperties commonProperties;
	public final MinMaxBounds.Ints rolls;
	public final boolean canRepeat;
	public final boolean hidden;
	public final boolean preventSync;
	public final int totalWeight;
	public final int emptyWeight;

	public RandomSelect(
			PostActionCommonProperties commonProperties,
			List<PostAction> entries,
			List<Integer> weights,
			int totalWeight,
			int emptyWeight,
			MinMaxBounds.Ints rolls
	) {
		Preconditions.checkArgument(entries.size() == weights.size());
		this.commonProperties = commonProperties;
		this.entries = entries;
		this.weights = weights;
		this.totalWeight = totalWeight;
		this.emptyWeight = emptyWeight;
		this.rolls = rolls;
		canRepeat = entries.stream().allMatch(PostAction::repeatable);
		hidden = entries.stream().allMatch(PostAction::hidden);
		preventSync = entries.stream().allMatch(PostAction::preventSync);
	}

	public RandomSelect(
			PostActionCommonProperties commonProperties,
			List<PostAction> entries,
			List<Integer> weights,
			int emptyWeight,
			MinMaxBounds.Ints rolls
	) {
		this(commonProperties, entries, weights, weights.stream().mapToInt(it -> it).sum() + emptyWeight, emptyWeight, rolls);
	}

	@Override
	public PostActionCommonProperties commonProperties() {
		return commonProperties;
	}

	@Override
	public PostActionType<RandomSelect> type() {
		return PostActionTypes.RANDOM;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var randomSource = context.get(LycheeContextKey.RANDOM);
		times *= BoundsExtensions.random(rolls, randomSource);
		if (times == 0) {
			return;
		}

		var validActions = Lists.<PostAction>newArrayList();
		var validWeights = new int[entries.size()];
		var totalWeights = 0;
		for (var i = 0; i < entries.size(); i++) {
			var entry = entries.get(i);
			if (entry.test(recipe, context, 1) == 1) {
				validWeights[validActions.size()] = weights.get(i);
				validActions.add(entry);
				totalWeights += weights.get(i);
			}
		}
		if (validActions.isEmpty()) {
			return;
		}
		totalWeights += emptyWeight;
		var childTimes = new int[validActions.size()];
		for (var i = 0; i < times; i++) {
			var index = getRandomEntry(randomSource, validWeights, totalWeights);
			if (index >= 0) {
				++childTimes[index];
			}
		}
		var actionContext = context.get(LycheeContextKey.ACTION);
		for (var i = 0; i < validActions.size(); i++) {
			if (childTimes[i] > 0) {
				actionContext.jobs.offer(new Job(validActions.get(i), childTimes[i]));
			}
		}
	}

	private int getRandomEntry(RandomSource random, int[] weights, int totalWeights) {
		var j = random.nextInt(totalWeights);
		for (var i = 0; i < weights.length; i++) {
			j -= weights[i];
			if (j < 0) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public List<ItemStack> getOutputItems() {
		return entries.stream().map(PostAction::getOutputItems).flatMap(List::stream).toList();
	}

	@Override
	public List<BlockPredicate> getOutputBlocks() {
		return entries.stream().map(PostAction::getOutputBlocks).flatMap(List::stream).toList();
	}

	@Override
	public Component getDisplayName() {
		if (entries.size() == 1 && emptyWeight == 0) {
			return Component.literal("%s × %s".formatted(
					entries.get(0).getDisplayName().getString(),
					BoundsExtensions.getDescription(rolls).getString()
			));
		}
		return CommonProxy.getCycledItem(entries, entries.get(0), 1000).getDisplayName();
	}

	@Override
	public boolean repeatable() {
		return canRepeat;
	}

	@Override
	public boolean hidden() {
		return hidden;
	}

	@Override
	public boolean preventSync() {
		return preventSync;
	}

	@Override
	public void getUsedPointers(ILycheeRecipe<?> recipe, Consumer<JsonPointer> consumer) {
		for (var action : entries) {
			action.getUsedPointers(recipe, consumer);
		}
	}

	@Override
	public Stream<PostAction> getChildActions() {
		return entries.stream();
	}

	public static class Type implements PostActionType<RandomSelect> {
		public static final Codec<RandomSelect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(RandomSelect::commonProperties),
				ExtraCodecs.nonEmptyList(new CompactListCodec<>(PostAction.CODEC, true)).fieldOf("entries").forGetter(it -> it.entries),
				Codec.list(ExtraCodecs.intRange(0, Integer.MAX_VALUE)).fieldOf("weights").forGetter(it -> it.weights),
				ExtraCodecs.strictOptionalField(ExtraCodecs.intRange(0, Integer.MAX_VALUE), "empty_weight", 0)
						.forGetter(it -> it.emptyWeight),
				ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "rolls", BoundsExtensions.ONE).forGetter(it -> it.rolls)
		).apply(instance, RandomSelect::new));

		@Override
		public @NotNull Codec<RandomSelect> codec() {
			return CODEC;
		}
	}
}
