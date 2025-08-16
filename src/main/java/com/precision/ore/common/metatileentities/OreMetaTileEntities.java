package com.precision.ore.common.metatileentities;

import com.precision.ore.OreModule;
import com.precision.ore.common.metatileentities.multiblockpart.MetaTileEntityDrillHeadHolder;
import com.precision.ore.common.metatileentities.multiblocks.MetaTileEntityBasicMiner;
import com.precision.ore.common.metatileentities.multiblocks.MetaTileEntityCoalMiner;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.util.ResourceLocation;

public class OreMetaTileEntities {

	public static MetaTileEntityDrillHeadHolder DRILL_HEAD_HOLDER;
	public static MetaTileEntityCoalMiner COAL_MINER;
	public static MetaTileEntityBasicMiner BASIC_MINER;

	public static void init() {
		OreModule.logger.info("Ore Module Registering Meta Tile Entities...");
		DRILL_HEAD_HOLDER = MetaTileEntities.registerMetaTileEntity(
				11000, new MetaTileEntityDrillHeadHolder(oreModuleID("drill_head_holder")));
		COAL_MINER =
				MetaTileEntities.registerMetaTileEntity(11001, new MetaTileEntityCoalMiner(oreModuleID("coal_miner")));
		BASIC_MINER = MetaTileEntities.registerMetaTileEntity(
				11002, new MetaTileEntityBasicMiner(oreModuleID("basic_miner")));
	}

	private static ResourceLocation oreModuleID(String name) {
		return new ResourceLocation(OreModule.MODID, name);
	}
}
