package com.precision.ore.integration.jei;

import com.google.common.collect.ImmutableSet;
import com.precision.ore.OreConfig;
import com.precision.ore.OreModule;
import com.precision.ore.api.worldgen.PrecisionWorldGenRegistry;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.precision.ore.common.items.OreMetaItems.*;

@JEIPlugin
public class OreModuleJEIPlugin implements IModPlugin {

    public static IJeiRuntime jeiRuntime;
    public static IIngredientBlacklist blacklist;

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        OreModuleJEIPlugin.jeiRuntime = jeiRuntime;
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new VirtualOreCategory(0, registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new VirtualOreCategory(1, registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        if (OreConfig.disableGregTechScanners) {
            blacklist.addIngredientToBlacklist(MetaItems.PROSPECTOR_LV.getStackForm());
            blacklist.addIngredientToBlacklist(MetaItems.PROSPECTOR_HV.getStackForm());
            blacklist.addIngredientToBlacklist(MetaItems.PROSPECTOR_LUV.getStackForm());
        }

        if (OreConfig.disableGregTechOreGeneration) {
            Set<OrePrefix> oreTypesToRemove = ImmutableSet.of(
                    OrePrefix.ore,
                    OrePrefix.oreNetherrack,
                    OrePrefix.oreEndstone
            );

            if (ConfigHolder.worldgen.allUniqueStoneTypes) {
                oreTypesToRemove.addAll(
                        ImmutableSet.of(
                                OrePrefix.oreAndesite,
                                OrePrefix.oreDiorite,
                                OrePrefix.oreGranite,
                                OrePrefix.oreBlackgranite,
                                OrePrefix.oreRedgranite,
                                OrePrefix.oreMarble,
                                OrePrefix.oreBasalt
                        )
                );
            }

            for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
                for (OrePrefix prefix : oreTypesToRemove) {
                    ItemStack stack = OreDictUnifier.get(prefix, material, 1);
                    if (stack != null && !stack.isEmpty()) {
                        blacklist.addIngredientToBlacklist(stack);
                    }
                }
            }
        }

        // Parsing precision.ore veins for adding precision.ore vein pages later
        Map<Integer, List<VirtualOreInfo>> veinList = new HashMap<>();
        PrecisionWorldGenRegistry.getBedrockOreVeinDeposit().forEach(def -> {
            if (!veinList.containsKey(def.getLayer())) {
                veinList.put(def.getLayer(), new CopyOnWriteArrayList<>());
            }
            veinList.get(def.getLayer()).add(new VirtualOreInfo(def));
        });

        // Registering recipes and catalysts for precision.ore vein pages
        veinList.forEach((layer, list) -> {
            String veinSpawnID = OreModule.MODID + ":vein_info" + layer;

            registry.addRecipes(list, veinSpawnID);
            registry.addRecipeCatalyst(layer == 0 ? PRIMITIVE_DRILL.getStackForm() : DRILL_HEAD.getStackForm(), veinSpawnID);
            registry.addRecipeCatalyst(PROSPECTOR_LV.getStackForm(), veinSpawnID);
            registry.addRecipeCatalyst(PROSPECTOR_HV.getStackForm(), veinSpawnID);
            registry.addRecipeCatalyst(PROSPECTOR_LUV.getStackForm(), veinSpawnID);
        });
    }
}
