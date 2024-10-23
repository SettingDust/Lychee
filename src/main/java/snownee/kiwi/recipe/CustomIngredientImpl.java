package snownee.kiwi.recipe;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public record CustomIngredientImpl(CustomIngredient ingredient) implements ICustomIngredient {
	static final Map<ResourceLocation, CustomIngredientSerializer<?>> REGISTERED_SERIALIZERS = new ConcurrentHashMap<>();
	private static final Map<CustomIngredientSerializer<?>, IngredientType<?>> INGREDIENT_TYPES = Maps.newIdentityHashMap();

	public static void registerSerializer(CustomIngredientSerializer<?> serializer) {
		Objects.requireNonNull(serializer.getIdentifier(), "CustomIngredientSerializer identifier may not be null.");

		if (REGISTERED_SERIALIZERS.putIfAbsent(serializer.getIdentifier(), serializer) != null) {
			throw new IllegalArgumentException(
					"CustomIngredientSerializer with identifier " + serializer.getIdentifier() + " already registered.");
		}
	}

	@Nullable
	public static CustomIngredientSerializer<?> getSerializer(ResourceLocation identifier) {
		Objects.requireNonNull(identifier, "Identifier may not be null.");

		return REGISTERED_SERIALIZERS.get(identifier);
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return ingredient.test(itemStack);
	}

	@Override
	public Stream<ItemStack> getItems() {
		return this.ingredient.getMatchingStacks().stream();
	}

	@Override
	public boolean isSimple() {
		return !ingredient.requiresTesting();
	}

	@Override
	public IngredientType<?> getType() {
		return INGREDIENT_TYPES.computeIfAbsent(ingredient.getSerializer(), $ -> {
			//noinspection rawtypes,unchecked
			return new IngredientType($.getCodec(true), $.getPacketCodec());
		});
	}
}