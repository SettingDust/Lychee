package snownee.lychee.util.action;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.contextual.ContextualHolder;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PostActionCommonProperties {
	public static final MapCodec<PostActionCommonProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.strictOptionalField(Codec.STRING, "@path").forGetter(PostActionCommonProperties::getPath),
			ExtraCodecs.strictOptionalField(ContextualHolder.CODEC, "contextual", ContextualHolder.EMPTY)
					.forGetter(PostActionCommonProperties::conditions),
			Codec.BOOL.optionalFieldOf("hide", false).forGetter(PostActionCommonProperties::hidden)
	).apply(instance, PostActionCommonProperties::new));
	private Optional<String> path;
	private final ContextualHolder conditions;
	private final boolean hidden;

	public PostActionCommonProperties(Optional<String> path, ContextualHolder conditions, boolean hidden) {
		this.path = path;
		this.conditions = conditions;
		this.hidden = hidden;
	}

	public PostActionCommonProperties() {
		this.conditions = new ContextualHolder(List.of(), null, null);
		this.path = Optional.empty();
		this.hidden = false;
	}

	public ContextualHolder conditions() {
		return conditions;
	}

	public Optional<String> getPath() {
		return path;
	}

	public void setPath(final @Nullable String path) {
		this.path = Optional.ofNullable(path);
	}

	public boolean hidden() {
		return hidden;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("path", path)
				.add("hidden", hidden)
				.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PostActionCommonProperties that = (PostActionCommonProperties) o;
		return Objects.equal(path, that.path);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(path);
	}
}
