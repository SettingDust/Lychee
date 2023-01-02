package snownee.lychee.core.contextual;

import com.google.gson.JsonObject;
import com.mojang.brigadier.ParseResults;

import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.Lychee;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;

public record Execute(String command, Ints bounds) implements ContextualCondition {

	public static final Ints DEFAULT_RANGE = Ints.atLeast(1);
	public static final Execute DUMMY = new Execute("", DEFAULT_RANGE);

	@Override
	public ContextualConditionType<? extends ContextualCondition> getType() {
		return ContextualConditionTypes.EXECUTE;
	}

	@Override
	public int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (command.isEmpty() || ctx.getLevel().isClientSide) {
			return 0;
		}
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		Entity entity = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
		Vec2 rotation = Vec2.ZERO;
		Component displayName = snownee.lychee.core.post.Execute.DEFAULT_NAME;
		String name = Lychee.ID;
		if (entity != null) {
			rotation = entity.getRotationVector();
			displayName = entity.getDisplayName();
			name = entity.getName().getString();
		}
		CommandSourceStack sourceStack = new CommandSourceStack(CommandSource.NULL, pos, rotation, ctx.getServerLevel(), 2, name, displayName, ctx.getLevel().getServer(), entity);
		Commands cmds = ctx.getLevel().getServer().getCommands();
		ParseResults<CommandSourceStack> results = cmds.getDispatcher().parse(command, sourceStack);
		int i = cmds.performCommand(results, command);
		return bounds.matches(i) ? times : 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(makeDescriptionId(false));
	}

	public static class Type extends ContextualConditionType<Execute> {

		@Override
		public Execute fromJson(JsonObject o) {
			Ints bounds = DEFAULT_RANGE;
			if (o.has("value")) {
				bounds = Ints.fromJson(o.get("value"));
			}
			return new Execute(GsonHelper.getAsString(o, "command"), bounds);
		}

		@Override
		public void toJson(Execute condition, JsonObject o) {
			o.addProperty("command", condition.command());
			if (condition.bounds() != DEFAULT_RANGE) {
				o.add("value", condition.bounds().serializeToJson());
			}
		}

		@Override
		public Execute fromNetwork(FriendlyByteBuf buf) {
			return DUMMY;
		}

		@Override
		public void toNetwork(Execute condition, FriendlyByteBuf buf) {
		}

	}

}
