package com.precision.ore.loaders.recipes;

import com.precision.ore.OreModule;
import com.precision.ore.common.items.OreMetaItems;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.behaviors.AbstractMaterialPartBehavior;
import net.minecraft.item.ItemStack;

public class DrillHeadRecipeHandler {

    public static void init(){
        for(Material material : OreModule.drillHeadMaterials) {
            ItemStack drillHead = OreMetaItems.DRILL_HEAD.getStackForm();
            AbstractMaterialPartBehavior.setPartMaterial(drillHead, material);

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
