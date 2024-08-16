package snownee.lychee.util;

import java.util.function.Predicate;

import net.minecraft.world.level.block.state.BlockState;

public interface RandomlyTickable {

	void lychee$setTickable(Predicate<BlockState> predicate);

	boolean lychee$isTickable(BlockState blockState);

}
