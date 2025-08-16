package com.precision.ore.integration.jei;

import com.precision.ore.OreModule;
import gregtech.integration.jei.basic.BasicRecipeCategory;
import javax.annotation.Nonnull;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeWrapper;

public abstract class CustomBasicRecipeCategory<T, W extends IRecipeWrapper> extends BasicRecipeCategory<T, W> {

	public CustomBasicRecipeCategory(String uniqueName, String localKey, IDrawable background, IGuiHelper guiHelper) {
		super(uniqueName, localKey, background, guiHelper);
	}

	@Nonnull
	@Override
	public String getModName() {
		return OreModule.MODID;
	}
}
