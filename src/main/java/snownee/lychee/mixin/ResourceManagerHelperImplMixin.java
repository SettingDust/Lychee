package snownee.lychee.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import snownee.lychee.fragment.Fragments;

@Mixin(value = ResourceManagerHelperImpl.class, remap = false)
public abstract class ResourceManagerHelperImplMixin {

	@Shadow
	private static ResourceManagerHelperImpl get(PackType type) {
		throw new AssertionError();
	}

	@Inject(at = @At(value = "TAIL"), method = "sort(Ljava/util/List;)V")
	private void lychee_apply(List<PreparableReloadListener> listeners, CallbackInfo ci) {
		if (get(PackType.SERVER_DATA) == (Object) this) {
			listeners.add(0, Fragments.INSTANCE);
		}
	}

}
