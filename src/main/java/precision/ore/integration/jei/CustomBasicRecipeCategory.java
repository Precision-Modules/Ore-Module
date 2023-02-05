package precision.ore.integration.jei;

import gregtech.integration.jei.recipe.primitive.BasicRecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.jetbrains.annotations.NotNull;
import precision.ore.OreModule;

public abstract class CustomBasicRecipeCategory<T, W extends IRecipeWrapper> extends BasicRecipeCategory<T, W> {

    public CustomBasicRecipeCategory(String uniqueName, String localKey, IDrawable background, IGuiHelper guiHelper) {
        super(uniqueName, localKey, background, guiHelper);
    }

    @NotNull
    @Override
    public String getModName() {
        return OreModule.MODID;
    }
}
