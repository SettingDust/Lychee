package snownee.lychee;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;

public final class Lychee {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static ResourceLocation resourceLocation(String path) {
		return path.contains(":") ? new ResourceLocation(path) : new ResourceLocation(ID, path);
	}
}
