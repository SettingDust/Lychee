package snownee.lychee.mixin.recipes.randomblockticking;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.util.RandomlyTickable;
import snownee.lychee.util.predicates.BlockStateSet;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin implements RandomlyTickable {

	@Unique
	private Predicate<BlockState> lychee$randomlyTickable = BlockStateSet.NONE;

	@Override
	public void lychee$setTickable(Predicate<BlockState> predicate) {
		lychee$randomlyTickable = predicate;
	}

	@Override
	public boolean lychee$isTickable(BlockState blockState) {
		return lychee$randomlyTickable.test(blockState);
	}
}
