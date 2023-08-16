package snownee.lychee.core.post;

import com.google.gson.JsonObject;
import com.mojang.brigadier.ParseResults;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.Lychee;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class Execute extends PostAction {

	public static final Execute DUMMY = new Execute("", false);
	public static final Component DEFAULT_NAME = Component.literal(Lychee.ID);

	private final String command;
	private final boolean hide;

	public Execute(String command, boolean hide) {
		this.command = command;
		this.hide = hide;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.EXECUTE;
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (command.isEmpty()) {
			return;
		}
		Vec3 pos = ctx.getParamOrNull(LootContextParams.ORIGIN);
		if (pos == null) {
			pos = Vec3.ZERO;
		}
		Entity entity = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
		Vec2 rotation = Vec2.ZERO;
		Component displayName = DEFAULT_NAME;
		String name = Lychee.ID;
		if (entity != null) {
			rotation = entity.getRotationVector();
			displayName = entity.getDisplayName();
			name = entity.getName().getString();
		}
		CommandSourceStack sourceStack = new CommandSourceStack(CommandSource.NULL, pos, rotation, ctx.getServerLevel(), 2, name, displayName, ctx.getServerLevel().getServer(), entity);
		for (int i = 0; i < times; i++) {
			Commands cmds = ctx.getServerLevel().getServer().getCommands();
			ParseResults<CommandSourceStack> results = cmds.getDispatcher().parse(command, sourceStack);
			cmds.performCommand(results, command);
		}
	}

	@Override
	public boolean preventSync() {
		return hide;
	}

	public static class Type extends PostActionType<Execute> {

		@Override
		public Execute fromJson(JsonObject o) {
			return new Execute(GsonHelper.getAsString(o, "command"), GsonHelper.getAsBoolean(o, "hide", false));
		}

		@Override
		public void toJson(Execute action, JsonObject o) {
			o.addProperty("command", action.command);
			if (action.hide) {
				o.addProperty("hide", true);
			}
		}

		@Override
		public Execute fromNetwork(FriendlyByteBuf buf) {
			if (buf.readBoolean()) {
				return new Execute("", false);
			}
			return DUMMY;
		}

		@Override
		public void toNetwork(Execute action, FriendlyByteBuf buf) {
			buf.writeBoolean(action.getConditions().isEmpty());
		}

	}

}
