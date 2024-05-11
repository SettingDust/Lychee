package snownee.lychee;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.util.KUtil;

public final class Lychee {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static ResourceLocation id(String path) {
		return KUtil.RL(path, ID);
	}
}
