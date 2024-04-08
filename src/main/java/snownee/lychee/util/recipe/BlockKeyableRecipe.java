package snownee.lychee.util.recipe;


import net.minecraft.advancements.critereon.BlockPredicate;
import snownee.lychee.util.context.LycheeContext;

public interface BlockKeyableRecipe<T extends BlockKeyableRecipe<T>> extends ILycheeRecipe<LycheeContext> {
	BlockPredicate blockPredicate();
}
