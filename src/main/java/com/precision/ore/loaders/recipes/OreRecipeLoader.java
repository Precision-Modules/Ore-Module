package com.precision.ore.loaders.recipes;

import com.precision.ore.common.items.OreMetaItems;
import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import gregtech.loaders.recipe.handlers.ToolRecipeHandler;
import net.minecraft.init.Items;
import com.precision.ore.OreConfig;
import com.precision.ore.OreModule;

public class OreRecipeLoader {

    public static void init(){
        OreModule.logger.info("Ore Module Registering Recipes...");
        if(OreConfig.disableGregTechScanners || OreConfig.disableGregTechOreGeneration) {
            ModHandler.removeRecipes(rec -> rec.getRegistryName() != null && rec.getRegistryName().getNamespace().equals(GTValues.MODID) && rec.getRegistryName().getPath().contains("prospector_"));
        }

        ModHandler.addMirroredShapedRecipe("primitive_drill", OreMetaItems.PRIMITIVE_DRILL.getStackForm(),
                "  S", "FS ", "FF ",
                'S', Items.STICK,
                'F', Items.FLINT);

        for (MetaItem<?>.MetaValueItem batteryItem : ToolRecipeHandler.batteryItems.get(GTValues.LV)) {
            ModHandler.addShapedEnergyTransferRecipe("prospector.lv_" + batteryItem.unlocalizedName, OreMetaItems.PROSPECTOR_LV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "SPE", "CDC", "PBP",
                    'E', MetaItems.EMITTER_LV.getStackForm(),
                    'P', new UnificationEntry(OrePrefix.plate, Materials.Steel),
                    'S', MetaItems.SENSOR_LV.getStackForm(),
                    'D', new UnificationEntry(OrePrefix.plate, Materials.Glass),
                    'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV),
                    'B', batteryItem.getStackForm());
        }

        for (MetaItem<?>.MetaValueItem batteryItem : ToolRecipeHandler.batteryItems.get(GTValues.HV)) {
            ModHandler.addShapedEnergyTransferRecipe("prospector.hv_" + batteryItem.unlocalizedName, OreMetaItems.PROSPECTOR_HV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "SPE", "CDC", "PBP",
                    'E', MetaItems.EMITTER_HV.getStackForm(),
                    'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel),
                    'S', MetaItems.SENSOR_HV.getStackForm(),
                    'D', MetaItems.COVER_SCREEN.getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV),
                    'B', batteryItem.getStackForm());

        }

        for (MetaItem<?>.MetaValueItem batteryItem : ToolRecipeHandler.batteryItems.get(GTValues.LuV)) {
            ModHandler.addShapedEnergyTransferRecipe("prospector.luv_" + batteryItem.unlocalizedName, OreMetaItems.PROSPECTOR_LUV.getStackForm(),
                    batteryItem::isItemEqual, true, true,
                    "SPE", "CDC", "PBP", 'E', MetaItems.EMITTER_LuV.getStackForm(),
                    'P', new UnificationEntry(OrePrefix.plate, Materials.RhodiumPlatedPalladium),
                    'S', MetaItems.SENSOR_LuV.getStackForm(),
                    'D', MetaItems.COVER_SCREEN.getStackForm(),
                    'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LuV),
                    'B', batteryItem.getStackForm());
        }

        DrillHeadRecipeHandler.init();
    }
}
