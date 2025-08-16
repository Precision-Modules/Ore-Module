package com.precision.ore.common.tools;

import com.precision.ore.api.worldgen.vein.BedrockOreVeinHandler;
import gregtech.api.items.metaitem.stats.IItemUseManager;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.behaviors.AbstractUsableBehaviour;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class PrimitiveDrillBehavior extends AbstractUsableBehaviour implements IItemUseManager {

	public static final PrimitiveDrillBehavior INSTANCE = new PrimitiveDrillBehavior();
	private static final int USE_DURATION = 100;
	private boolean canUse = false;
	private BedrockOreVeinHandler.OreVeinWorldEntry entry = null;

	public PrimitiveDrillBehavior() {
		super(25);
	}

	@Override
	public EnumAction getUseAction(ItemStack itemStack) {
		return EnumAction.BLOCK;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemStack) {
		return USE_DURATION;
	}

	@Override
	public EnumActionResult onItemUseFirst(
			EntityPlayer player,
			World world,
			BlockPos pos,
			EnumFacing side,
			float hitX,
			float hitY,
			float hitZ,
			EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!stack.equals(ItemStack.EMPTY)) {
			Block block = world.getBlockState(pos).getBlock();
			ItemStack stone = new ItemStack(block);
			if (!stone.equals(ItemStack.EMPTY)
					&& OreDictUnifier.getOreDictionaryNames(stone).contains("stone")) {
				entry = BedrockOreVeinHandler.getOreVeinWorldEntry(world, pos.getX() / 16, pos.getY() / 16, 0);
				if (entry != null && entry.getOperationsRemaining() > 0) {
					canUse = true;
					return EnumActionResult.PASS;
				}
			}
		}
		canUse = false;
		return EnumActionResult.FAIL;
	}

	@Override
	public boolean canStartUsing(ItemStack stack, EntityPlayer player) {
		return getUsesLeft(stack) > 0 && canUse;
	}

	@Override
	public void onItemUsingTick(ItemStack stack, EntityPlayer player, int count) {
		int percentage = 100 - count * 100 / USE_DURATION;
		player.sendStatusMessage(new TextComponentString("Progress: " + percentage + "%"), true);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, EntityPlayer player) {
		if (entry != null && entry.getOperationsRemaining() > 0) {
			Material ore = entry.getDefinition().getStoredOres().get(0);
			if (ore != null) {
				ItemStack oreStack = OreDictUnifier.get(OrePrefix.crushed, ore);
				if (oreStack != ItemStack.EMPTY) {
					player.addItemStackToInventory(oreStack);
					setUsesLeft(stack, getUsesLeft(stack) - 1);
					if (getUsesLeft(stack) == 0) stack = ItemStack.EMPTY;
					entry.decreaseOperations(1);
					canUse = false;
					entry = null;
				}
			}
		}
		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, List<String> lines) {
		super.addInformation(stack, lines);
		lines.add("Max uses: " + totalUses);
		lines.add("Uses left: " + getUsesLeft(stack));
	}
}
