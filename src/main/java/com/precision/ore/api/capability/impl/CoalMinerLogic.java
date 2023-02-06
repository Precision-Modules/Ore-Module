package com.precision.ore.api.capability.impl;

import com.precision.ore.common.metatileentities.multiblocks.MetaTileEntityCoalMiner;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import com.precision.ore.api.capability.AbstractMinerLogic;

import java.util.ArrayList;
import java.util.List;

public class CoalMinerLogic extends AbstractMinerLogic {

    public CoalMinerLogic(MetaTileEntityCoalMiner metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected MetaTileEntityCoalMiner getMetaTileEntity() {
        return ((MetaTileEntityCoalMiner) super.getMetaTileEntity());
    }

    @Override
    protected boolean consumeEnergy(boolean simulate) {
        return getMetaTileEntity().drainEnergy(simulate);
    }

    @Override
    protected boolean consumeFluid(boolean simulate) {
        return true;
    }

    protected boolean checkCanDrain() {
        if (!consumeEnergy(true)) {
            if (progressTime >= 2) {
                if (ConfigHolder.machines.recipeProgressLowEnergy)
                    this.progressTime = 1;
                else
                    this.progressTime = Math.max(1, progressTime - 2);
            }
            return false;
        }

        List<ItemStack> ores = new ArrayList<>();
        for(Material ore : vein.getDefinition().getStoredOres())
            ores.add(OreDictUnifier.get(OrePrefix.crushed, ore, getOrePerCycle()));

        if (getMetaTileEntity().fillInventory(ores, true)) {
            this.isInventoryFull = false;
            return true;
        }
        this.isInventoryFull = true;

        if (isActive()) {
            setActive(false);
            setWasActiveAndNeedsUpdate(true);
        }
        return false;
    }
}
