package com.precision.ore.api.renderer;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Matrix4;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import com.precision.ore.OreModule;
import com.precision.ore.api.capability.IDrillHeadHolder;

import java.util.EnumSet;

public class DrillHeadRenderer implements TextureUtils.IIconRegister {

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite drillHead;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite drillHeadActive;
    @SideOnly(Side.CLIENT)
    private final EnumSet<EnumFacing> facingToDraw = EnumSet.of(EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST);

    public DrillHeadRenderer(){
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.drillHead = textureMap.registerSprite(new ResourceLocation(OreModule.MODID, "blocks/multiblock/miner/drill_head"));
        this.drillHeadActive = textureMap.registerSprite(new ResourceLocation(OreModule.MODID, "blocks/multiblock/miner/drill_head_active"));
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, int length, boolean hasDrill, boolean isActive, int drillRGB) {
        if (hasDrill) {
            TextureAtlasSprite sprite = isActive ? drillHeadActive : drillHead;
            for (int i = 0; i < length; ++i) {
                translation.translate(0.0, -1.0, 0.0);
                for (EnumFacing side : facingToDraw) {
                    IVertexOperation[] color = ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240), new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(drillRGB)));
                    Textures.renderFace(renderState, translation, color, side, IDrillHeadHolder.PIPE_CUBOID, sprite, BlockRenderLayer.CUTOUT_MIPPED);
                }
            }
        }
    }
}
