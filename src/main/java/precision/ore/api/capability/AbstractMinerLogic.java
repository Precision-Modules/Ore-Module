package precision.ore.api.capability;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import precision.ore.api.metatileentities.OreMultiblockAbility;
import precision.ore.api.worldgen.vein.BedrockOreVeinHandler;
import precision.ore.common.metatileentities.multiblocks.MetaTileEntityAbstractMiner;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMinerLogic {

    private final int MAX_PROGRESS = 200;
    protected int progressTime = 0;
    protected final MetaTileEntityAbstractMiner metaTileEntity;

    protected boolean isActive;
    protected boolean isWorkingEnabled = true;
    protected boolean wasActiveAndNeedsUpdate;
    protected boolean isDone = false;
    protected boolean isInventoryFull;
    protected int previousLayer;

    protected BedrockOreVeinHandler.OreVeinWorldEntry vein = null;

    public AbstractMinerLogic(MetaTileEntityAbstractMiner metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
    }

    protected MetaTileEntityAbstractMiner getMetaTileEntity(){
        return metaTileEntity;
    }

    private int getLayer(){
        return getMetaTileEntity().getLayer();
    }

    /**
     * Performs the actual drilling
     * Call this method every tick in update
     */
    public void performDrilling() {
        if (getMetaTileEntity().getWorld().isRemote) return;

        if(!getMetaTileEntity().getAbilities(OreMultiblockAbility.DRILL_HOLDER).get(0).hasDrillHead())
            return;

        // if we have no Ore, try to get a new one
        if (vein == null || getLayer() != previousLayer) {
            progressTime = 0;
            if (!acquireNewOre())
                return; // stop if we still have no Ore
        }

        previousLayer = getLayer();
        // drills that cannot work do nothing
        if (!this.isWorkingEnabled)
            return;

        // check if drilling is possible
        if (!checkCanDrain())
            return;

        if(!consumeFluid(true)) {
            if(this.isActive)
                setActive(false);
            return;
        }

        // if the inventory is not full, drain energy etc. from the drill
        // the storages have already been checked earlier
        if (!isInventoryFull) {
            // actually drain the energy
            consumeEnergy(false);

            // since energy is being consumed the rig is now active
            if (!this.isActive)
                setActive(true);
        } else {
            // the rig cannot drain, therefore it is inactive
            if (this.isActive)
                setActive(false);
            return;
        }

        // increase progress
        progressTime++;
        if (progressTime % getMaxProgressTime() != 0)
            return;
        progressTime = 0;

        List<ItemStack> outOres = new ArrayList<>();

        for(Material ore : vein.getDefinition().getStoredOres())
            if(GTValues.RNG.nextInt(vein.getDefinition().getAllOresWeight()) <= vein.getDefinition().getOreWeight(ore))
                outOres.add(OreDictUnifier.get(OrePrefix.crushed, ore, getOrePerCycle()));

        if (getMetaTileEntity().fillInventory(outOres, true)) {
            getMetaTileEntity().fillInventory(outOres, false);
            consumeFluid(false);
            getMetaTileEntity().getDrillHeadHolder().damageDrillHead(1);
            BedrockOreVeinHandler.depleteVein(getMetaTileEntity().getWorld(), getChunkX(), getChunkZ(), getLayer(), 1, false);
        } else {
            isInventoryFull = true;
            setActive(false);
            setWasActiveAndNeedsUpdate(true);
        }
    }

    abstract protected boolean checkCanDrain();

    abstract protected boolean consumeEnergy(boolean simulate);

    /**
     *
     * @return true if the rig is able to drain, else false
     */
    
    abstract protected boolean consumeFluid(boolean simulate);

    private boolean acquireNewOre() {
        this.vein = BedrockOreVeinHandler.getOreVeinWorldEntry(getMetaTileEntity().getWorld(), getChunkX(), getChunkZ(), getLayer());
        return this.vein != null;
    }

    protected int getOrePerCycle(){
        return getMetaTileEntity().getDrillHeadHolder().getDrillHeadLevel();
    }

    protected int getChunkX() {
        return getBlockPos().getX() / 16;
    }

    protected int getChunkZ() {
        return getBlockPos().getZ() / 16;
    }

    protected BlockPos getBlockPos(){
        return metaTileEntity.getPos();
    }

    /**
     *
     * @return true if the rig is active
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     *
     * @param active the new state of the rig's activity: true to change to active, else false
     */
    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            getMetaTileEntity().markDirty();
            if (getMetaTileEntity().getWorld() != null && !metaTileEntity.getWorld().isRemote) {
                getMetaTileEntity().writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    /**
     *
     * @param isWorkingEnabled the new state of the rig's ability to work: true to change to enabled, else false
     */
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        if (this.isWorkingEnabled != isWorkingEnabled) {
            this.isWorkingEnabled = isWorkingEnabled;
            getMetaTileEntity().markDirty();
            if (getMetaTileEntity().getWorld() != null && !metaTileEntity.getWorld().isRemote) {
                getMetaTileEntity().writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
            }
        }
    }

    /**
     *
     * @return whether working is enabled for the logic
     */
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    /**
     *
     * @return whether the rig is currently working
     */
    public boolean isWorking() {
        return isActive && isWorkingEnabled;
    }

    /**
     *
     * @return the current progress towards producing Ore of the rig
     */
    public int getProgressTime() {
        return this.progressTime;
    }

    public double getProgressPercent() {
        return getProgressTime() * 1.0 / getMaxProgressTime();
    }

    public int getMaxProgressTime(){
        return MAX_PROGRESS;
    }

    /**
     *
     * @return whether the inventory is full
     */
    public boolean isInventoryFull() {
        return this.isInventoryFull;
    }

    /**
     * writes all needed values to NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeToNBT(NBTTagCompound)} method
     */
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        data.setBoolean("wasActiveAndNeedsUpdate", this.wasActiveAndNeedsUpdate);
        data.setBoolean("isDone", isDone);
        data.setInteger("progressTime", progressTime);
        data.setBoolean("isInventoryFull", isInventoryFull);
        return data;
    }

    /**
     * reads all needed values from NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#readFromNBT(NBTTagCompound)} method
     */
    public void readFromNBT(@Nonnull NBTTagCompound data) {
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        this.wasActiveAndNeedsUpdate = data.getBoolean("wasActiveAndNeedsUpdate");
        this.isDone = data.getBoolean("isDone");
        this.progressTime = data.getInteger("progressTime");
        this.isInventoryFull = data.getBoolean("isInventoryFull");
    }

    /**
     * writes all needed values to InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeInitialSyncData(PacketBuffer)} method
     */
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeBoolean(this.wasActiveAndNeedsUpdate);
        buf.writeInt(this.progressTime);
        buf.writeBoolean(this.isInventoryFull);
    }

    /**
     * reads all needed values from InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)} method
     */
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        setActive(buf.readBoolean());
        setWorkingEnabled(buf.readBoolean());
        setWasActiveAndNeedsUpdate(buf.readBoolean());
        this.progressTime = buf.readInt();
        this.isInventoryFull = buf.readBoolean();
    }

    /**
     * reads all needed values from CustomData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveCustomData(int, PacketBuffer)} method
     */
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            getMetaTileEntity().scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            getMetaTileEntity().scheduleRenderUpdate();
        }
    }

    /**
     *
     * @return whether the rig was active and needs an update
     */
    public boolean wasActiveAndNeedsUpdate() {
        return this.wasActiveAndNeedsUpdate;
    }

    /**
     * set whether the rig was active and needs an update
     *
     * @param wasActiveAndNeedsUpdate the state to set
     */
    public void setWasActiveAndNeedsUpdate(boolean wasActiveAndNeedsUpdate) {
        this.wasActiveAndNeedsUpdate = wasActiveAndNeedsUpdate;
    }
}
