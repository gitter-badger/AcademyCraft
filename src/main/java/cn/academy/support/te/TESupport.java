package cn.academy.support.te;

import cofh.thermalexpansion.block.cell.BlockCell;
import cofh.thermalexpansion.item.TEItems;
import net.minecraft.item.ItemStack;
import cn.academy.crafting.ModuleCrafting;
import cn.academy.energy.ModuleEnergy;
import cn.academy.support.EnergyBlockHelper;
import cpw.mods.fml.common.registry.GameRegistry;

public class TESupport {
	
	/** The convert rate (RF * RATE = IF) */
	public static final float CONV_RATE = 1f;
	
	public static void init() {
		BlockRFInput rfInput = new BlockRFInput();
		BlockRFOutput rfOutput = new BlockRFOutput();
		
		GameRegistry.registerBlock(rfInput, "rf_input");
		GameRegistry.registerBlock(rfOutput, "rf_output");
		
		GameRegistry.registerTileEntity(TileRFInput.class, "rf_input");
		GameRegistry.registerTileEntity(TileRFOutput.class, "rf_output");
		
		EnergyBlockHelper.register(new RFProviderManager());
		EnergyBlockHelper.register(new RFReceiverManager());
		
		GameRegistry.addRecipe(new ItemStack(rfInput), "   ", "abc", "d",
				'a', ModuleEnergy.energyUnit, 'b', ModuleCrafting.machineFrame,
				'c', TEItems.powerCoilGold, 'd', ModuleCrafting.convComp);
		
		
		GameRegistry.addRecipe(new ItemStack(rfInput), "   ", "abc", "d",
				'a', ModuleEnergy.energyUnit, 'b', ModuleCrafting.machineFrame,
				'c', BlockCell.cellBasic, 'd', ModuleCrafting.convComp);
	}
	
}
