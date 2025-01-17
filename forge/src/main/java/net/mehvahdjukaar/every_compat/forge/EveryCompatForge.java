package net.mehvahdjukaar.every_compat.forge;

import net.mehvahdjukaar.every_compat.EveryCompat;
import net.mehvahdjukaar.every_compat.EveryCompatClient;
import net.mehvahdjukaar.every_compat.modules.another_furniture.AnotherFurnitureModule;
import net.mehvahdjukaar.every_compat.modules.backpacked.BackpackedModule;
import net.mehvahdjukaar.every_compat.modules.buildersaddition.BuildersAdditionModule;
import net.mehvahdjukaar.every_compat.modules.camp_chair.CampChairModule;
import net.mehvahdjukaar.every_compat.modules.create.CreateModule;
import net.mehvahdjukaar.every_compat.modules.decorative_blocks.DecorativeBlocksModule;
import net.mehvahdjukaar.every_compat.modules.dramaticdoors.DramaticDoorsMacawModule;
import net.mehvahdjukaar.every_compat.modules.dramaticdoors.DramaticDoorsModule;
import net.mehvahdjukaar.every_compat.modules.exline.BarkCarpetsModule;
import net.mehvahdjukaar.every_compat.modules.farmersdelight.FarmersDelightModule;
import net.mehvahdjukaar.every_compat.modules.friendsandfoes.FriendsAndFoesModule;
import net.mehvahdjukaar.every_compat.modules.furnish.FurnishModule;
import net.mehvahdjukaar.every_compat.modules.handcrafted.HandcraftedModule;
import net.mehvahdjukaar.every_compat.modules.infinitybuttons.InfinityButtonsModule;
import net.mehvahdjukaar.every_compat.modules.mcaw.*;
import net.mehvahdjukaar.every_compat.modules.missing_wilds.MissingWildModule;
import net.mehvahdjukaar.every_compat.modules.mosaic_carpentry.MosaicCarpentryModule;
import net.mehvahdjukaar.every_compat.modules.mrcrayfish_furniture.MrCrayfishFurnitureModule;
import net.mehvahdjukaar.every_compat.modules.pokecube.PokecubeLegendsModule;
import net.mehvahdjukaar.every_compat.modules.productive_bees.ProductiveBeesModule;
import net.mehvahdjukaar.every_compat.modules.quark.QuarkModule;
import net.mehvahdjukaar.every_compat.modules.abnormal.WoodworksModule;
import net.mehvahdjukaar.every_compat.modules.storagedrawers.StorageDrawersModule;
import net.mehvahdjukaar.every_compat.modules.twigs.TwigsModule;
import net.mehvahdjukaar.every_compat.modules.twilightforest.TwilightForestModule;
import net.mehvahdjukaar.every_compat.modules.xerca.XercaModule;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Author: MehVahdJukaar
 */
@Mod(EveryCompat.MOD_ID)
public class EveryCompatForge extends EveryCompat {
    public static final String MOD_ID = EveryCompat.MOD_ID;

    public EveryCompatForge() {
        this.commonInit();

        CraftingHelper.register(new BlockTypeEnabledCondition.Serializer());

        addModule("mcwbridges", () -> MacawBridgesModule::new);
        addModule("mcwbridges", () -> MacawBridgesModule::new);
        addModule("mcwdoors", () -> MacawDoorsModule::new);
        addModule("mcwfences", () -> MacawFencesModule::new);
        addModule("mcwfences", () -> MacawFencesModule::new);
        addModule("mcwfurnitures", () -> MacawFurnitureModule::new);
        addModule("mcwlights", () -> MacawLightsModule::new);
        addModule("mcwpaths", () -> MacawPathsModule::new);
        addModule("mcwroofs", () -> MacawRoofsModule::new);
        addModule("mcwtrpdoors", () -> MacawTrapdoorsModule::new);
        addModule("mcwwindows", () -> MacawWindowsModule::new);

        addModule("another_furniture", () -> AnotherFurnitureModule::new);
        addModule("backpacked", () -> BackpackedModule::new);
        addModule("barkcarpets", () -> BarkCarpetsModule::new);
        addModule("buildersaddition", () -> BuildersAdditionModule::new);
        addModule("campchair", () -> CampChairModule::new);
        addModule("cfm", () -> MrCrayfishFurnitureModule::new);
        addModule("create", () -> CreateModule::new);
        addModule("decorative_blocks", () -> DecorativeBlocksModule::new);
        addModule("dramaticdoors", () -> DramaticDoorsModule::new);
        addModule("dramaticdoors", () -> DramaticDoorsMacawModule::new);
        addModule("farmersdelight", () -> FarmersDelightModule::new);
        addModule("furnish", () -> FurnishModule::new);
        addModule("missingwilds", () -> MissingWildModule::new);
        addModule("mosaic_carpentry", () -> MosaicCarpentryModule::new);
        addModule("pokecube_legends", () -> PokecubeLegendsModule::new);
        addModule("productivebees", () -> ProductiveBeesModule::new);
        addModule("quark", () -> QuarkModule::new);
        addModule("storagedrawers", () -> StorageDrawersModule::new);
        addModule("twilightforest", () -> TwilightForestModule::new);
        addModule("woodworks", () -> WoodworksModule::new);
        addModule("friendsandfoes", () -> FriendsAndFoesModule::new);
        addModule("twigs", () -> TwigsModule::new);
        addModule("infinitybuttons", () -> InfinityButtonsModule::new);
        addModule("valhelsia_structures", () -> ValhelsiaStructuresModule::new);

        // addModule("graveyard", () -> GraveyardModule::new);

        // Disabled due to block entity complications
        // addModule("handcrafted", () -> HandcraftedModule::new);
        
        // Disabled until custom block models work
        // addModule("xercamod", () -> XercaModule::new);

        if(PlatformHelper.getEnv().isClient()){
            EveryCompatClient.commonInit();
        }

        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event){
        this.commonSetup();
    }
}
