package net.mehvahdjukaar.every_compat.api;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.every_compat.WoodGood;
import net.mehvahdjukaar.every_compat.misc.Utils;
import net.mehvahdjukaar.every_compat.modules.CompatModule;
import net.mehvahdjukaar.selene.block_set.BlockType;
import net.mehvahdjukaar.selene.block_set.leaves.LeavesType;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.client.asset_generators.LangBuilder;
import net.mehvahdjukaar.selene.client.asset_generators.textures.Palette;
import net.mehvahdjukaar.selene.client.asset_generators.textures.Respriter;
import net.mehvahdjukaar.selene.client.asset_generators.textures.TextureImage;
import net.mehvahdjukaar.selene.items.WoodBasedBlockItem;
import net.mehvahdjukaar.selene.resourcepack.*;
import net.mehvahdjukaar.selene.resourcepack.resources.TagBuilder;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

//contrary to popular belief this class is indeed not simple. Its usage however is
public class SimpleEntrySet<T extends BlockType, B extends Block> extends EntrySet<T, B> {

    protected final Supplier<T> baseType;
    protected final Supplier<B> baseBlock;

    public final String name;
    @Nullable
    public final String prefix;

    protected final String formattedName;
    protected final Function<T, B> blockFactory;
    @Nullable
    protected final TileHolder<?> tileHolder;

    protected final CreativeModeTab tab;
    private final boolean copyLoot;
    private final Map<ResourceLocation, Set<ResourceKey<?>>> tags = new HashMap<>();
    private final Set<Supplier<ResourceLocation>> recipeLocations = new HashSet<>();
    private final Set<Pair<ResourceLocation, @Nullable ResourceLocation>> textures = new HashSet<>();
    @Nullable
    private final BiFunction<T, ResourceManager, Pair<List<Palette>, @Nullable AnimationMetadataSection>> paletteSupplier;
    @Nullable
    private final Supplier<Supplier<RenderType>> renderType;


    private SimpleEntrySet(String name, @Nullable String prefix, Function<T, B> blockSupplier,
                           Supplier<B> baseBlock, Supplier<T> baseType,
                           CreativeModeTab tab, boolean copyLoot,
                           @Nullable TileHolder<?> tileFactory,
                           @Nullable Supplier<Supplier<RenderType>> renderType,
                           @Nullable BiFunction<T, ResourceManager, Pair<List<Palette>, @Nullable AnimationMetadataSection>> paletteSupplier) {
        super((prefix == null ? "" : prefix + "_") + name);
        this.name = name;
        this.blockFactory = blockSupplier;
        this.prefix = prefix;
        this.tileHolder = tileFactory;
        this.tab = tab;
        this.copyLoot = copyLoot;
        this.formattedName = (prefix == null ? "" : prefix + "_") + "%s_" + name;
        this.baseBlock = baseBlock;
        this.baseType = baseType;

        this.renderType = renderType;
        this.paletteSupplier = paletteSupplier;
    }

    public Class<T> getType() {
        return (Class<T>) this.baseType.get().getClass();
    }

    public void addTranslations(AfterLanguageLoadEvent lang) {
        blocks.forEach((w, v) -> LangBuilder.addDynamicEntry(lang, "block.wood_good." + name, (BlockType) w, v));
    }

    public void registerWoodBlocks(CompatModule module, IForgeRegistry<Block> registry, Collection<WoodType> woodTypes) {
        if (WoodType.class == getType()) {
            registerBlocks(module, registry, (Collection<T>) woodTypes);
        }
    }

    public void registerLeavesBlocks(CompatModule module, IForgeRegistry<Block> registry, Collection<LeavesType> leavesTypes) {
        if (LeavesType.class == getType()) {
            registerBlocks(module, registry, (Collection<T>) leavesTypes);
        }
    }

    @Override
    public void registerBlocks(CompatModule module, IForgeRegistry<Block> registry, Collection<T> woodTypes) {
        baseType.get().addChild(module.shortenedId() + "/" + baseName, baseBlock.get());

        for (T w : woodTypes) {
            String name = module.makeBlockId(w, this.name);
            if (w.isVanilla() || module.isEntryAlreadyRegistered(name, registry)) continue;

            B block = blockFactory.apply(w);
            this.blocks.put(w, block);
            registry.register(block.setRegistryName(WoodGood.res(name)));
            w.addChild(module.shortenedId() + "/" + baseName, block);
        }
    }

    @Override
    public void registerItems(CompatModule module, IForgeRegistry<Item> registry) {
        blocks.forEach((w, value) -> {
            Item i;
            if (w.getClass() == WoodType.class) {
                i = new WoodBasedBlockItem(value, new Item.Properties().tab(tab), (WoodType) w);
            } else {
                int burn = baseBlock.get().asItem().getBurnTime(baseBlock.get().asItem().getDefaultInstance(), null);
                if (burn == 0) {
                    i = new BlockItem(value, new Item.Properties().tab(tab));
                } else {
                    i = new WoodBasedBlockItem(value, new Item.Properties().tab(tab), burn);
                }
            }
            this.items.put(w, i);
            registry.register(i.setRegistryName(value.getRegistryName()));
        });
    }

    @Override
    public void registerTiles(CompatModule module, IForgeRegistry<BlockEntityType<?>> registry) {
        if (tileHolder != null) {
            var tile = this.tileHolder.createInstance(blocks.values().toArray(Block[]::new));
            registry.register(tile.setRegistryName(module.modRes(this.getName())));
        }
    }

    @Override
    public void registerEntityRenderers(CompatModule simpleModule, EntityRenderersEvent.RegisterRenderers event) {
        if (this.tileHolder != null) {
            this.tileHolder.registerRenderer(event);
        }
    }

    @Override
    public void setRenderLayer() {
        if (renderType != null) {
            blocks.values().forEach(t -> ItemBlockRenderTypes.setRenderLayer(t, renderType.get().get()));
        }
    }

    @Override
    public void addTags(CompatModule module, DynamicDataPack pack, ResourceManager manager) {
        if (!tags.isEmpty()) {
            for (var tb : tags.entrySet()) {
                TagBuilder builder = TagBuilder.of(tb.getKey()).addEntries(blocks.values());
                for (var t : tb.getValue()) {
                    pack.addTag(builder, t);
                }
            }
        }
    }

    @Override
    public void addLootTables(CompatModule module, DynamicDataPack pack, ResourceManager manager) {
        if (copyLoot) {
            blocks.forEach((wood, value) -> pack.addSimpleBlockLootTable(value));
        } else {
            ResourceLocation reg = baseBlock.get().getRegistryName();
            Utils.addBlockResources(module.getModId(), manager, pack, blocks, reg.getPath(),
                    ResType.BLOCK_LOOT_TABLES.getPath(reg));
        }
    }

    @Override
    public void addRecipes(CompatModule module, DynamicDataPack pack, ResourceManager manager) {
        this.recipeLocations.forEach(r -> {
            Utils.addBlocksRecipes(manager, pack, blocks, r.get(), baseType.get());
        });

    }

    @Override
    public void addModels(CompatModule module, DynamicTexturePack pack, ResourceManager manager) {
        Utils.addStandardResources(module.getModId(), manager, pack, blocks);
    }

    @Override
    public void addTextures(CompatModule module, RPAwareDynamicTextureProvider handler, ResourceManager manager) {
        if (textures.isEmpty()) return;
        boolean isWood = this.getType() == WoodType.class;
        if (paletteSupplier == null && !isWood) {
            throw new UnsupportedOperationException("You need to provide a palette supplier for non wood type based blocks");
        }
        List<TextureImage> images = new ArrayList<>();
        try {
            Map<ResourceLocation, Respriter> respriters = new HashMap<>();
            for (var p : textures) {
                ResourceLocation textureId = p.getFirst();
                try {
                    TextureImage main = TextureImage.open(manager, textureId);
                    images.add(main);
                    ResourceLocation m = p.getSecond();
                    Respriter r;
                    if (m != null) {
                        TextureImage mask = TextureImage.open(manager, m);
                        images.add(main);
                        r = Respriter.masked(main, mask);
                    } else {
                        r = Respriter.of(main);
                    }
                    respriters.put(textureId, r);
                } catch (Exception e) {
                    WoodGood.LOGGER.error("Failed to read block texture at: {}", p, e);
                }
            }

            for (var entry : blocks.entrySet()) {
                B b = entry.getValue();
                T w = entry.getKey();
                ResourceLocation blockId = b.getRegistryName();

                List<Palette> targetPalette = null;
                AnimationMetadataSection animation = null;
                if (paletteSupplier != null) {
                    var pal = paletteSupplier.apply(w, manager);
                    animation = pal.getSecond();
                    targetPalette = pal.getFirst();
                } else {
                    try (TextureImage plankTexture = TextureImage.open(manager,
                            RPUtils.findFirstBlockTextureLocation(manager, ((WoodType) w).planks))) {
                        targetPalette = Palette.fromAnimatedImage(plankTexture);
                        animation = plankTexture.getMetadata();
                    } catch (Exception ignored) {
                    }
                }
                if (targetPalette == null) {
                    WoodGood.LOGGER.error("Could not get texture palette for block {} : ", b);
                    continue;
                }
                AnimationMetadataSection finalAnimation = animation;
                List<Palette> finalTargetPalette = targetPalette;

                for (var re : respriters.entrySet()) {
                    String oldPath = re.getKey().getPath();

                    String newId = BlockTypeResTransformer.replaceType(oldPath, blockId, w, baseType.get().getTypeName());

                    Respriter respriter = re.getValue();
                    if (isWood) {
                        module.addWoodTexture((WoodType) w, handler, manager, newId, () ->
                                respriter.recolorWithAnimation(finalTargetPalette, finalAnimation));

                    } else {
                        handler.addTextureIfNotPresent(manager, newId, () ->
                                respriter.recolorWithAnimation(finalTargetPalette, finalAnimation));

                    }
                }
            }

        } catch (Exception e) {
            WoodGood.LOGGER.error("Could not generate any block texture for entry set {} : ", module.modRes(this.getName()), e);
        } finally {
            for (var t : images) {
                t.close();
            }
        }

    }

    //ok...
    public static <T extends BlockType, B extends Block> Builder<T, B> builder(
            String name, Supplier<B> baseBlock, Supplier<T> baseType, Function<T, B> blockSupplier) {

        return new Builder<>(name, null, baseType, baseBlock, blockSupplier);
    }

    public static <T extends BlockType, B extends Block> Builder<T, B> builder(
            String name, String prefix, Supplier<B> baseBlock, Supplier<T> baseType, Function<T, B> blockSupplier) {

        return new Builder<T, B>(name, prefix, baseType, baseBlock, blockSupplier);
    }

    public static class Builder<T extends BlockType, B extends Block> {

        private final Supplier<T> baseType;
        private final Supplier<B> baseBlock;
        private final String name;
        @Nullable
        private final String prefix;
        private CreativeModeTab tab = CreativeModeTab.TAB_DECORATIONS;
        private final Function<T, B> blockFactory;
        @Nullable
        private TileHolder<?> tileFactory;
        @Nullable
        private Supplier<Supplier<RenderType>> renderType = null;
        @Nullable
        private BiFunction<T, ResourceManager, Pair<List<Palette>, @Nullable AnimationMetadataSection>> palette = null;
        private final Map<ResourceLocation, Set<ResourceKey<?>>> tags = new HashMap<>();
        private final Set<Supplier<ResourceLocation>> recipes = new HashSet<>();
        private final Set<Pair<ResourceLocation, @Nullable ResourceLocation>> textures = new HashSet<>();
        private boolean copyLoot;

        private Builder(String name, @Nullable String prefix, Supplier<T> baseType, Supplier<B> baseBlock, Function<T, B> blockFactory) {
            this.baseType = baseType;
            this.baseBlock = baseBlock;
            this.name = name;
            this.prefix = prefix;
            this.blockFactory = blockFactory;
        }

        public SimpleEntrySet<T, B> build() {
            var e = new SimpleEntrySet<>(name, prefix, blockFactory, baseBlock, baseType, tab, copyLoot, tileFactory, renderType, palette);
            e.recipeLocations.addAll(this.recipes);
            e.tags.putAll(this.tags);
            e.textures.addAll(textures);
            return e;
        }

        public <H extends BlockEntity> Builder<T, B> addTile(BlockEntityType.BlockEntitySupplier<H> tileFactory) {
            this.tileFactory = new TileHolder<>(tileFactory);
            return this;
        }

        public <H extends BlockEntity> Builder<T, B> addTile(BlockEntityType.BlockEntitySupplier<H> tileFactory,
                                                             Supplier<BlockEntityRendererProvider<H>> renderer) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                this.tileFactory = new TileHolder<>(tileFactory, renderer);
            } else {
                this.tileFactory = new TileHolder<>(tileFactory);
            }
            return this;
        }

        public Builder<T, B> setTab(CreativeModeTab tab) {
            this.tab = tab;
            return this;
        }

        public Builder<T, B> useLootFromBase() {
            this.copyLoot = true;
            return this;
        }

        public Builder<T, B> setRenderType(Supplier<Supplier<RenderType>> renderType) {
            //this.renderType = renderType;
            return this;
        }

        public Builder<T, B> addTag(ResourceLocation location, ResourceKey<?> registry) {
            var s = this.tags.computeIfAbsent(location, b -> new HashSet<>());
            s.add(registry);
            return this;
        }

        public Builder<T, B> addTag(TagKey<?> tag, ResourceKey<?> registry) {
            addTag(tag.location(), registry);
            return this;
        }

        public Builder<T, B> defaultRecipe() {
            this.recipes.add(() -> this.baseBlock.get().getRegistryName());
            return this;
        }

        public Builder<T, B> addRecipe(ResourceLocation resourceLocation) {
            this.recipes.add(() -> resourceLocation);
            return this;
        }

        public Builder<T, B> addTexture(ResourceLocation resourceLocation) {
            this.textures.add(Pair.of(resourceLocation, null));
            return this;
        }

        public Builder<T, B> addMaskedTexture(ResourceLocation textureLocation, ResourceLocation maskLocation) {
            this.textures.add(Pair.of(textureLocation, maskLocation));
            return this;
        }

        //by default, they all use planks palette
        public Builder<T, B> setPalette(BiFunction<T, ResourceManager, Pair<List<Palette>, @Nullable AnimationMetadataSection>> paletteProvider) {
            this.palette = paletteProvider;
            return this;
        }

        //only works for oak type. Will fail if its used on leaves
        public Builder<T, B> createPaletteFromOak(Consumer<Palette> paletteTransform) {
            return this.setPalette((w, m) -> {
                try (TextureImage plankTexture = TextureImage.open(m,
                        RPUtils.findFirstBlockTextureLocation(m, ((WoodType) w).planks))) {

                    List<Palette> targetPalette = Palette.fromAnimatedImage(plankTexture);
                    targetPalette.forEach(paletteTransform::accept);
                    return Pair.of(targetPalette, plankTexture.getMetadata());
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Failed to generate palette for %s : %s", w, e));
                }
            });
        }
    }


    private static class TileHolder<H extends BlockEntity> {

        protected final BlockEntityType.BlockEntitySupplier<H> tileFactory;
        protected Supplier<BlockEntityRendererProvider<H>> renderer = null;
        public BlockEntityType<? extends H> tile = null;


        @OnlyIn(Dist.CLIENT)
        public TileHolder(BlockEntityType.BlockEntitySupplier<H> tileFactory,
                          Supplier<BlockEntityRendererProvider<H>> renderer) {
            this.tileFactory = tileFactory;
            this.renderer = renderer;
        }

        public TileHolder(BlockEntityType.BlockEntitySupplier<H> tileFactory) {
            this.tileFactory = tileFactory;
        }

        public BlockEntityType<? extends H> get() {
            return tile;
        }

        public BlockEntityType<? extends H> createInstance(Block... blocks) {
            if (tile != null) throw new UnsupportedOperationException("tile has already been created");
            this.tile = BlockEntityType.Builder.of(tileFactory, blocks).build(null);
            return tile;
        }

        @OnlyIn(Dist.CLIENT)
        public void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
            if (this.renderer != null) {
                event.registerBlockEntityRenderer(tile, this.renderer.get());
            }
        }
    }

}