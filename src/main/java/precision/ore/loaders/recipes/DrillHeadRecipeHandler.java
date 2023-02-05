package precision.ore.loaders.recipes;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.item.ItemStack;
import precision.ore.OreModule;
import precision.ore.common.tools.DrillHeadBehavior;

import static precision.ore.common.items.OreMetaItems.DRILL_HEAD;

public class DrillHeadRecipeHandler {

    public static void init(){
        for(Material material : OreModule.drillHeadMaterials) {
            ItemStack drillHead = DRILL_HEAD.getStackForm();
            DrillHeadBehavior materialInstance = DrillHeadBehavior.getInstanceFor(drillHead);
            if (materialInstance != null) {
                materialInstance.setPartMaterial(drillHead, material);

                RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                        .input(OrePrefix.plateDense, material, 5)
                        .input(OrePrefix.stickLong, material, 3)
                        .input(OrePrefix.ring, material, 3)
                        .outputs(drillHead)
                        .EUt(32).duration(320)
                        .buildAndRegister();
            }
        }
    }

}
