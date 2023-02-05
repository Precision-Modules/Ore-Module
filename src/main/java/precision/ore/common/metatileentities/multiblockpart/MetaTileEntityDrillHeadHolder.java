package precision.ore.common.metatileentities.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import precision.ore.api.capability.IDrillHeadHolder;
import precision.ore.api.metatileentities.OreMultiblockAbility;
import precision.ore.api.textures.OreTextures;
import precision.ore.common.metatileentities.multiblocks.MetaTileEntityAbstractMiner;
import precision.ore.common.tools.DrillHeadBehavior;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class MetaTileEntityDrillHeadHolder extends MetaTileEntityMultiblockPart implements IDrillHeadHolder, IMultiblockAbilityPart<IDrillHeadHolder> {

    private final InventoryDrillHandler drillHandler;

    public MetaTileEntityDrillHeadHolder(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 0);
        this.drillHandler = new InventoryDrillHandler();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDrillHeadHolder(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .slot(this.drillHandler, 0, 79, 34, true, true)
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    private boolean isMinerActive(){
        MetaTileEntityAbstractMiner miner = ((MetaTileEntityAbstractMiner) getController());
        return miner != null && miner.isActive();
    }

    @Override
    public boolean hasDrillHead() {
        return drillHandler.hasDrillHead();
    }

    @Override
    public int getDrillHeadLevel() {
        return drillHandler.getDrillHeadLevel();
    }

    @Override
    public int getDrillHeadColor() {
        return drillHandler.getDrillHeadColor();
    }

    @Override
    public void damageDrillHead(int damage) {
        drillHandler.damageRotor(damage);
    }

    @Override
    public void update() {
        if(!getWorld().isRemote)
            return;

        if(!hasDrillHead())
            return;

        if(isMinerActive())
            dustParticles();
    }

    @Override
    public MultiblockAbility<IDrillHeadHolder> getAbility() {
        return OreMultiblockAbility.DRILL_HOLDER;
    }

    @Override
    public void registerAbilities(List<IDrillHeadHolder> abilityList) {
        abilityList.add(this);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setTag("inventory", drillHandler.serializeNBT());
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.drillHandler.deserializeNBT(data.getCompoundTag("inventory"));
        super.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeCompoundTag(drillHandler.serializeNBT());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            this.drillHandler.deserializeNBT(buf.readCompoundTag());
        } catch (IOException e) {
            // :3
        }
        scheduleRenderUpdate();
    }

    public void dustParticles(){
        int x = getPos().getX();
        int z = getPos().getZ();
        float ySpd = 0.3F + 0.1F * GTValues.RNG.nextFloat();
        float xSpd = (0.1F + 0.2F * GTValues.RNG.nextFloat()) * (GTValues.RNG.nextBoolean() ? -1 : 1);
        float zSpd = (0.1F + 0.2F * GTValues.RNG.nextFloat()) * (GTValues.RNG.nextBoolean() ? -1 : 1);
        getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, x + 0.5F, getPos().getY() - 2, z + 0.5F, xSpd, ySpd, zSpd);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        OreTextures.DRILL_HEAD_RENDERER.render(renderState, translation, pipeline, getWorld() != null ? getPos().getY() : 0, hasDrillHead(), isMinerActive(), getDrillHeadColor());
    }

    private class InventoryDrillHandler extends ItemStackHandler {

        InventoryDrillHandler(){
            super(1);
        }

        @Nullable
        private ItemStack getDrillHeadStack() {
            if (!hasDrillHead())
                return null;
            return getStackInSlot(0);
        }

        @Nullable
        private DrillHeadBehavior getDrillHeadBehavior() {
            ItemStack stack = getStackInSlot(0);
            if (stack.isEmpty())
                return null;

            return DrillHeadBehavior.getInstanceFor(stack);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean hasDrillHead() {
            return getDrillHeadBehavior() != null;
        }

        private int getDrillHeadColor() {
            if (!hasDrillHead()) return 0;
            //noinspection ConstantConditions
            return getDrillHeadBehavior().getPartMaterial(getStackInSlot(0)).getMaterialRGB();
        }

        private int getDrillHeadLevel(){
            if(!hasDrillHead()) return 0;
            //noinspection ConstantConditions
            return getDrillHeadBehavior().getDrillHeadLevel(getStackInSlot(0));
        }

        private void damageRotor(int damageAmount) {
            if (!hasDrillHead()) return;
            //noinspection ConstantConditions
            getDrillHeadBehavior().applyDrillHeadDamage(getStackInSlot(0), damageAmount);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return DrillHeadBehavior.getInstanceFor(stack) != null && super.isItemValid(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            scheduleRenderUpdate();
        }
    }
}
