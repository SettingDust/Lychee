package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.ReloadableServerResources;
import snownee.lychee.RecipeTypes;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

	// because tags are loaded after recipes, we bake cache here
	@Inject(
			method = "updateRegistryTags()V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Blocks;rebuildCache()V"))
	private void lychee_updateRegistryTags(CallbackInfo ci) {
		RecipeTypes.buildCache();
	}

}
