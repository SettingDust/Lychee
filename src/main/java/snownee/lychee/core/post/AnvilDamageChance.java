package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.PostActionTypes;
import snownee.lychee.block_crushing.LycheeFallingBlockEntity;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class AnvilDamageChance extends PostAction {

	public final float chance;

	public AnvilDamageChance(float chance) {
		this.chance = chance;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.ANVIL_DAMAGE_CHANCE;
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Entity entity = ctx.getParam(LootContextParams.THIS_ENTITY);
		if (entity instanceof LycheeFallingBlockEntity) {
			((LycheeFallingBlockEntity) entity).lychee$anvilDamageChance(chance);
		}
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	public static class Type extends PostActionType<AnvilDamageChance> {

		@Override
		public AnvilDamageChance fromJson(JsonObject o) {
			return new AnvilDamageChance(o.get("chance").getAsFloat());
		}

		@Override
		public void toJson(AnvilDamageChance action, JsonObject o) {
			o.addProperty("chance", action.chance);
		}

		@Override
		public AnvilDamageChance fromNetwork(FriendlyByteBuf buf) {
			return new AnvilDamageChance(buf.readFloat());
		}

		@Override
		public void toNetwork(AnvilDamageChance action, FriendlyByteBuf buf) {
			buf.writeFloat(action.chance);
		}

	}

}