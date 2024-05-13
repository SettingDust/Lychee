package snownee.lychee.util.action;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.codec.LycheeCodecs;

public interface PostActionType<T extends PostAction> extends SerializableType<T> {
	Codec<PostAction> CODEC = LycheeRegistries.POST_ACTION.byNameCodec().dispatch(PostAction::type, PostActionType::codec);

	StreamCodec<RegistryFriendlyByteBuf, PostAction> STREAM_CODEC = ByteBufCodecs.registry(LycheeRegistries.POST_ACTION.key()).dispatch(
			PostAction::type,
			PostActionType::streamCodec);

	StreamCodec<RegistryFriendlyByteBuf, List<PostAction>> STREAM_LIST_CODEC = STREAM_CODEC.apply(original ->
			new StreamCodec<>() {
				@Override
				public void encode(RegistryFriendlyByteBuf byteBuf, List<PostAction> list) {
					var filtered = list.stream().filter(it -> !it.preventSync()).toList();
					ByteBufCodecs.writeCount(byteBuf, filtered.size(), Integer.MAX_VALUE);
					for (PostAction action : filtered) {
						original.encode(byteBuf, action);
					}
				}

				@Override
				public List<PostAction> decode(RegistryFriendlyByteBuf byteBuf) {
					var size = ByteBufCodecs.readCount(byteBuf, Integer.MAX_VALUE);
					var list = new ArrayList<PostAction>(size);
					for (int i = 0; i < size; i++) {
						var action = original.decode(byteBuf);
						if (!action.preventSync()) {
							list.add(action);
						}
					}
					return list;
				}
			});

	Codec<List<PostAction>> LIST_CODEC = LycheeCodecs.compactList(CODEC);

	@Override
	StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
}
