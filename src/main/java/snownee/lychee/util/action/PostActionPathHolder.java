package snownee.lychee.util.action;

import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class PostActionPathHolder {
	private Optional<String> path;

	public Optional<String> path() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path.describeConstable();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final PostActionPathHolder that = (PostActionPathHolder) o;
		return Objects.equal(path, that.path);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(path);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
						  .add("path", path)
						  .toString();
	}
}
