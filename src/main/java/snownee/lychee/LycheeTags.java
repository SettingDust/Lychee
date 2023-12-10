package snownee.lychee;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class LycheeTags {

	public static void init() {
	}

	public static final TagKey<Item> FIRE_IMMUNE = itemTag("fire_immune");

	public static final TagKey<Item> DISPENSER_PLACEMENT = itemTag("dispenser_placement");

	public static final TagKey<Item> ITEM_EXPLODING_CATALYSTS = itemTag("item_exploding_catalysts");

	public static final TagKey<Item> BLOCK_EXPLODING_CATALYSTS = itemTag("block_exploding_catalysts");

	public static final TagKey<Block> EXTEND_BOX = blockTag("extend_box");

	public static final TagKey<EntityType<?>> LIGHTNING_IMMUNE = entityTag("lightning_immune");

	public static final TagKey<EntityType<?>> LIGHTING_FIRE_IMMUNE = entityTag("lightning_fire_immune");

	public static TagKey<EntityType<?>> entityTag(String path) {
		return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(Lychee.ID, path));
	}

	public static TagKey<Item> itemTag(String path) {
		return ItemTags.create(new ResourceLocation(Lychee.ID, path));
	}

	public static TagKey<Block> blockTag(String path) {
		return BlockTags.create(new ResourceLocation(Lychee.ID, path));
	}

}
