package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

@Mixin(LootContextParamSets.class)
public class LootContextParamSetsMixin {
	@WrapOperation(
			method = "register",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
	private static ResourceLocation lychee$allowOtherNamespace(final String string, final Operation<ResourceLocation> original) {
		if (string.indexOf(ResourceLocation.NAMESPACE_SEPARATOR) == -1) {
			// Avoid hiding the other mixins
			return original.call(string);
		}
		return ResourceLocation.parse(string);
	}
}
