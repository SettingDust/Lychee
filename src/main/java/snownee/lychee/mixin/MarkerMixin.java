package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.item.crafting.Recipe;
import snownee.lychee.core.ActionRuntime.State;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.Delay.LycheeMarker;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

@Mixin(Marker.class)
public class MarkerMixin implements LycheeMarker {

	private int lychee$ticks;
	private LycheeRecipe<?> lychee$recipe;
	private LycheeContext lychee$ctx;

	@Override
	public void lychee$setContext(LycheeRecipe<?> recipe, LycheeContext ctx) {
		lychee$ctx = ctx;
		lychee$recipe = recipe;
	}

	@Override
	public LycheeContext lychee$getContext() {
		return lychee$ctx;
	}

	@Override
	public void lychee$addDelay(int delay) {
		lychee$ticks += delay;
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void lychee_tick(CallbackInfo ci) {
		if (lychee$recipe == null || lychee$ctx == null) {
			return;
		}
		if (lychee$ticks-- > 0) {
			return;
		}
		lychee$ctx.runtime.state = State.RUNNING;
		lychee$ctx.runtime.run(lychee$recipe, lychee$ctx);
		if (lychee$ctx.runtime.state == State.STOPPED) {
			getEntity().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	private void lychee_readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (compoundTag.contains("lychee:ctx")) {
			lychee$ticks = compoundTag.getInt("lychee:ticks");
			lychee$ctx = LycheeContext.load(new Gson().fromJson(compoundTag.getString("lychee:ctx"), JsonObject.class), this);
			ResourceLocation recipeId = ResourceLocation.tryParse(compoundTag.getString("lychee:recipe"));
			Recipe<?> recipe = LUtil.recipe(recipeId);
			if (recipe instanceof LycheeRecipe) {
				lychee$recipe = (LycheeRecipe<?>) recipe;
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	private void lychee_addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (lychee$recipe == null || lychee$ctx == null) {
			return;
		}
		compoundTag.putInt("lychee:ticks", lychee$ticks);
		compoundTag.putString("lychee:ctx", lychee$ctx.save().toString());
		compoundTag.putString("lychee:recipe", lychee$recipe.getId().toString());
	}

}