package com.precision.ore.asm;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("OreModuleLoadingPlugin")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class PrecisionLoadingPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {"com.precision.ore.asm.PrecisionTransformer"};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Nullable @Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
