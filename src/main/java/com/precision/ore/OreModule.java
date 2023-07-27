package com.precision.ore;

import codechicken.lib.texture.TextureUtils;
import com.precision.ore.api.textures.OreTextures;
import com.precision.ore.api.worldgen.PrecisionWorldGenRegistry;
import com.precision.ore.api.worldgen.vein.BedrockOreVeinSaveData;
import com.precision.ore.common.items.OreMetaItems;
import com.precision.ore.common.metatileentities.OreMetaTileEntities;
import com.precision.ore.loaders.recipes.OreRecipeLoader;
import com.precision.ore.network.PacketOreVeinList;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.core.network.internal.NetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;


@Mod(modid = OreModule.MODID,
        name = OreModule.NAME,
        version = OreModule.VERSION,
        dependencies = "required-after:gregtech")
public class OreModule {

    public OreModule() {}

    public static final String MODID = "ore-module";
    public static final String NAME = "Precision: Modules — Ore";
    public static final String VERSION = "1.0.0";

    public static final Logger logger = LogManager.getLogger(NAME);

    public static List<Material> drillHeadMaterials;


    @Mod.EventHandler
    void construction(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    void preInit(FMLPreInitializationEvent event) {
        NetworkHandler.getInstance().registerPacket(PacketOreVeinList.class);
        TextureUtils.addIconRegister(OreTextures::register);
        OreMetaTileEntities.init();
    }

    @Mod.EventHandler
    void init(FMLInitializationEvent event) {
        PrecisionWorldGenRegistry.INSTANCE.initializeRegistry();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    void registerItems(RegistryEvent.Register<Item> event) {
        OreMetaItems.init();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        OreRecipeLoader.init();
    }

    @SubscribeEvent
    void registerMaterials(MaterialEvent event) {
        Materials.Iron.addFlags(MaterialFlags.GENERATE_FRAME);
    }

    @SubscribeEvent()
    void postRegisterMaterials(PostMaterialEvent event){
        drillHeadMaterials = GregTechAPI.materialManager.getRegisteredMaterials().stream()
                .filter(material -> material != null && material.hasProperty(PropertyKey.TOOL) && material.hasFlags(MaterialFlags.GENERATE_DENSE, MaterialFlags.GENERATE_RING, MaterialFlags.GENERATE_LONG_ROD))
                .collect(Collectors.toList());
    }

    @SubscribeEvent
    public static void onWorldUnloadEvent(WorldEvent.Unload event) {
        BedrockOreVeinSaveData.setDirty();
    }

    @SubscribeEvent
    public static void onWorldSaveEvent(WorldEvent.Save event) {
        BedrockOreVeinSaveData.setDirty();
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
            if (!world.isRemote) {
                BedrockOreVeinSaveData saveOreData = (BedrockOreVeinSaveData) world.loadData(BedrockOreVeinSaveData.class, BedrockOreVeinSaveData.dataName);
                if(saveOreData == null){
                    saveOreData = new BedrockOreVeinSaveData(BedrockOreVeinSaveData.dataName);
                    world.setData(BedrockOreVeinSaveData.dataName, saveOreData);
                }
                BedrockOreVeinSaveData.setInstance(saveOreData);
            }
        }
    }
}



