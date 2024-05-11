package snownee.lychee.util;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface SerializableType<T> {
	@NotNull
	MapCodec<T> codec();

	default StreamCodec<? extends ByteBuf, T> streamCodec() {
		return ByteBufCodecs.fromCodecWithRegistries(codec().codec());
	}
}