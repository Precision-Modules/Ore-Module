package com.precision.ore;

import net.minecraftforge.common.config.Config;

@Config(modid = OreModule.MODID, name = OreModule.MODID, category = "Ore")
public class OreConfig {

    @Config.Comment("Disable GregTech Ore generation and Remove all Ore Blocks")
    @Config.RequiresMcRestart
    public static boolean disableGregTechOreGeneration = true;

    @Config.Comment("Disable and Remove GregTech Ore Scanners")
    @Config.RequiresMcRestart
    public static boolean disableGregTechScanners = true;

    @Config.Comment({"Disable Ore Module MetaTileEntities Registration",
            "MetaTileEntities should be added manually",
            "For modpack authors only"})
    @Config.RequiresMcRestart
    public static boolean registerMetaTileEntities = true;

    @Config.Comment({"Disable Ore Module Items Registration",
            "Items should be added manually",
            "For modpack authors only"})
    @Config.RequiresMcRestart
    public static boolean registerItems = true;

    @Config.Comment({"Disable Ore Module Item Recipes Registration",
            "Item Recipes should be added manually",
            "For modpack authors only"})
    @Config.RequiresMcRestart
    public static boolean registerRecipes = true;
}
