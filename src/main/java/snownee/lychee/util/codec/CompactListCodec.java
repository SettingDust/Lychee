package snownee.lychee.util.codec;

import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.ListCodec;

import net.minecraft.util.ExtraCodecs;

//TODO move to Kiwi
public final class CompactListCodec<E> implements Codec<List<E>> {
	private final Codec<E> singleCodec;
	private final Codec<List<E>> listCodec;
	private final boolean isNestedList;

	public CompactListCodec(Codec<E> singleCodec) {
		this.singleCodec = singleCodec;
		this.isNestedList = singleCodec instanceof ListCodec;
		this.listCodec = singleCodec.listOf();
	}

	public Codec<List<E>> nonEmpty() {
		return ExtraCodecs.nonEmptyList(this);
	}

	@Override
	public <T> DataResult<Pair<List<E>, T>> decode(DynamicOps<T> ops, T input) {
		var list = ops.getList(input);
		if (isNestedList && list.result().isPresent()) {
			var listResult = listCodec.decode(ops, input);
			if (listResult.result().isPresent()) {
				return listResult;
			}
			return singleCodec.decode(ops, input).map($ -> $.mapFirst(List::of));
		}
		if (list.result().isPresent()) {
			return listCodec.decode(ops, input);
		}
		return singleCodec.decode(ops, input).map($ -> $.mapFirst(List::of));
	}

	@Override
	public <T> DataResult<T> encode(List<E> input, DynamicOps<T> ops, T prefix) {
		if (input.size() == 1) {
			return singleCodec.encode(input.get(0), ops, prefix);
		}
		return listCodec.encode(input, ops, prefix);
	}

	@Override
	public String toString() {
		return "CompactListCodec[" + singleCodec + ", isNestedList=" + isNestedList + "]";
	}
}
