package snownee.lychee.util.action;

import net.minecraft.world.entity.Marker;

public interface ActionMarker {
	void lychee$setData(ActionData data);

	ActionData lychee$getData();

	default Marker lychee$self() {
		return (Marker) this;
	}
}
