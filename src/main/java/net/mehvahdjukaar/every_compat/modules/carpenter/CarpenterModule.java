package net.mehvahdjukaar.every_compat.modules.carpenter;

import com.mojang.datafixers.util.Pair;
import gg.moonflower.carpenter.common.block.CarpenterBookshelfBlock;
import gg.moonflower.carpenter.common.item.TabInsertBlockItem;
import gg.moonflower.carpenter.core.registry.CarpenterBlocks;
import net.mehvahdjukaar.every_compat.WoodGood;
import net.mehvahdjukaar.every_compat.api.SimpleEntrySet;
import net.mehvahdjukaar.every_compat.api.SimpleModule;
import net.mehvahdjukaar.selene.block_set.BlockType;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.selene.client.asset_generators.textures.Palette;
import net.mehvahdjukaar.selene.client.asset_generators.textures.TextureImage;
import net.mehvahdjukaar.selene.resourcepack.RPUtils;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.Tags;

import java.util.List;

public class CarpenterModule extends SimpleModule {

    public final SimpleEntrySet<WoodType, Block> BOOKSHELVES;

    public CarpenterModule(String modId) {
        super(modId, "ca");

        BOOKSHELVES = SimpleEntrySet.builder(WoodType.class, "bookshelf",
                        () -> this.getModBlock("acacia_bookshelf"), () -> WoodTypeRegistry.WOOD_TYPES.get(new ResourceLocation("acacia")),
                        w -> new CompatBookshelfBlock())
                .addCustomItem((woodType, block, properties) -> new TabInsertBlockItem(block, Items.BOOKSHELF, properties))
                .setTab(() -> CreativeModeTab.TAB_BUILDING_BLOCKS)
                .useLootFromBase()
                .addTag(BlockTags.MINEABLE_WITH_AXE, Registry.BLOCK_REGISTRY)
                .addTag(Tags.Items.BOOKSHELVES, Registry.BLOCK_REGISTRY)
                .addTag(Tags.Items.BOOKSHELVES, Registry.ITEM_REGISTRY)
                .defaultRecipe()
                .addTextureM(WoodGood.res("block/acacia_bookshelf"), WoodGood.res("block/acacia_bookshelf_m"))
                .setPalette(this::bookshelfPalette)
                .build();

        this.addEntry(BOOKSHELVES);
    }

    // Copied from quark module since carpenter also adds bookshelves
    private Pair<List<Palette>, AnimationMetadataSection> bookshelfPalette(BlockType w, ResourceManager m) {
        try (TextureImage plankTexture = TextureImage.open(m,
                RPUtils.findFirstBlockTextureLocation(m, ((WoodType) w).planks))) {

            List<Palette> targetPalette = Palette.fromAnimatedImage(plankTexture);
            targetPalette.forEach(p -> {
                var l0 = p.getDarkest();
                p.increaseDown();
                p.increaseDown();
                p.increaseDown();
                p.increaseDown();
                p.remove(l0);
            });
            return Pair.of(targetPalette, plankTexture.getMetadata());
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to generate palette for %s : %s", w, e));
        }
    }

    private static class CompatBookshelfBlock extends CarpenterBookshelfBlock {

        public CompatBookshelfBlock() {
            super(BlockBehaviour.Properties.copy(CarpenterBlocks.SPRUCE_BOOKSHELF.get()));
        }
    }
}
