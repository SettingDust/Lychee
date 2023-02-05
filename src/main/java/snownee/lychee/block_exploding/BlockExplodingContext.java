package snownee.lychee.block_exploding;

import java.util.Map;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.core.LycheeContext;

public class BlockExplodingContext extends LycheeContext {

	protected BlockExplodingContext(RandomSource pRandom, Level level, Map<LootContextParam<?>, Object> pParams) {
		super(pRandom, level, pParams);
	}

	public static class Builder extends LycheeContext.Builder<BlockExplodingContext> {
		public Builder(Level level) {
			super(level);
		}

		@Override
		public BlockExplodingContext create(LootContextParamSet pParameterSet) {
			beforeCreate(pParameterSet);
			return new BlockExplodingContext(random, level, params);
		}
	}

}
