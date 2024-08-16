package snownee.lychee.mixin.recipes.randomblockticking;

import org.apache.commons.lang3.stream.Streams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunkSection;
import snownee.kiwi.loader.Platform;
import snownee.lychee.mixin.ChunkMapAccess;

@Mixin(Blocks.class)
public class BlocksMixin {
	@Inject(method = "rebuildCache", at = @At("TAIL"))
	private static void rebuildCache(CallbackInfo ci) {
		var server = Platform.getServer();
		if (server == null) {
			return;
		}
		Streams.of(server.getAllLevels())
				.flatMap(it -> Streams.of(((ChunkMapAccess) it.getChunkSource().chunkMap).callGetChunks()))
				.filter(it -> it.getTickingChunk() != null)
				.flatMap(it -> Streams.of(it.getTickingChunk().getSections()))
				.forEach(LevelChunkSection::recalcBlockCounts);
	}
}
