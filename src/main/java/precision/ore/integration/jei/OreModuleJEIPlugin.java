package precision.ore.integration.jei;

import gregtech.common.items.MetaItems;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import org.jetbrains.annotations.NotNull;
import precision.ore.OreConfig;
import precision.ore.OreModule;
import precision.ore.api.worldgen.PrecisionWorldGenRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static precision.ore.common.items.OreMetaItems.*;

@JEIPlugin
public class OreModuleJEIPlugin implements IModPlugin {

    public static IJeiRuntime jeiRuntime;

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        OreModuleJEIPlugin.jeiRuntime = jeiRuntime;
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new VirtualOreCategory(0, registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new VirtualOreCategory(1, registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(@NotNull IModRegistry registry) {
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();

        if(OreConfig.disableGregTechScanners || OreConfig.disableGregTechOreGeneration) {
            blacklist.addIngredientToBlacklist(MetaItems.PROSPECTOR_LV.getStackForm());
            blacklist.addIngredientToBlacklist(MetaItems.PROSPECTOR_HV.getStackForm());
            blacklist.addIngredientToBlacklist(MetaItems.PROSPECTOR_LUV.getStackForm());
        }

        // Parsing precision.ore veins for adding precision.ore vein pages later
        Map<Integer, List<VirtualOreInfo>> veinList = new HashMap<>();
        PrecisionWorldGenRegistry.getBedrockOreVeinDeposit().forEach(def -> {
            if(!veinList.containsKey(def.getLayer())) {
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
