package snownee.lychee.compat.kubejs;

import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;

import snownee.kiwi.util.KUtil;

public class LycheeKubeJSUtils {
	public static JsonObject toJSON(Object obj) {
		if (obj instanceof String s) {
			Object o = KUtil.loadYaml(s, Object.class);
			return new Dynamic<>(JavaOps.INSTANCE, o).convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
		}
		throw new IllegalArgumentException("Unsupported type: " + obj);
	}
}
