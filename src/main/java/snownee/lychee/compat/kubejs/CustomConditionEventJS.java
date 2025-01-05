package snownee.lychee.compat.kubejs;

import java.util.Map;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.MapJS;
import snownee.lychee.contextual.CustomCondition;

public class CustomConditionEventJS implements KubeEvent {

	public final String id;
	public final CustomCondition condition;
	public final Map<?, ?> data;

	public CustomConditionEventJS(String id, CustomCondition condition) {
		this.id = id;
		this.condition = condition;
		this.data = MapJS.of(condition.data);
	}

}
