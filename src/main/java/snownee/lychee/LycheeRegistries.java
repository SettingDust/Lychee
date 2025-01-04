package snownee.lychee;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.context.LycheeContextSerializer;
import snownee.lychee.util.contextual.ContextualConditionType;

public final class LycheeRegistries {
	public static final MappedRegistry<ContextualConditionType<?>> CONTEXTUAL = register("contextual");
	public static final MappedRegistry<PostActionType<?>> POST_ACTION = register("post_action");
	public static final MappedRegistry<LycheeContextKey<?>> CONTEXT = register("context");
	public static final MappedRegistry<LycheeContextSerializer<?>> CONTEXT_SERIALIZER = register("context_serializer");

	public static void init(NewRegistryEvent event) {
		event.register(CONTEXTUAL);
		event.register(POST_ACTION);
		event.register(CONTEXT);
		event.register(CONTEXT_SERIALIZER);
	}

	private static <T> MappedRegistry<T> register(String id) {
		return (MappedRegistry<T>) new RegistryBuilder<>(ResourceKey.<T>createRegistryKey(Lychee.id(id))).sync(true).create();
	}
}
