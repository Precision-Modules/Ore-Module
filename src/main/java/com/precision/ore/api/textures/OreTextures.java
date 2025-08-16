package com.precision.ore.api.textures;

import codechicken.lib.texture.TextureUtils.IIconRegister;
import com.precision.ore.OreModule;
import com.precision.ore.api.renderer.DrillHeadRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OreTextures {

	public static final List<IIconRegister> iconRegisters = new ArrayList<>();

	public static final DrillHeadRenderer DRILL_HEAD_RENDERER = new DrillHeadRenderer();

	@SideOnly(Side.CLIENT)
	public static void register(TextureMap textureMap) {
		OreModule.logger.info("Loading Ore Module meta tile entity texture sprites...");
		for (IIconRegister iconRegister : iconRegisters) {
			iconRegister.registerIcons(textureMap);
		}
	}
}
