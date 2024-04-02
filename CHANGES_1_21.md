# Changes in 1.20.5 & 1.21

- Kiwi is now a required dependency for Lychee.
- The `lychee:tag` key to assign NBT to an ItemStack has been replaced by the vanilla feature `components`.
- The `lychee:tag` key to assign visual-only NBT to an Ingredient has been replaced by an independent ingredient
  type `lychee:visual_only_components`. And it can be used in more ingredient types, not only a simple item.
- The translation key for biome tags is now `biome.#<namespace>.<path.replace('/', '.')>` instead
  of `biomeTag.<namespace>.<path.replace('/', '.')>`.
- The `lychee:biome_tag` in LocationPredicate is no longer supported. Instead, you should use the vanilla's new feature instead.
- JSON fragments now work with any recipe types, and you can now replace the recipe type or the conditions provided by
  loaders. The fragment id is now **namespace-sensitive**.
