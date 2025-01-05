package snownee.lychee.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;

public interface LycheeKubeJSEvents {

	EventGroup GROUP = EventGroup.of("LycheeEvents");

	TargetedEventHandler<String> CLICKED_INFO_BADGE = GROUP.client("clickedInfoBadge", () -> ClickedInfoBadgeEventJS.class)
			.supportsTarget(EventTargetType.STRING)
			.hasResult();
	TargetedEventHandler<String> CUSTOM_ACTION = GROUP.startup("customAction", () -> CustomActionEventJS.class)
			.requiredTarget(EventTargetType.STRING)
			.hasResult();
	TargetedEventHandler<String> CUSTOM_CONDITION = GROUP.startup("customCondition", () -> CustomConditionEventJS.class)
			.requiredTarget(EventTargetType.STRING)
			.hasResult();
}
