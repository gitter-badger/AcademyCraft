package cn.academy.misc.achievements;

import java.util.HashMap;
import java.util.HashSet;

import cn.academy.ability.api.Category;
import cn.academy.ability.api.Skill;
import cn.academy.ability.api.data.AbilityData;
import cn.academy.ability.api.event.LevelChangeEvent;
import cn.academy.ability.api.event.SkillLearnEvent;
import cn.academy.crafting.api.event.MatterUnitHarvestEvent;
import cn.academy.misc.achievements.aches.AchEvItemCrafted;
import cn.academy.misc.achievements.aches.AchEvLevelChange;
import cn.academy.misc.achievements.aches.AchEvMatterUnitHarvest;
import cn.academy.misc.achievements.aches.AchEvSkillLearn;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

/**
 * @author EAirPeter
 */
public final class DispatcherAch {

	public static final DispatcherAch INSTANCE = new DispatcherAch();
	
	
	//cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent
	
	private HashMap<Item, HashSet<AchEvItemCrafted>> hcItemCrafted = new HashMap<Item, HashSet<AchEvItemCrafted>>();
	
	public void rgItemCrafted(Item item, AchEvItemCrafted ach) {
		if (hcItemCrafted.containsKey(item))
			hcItemCrafted.get(item).add(ach);
		else {
			HashSet<AchEvItemCrafted> set = new HashSet<AchEvItemCrafted>();
			set.add(ach);
			hcItemCrafted.put(item, set);
		}
		hcItemCrafted.get(item).add(ach);
	}
	
	public void urItemCrafted(Item item, AchEvItemCrafted ach) {
		HashSet<AchEvItemCrafted> set = hcItemCrafted.get(item);
		if (set != null)
			set.remove(ach);
	}
	
	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event) {
		HashSet<AchEvItemCrafted> set = hcItemCrafted.get(event.crafting.getItem());
		if (set != null)
			for (AchEvItemCrafted a : set)
				if (a.accept(event))
					event.player.triggerAchievement(a);
	}
	
	
	//cn.academy.ability.api.event.LevelChangeEvent
	
	private HashMap<Category, AchEvLevelChange[]> hcLevelChange = new HashMap<Category, AchEvLevelChange[]>();
	
	public void rgLevelChange(Category cat, int lv, AchEvLevelChange ach) {
		if (hcLevelChange.containsKey(cat))
			hcLevelChange.get(cat)[lv - 1] = ach;
		else {
			AchEvLevelChange[] arr = new AchEvLevelChange[5];
			arr[lv - 1] = ach;
			hcLevelChange.put(cat, arr);
		}
	}
	
	public void urLevelChange(Category cat, int lv) {
		AchEvLevelChange[] arr = hcLevelChange.get(cat);
		if (arr != null)
			arr[lv - 1] = null;
	}
	
	@SubscribeEvent
	public void onLevelChange(LevelChangeEvent event) {
		AbilityData data = AbilityData.get(event.player);
		int xlv = data.getLevel() - 1;
		AchEvLevelChange[] arr = hcLevelChange.get(data.getCategory());
		if (arr != null && xlv >= 0 && arr[xlv] != null && arr[xlv].accept(event))
			event.player.triggerAchievement(arr[xlv]);
	}
	
	
	//cn.academy.crafting.api.event.MatterUnitHarvestEvent
	
	private HashMap<Block, AchEvMatterUnitHarvest> hcMatterUnitHarvest = new HashMap<Block, AchEvMatterUnitHarvest>();
	
	public void rgMatterUnitHarvest(Block blo, AchEvMatterUnitHarvest ach) {
		hcMatterUnitHarvest.put(blo, ach);
	}
	
	public void urMatterUnitHarvest(Block blo) {
		hcMatterUnitHarvest.remove(blo);
	}
	
	@SubscribeEvent
	public void onMatterUnitHarvest(MatterUnitHarvestEvent event) {
		AchEvMatterUnitHarvest ach = hcMatterUnitHarvest.get(event.mat.block);
		if (ach != null && ach.accept(event))
			event.player.triggerAchievement(ach);
	}
	
	
	//cn.academy.ability.api.event.SkillLearnEvent
	
	private HashMap<Skill, AchEvSkillLearn> hcSkillLearn = new HashMap<Skill, AchEvSkillLearn>();
	
	public void rgSkillLearn(Skill skill, AchEvSkillLearn ach) {
		hcSkillLearn.put(skill, ach);
	}
	
	public void urSkillLearn(Skill skill) {
		hcSkillLearn.remove(skill);
	}
	
	@SubscribeEvent
	public void onSkillLearn(SkillLearnEvent event) {
		AchEvSkillLearn ach = hcSkillLearn.get(event.skill);//CHANGED HERE
		if (ach != null && ach.accept(event))
			event.player.triggerAchievement(ach);
	}
	
	
	//Init
	
	private DispatcherAch() {
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	//stub method for loading
	public static void init() {
	}
	
}
