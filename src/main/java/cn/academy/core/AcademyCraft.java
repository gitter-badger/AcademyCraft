/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.academy.core;

import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import cn.academy.core.util.ValuePipeline;
import cn.annoreg.core.Registrant;
import cn.annoreg.core.RegistrationManager;
import cn.annoreg.core.RegistrationMod;
import cn.annoreg.mc.RegItem;
import cn.annoreg.mc.RegMessageHandler;
import cn.liutils.crafting.CustomMappingHelper;
import cn.liutils.crafting.RecipeRegistry;
import cn.liutils.ripple.ScriptFunction;
import cn.liutils.ripple.ScriptProgram;
import cn.liutils.util.client.shader.ShaderProgram;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Academy Craft Mod Main Class
 * @author acaly, WeathFolD, KS
 *
 */
@Mod(modid = "academy-craft", name = "AcademyCraft", version = AcademyCraft.VERSION, dependencies="required-after:LIUtils")
@RegistrationMod(pkg = "cn.academy.", res = "academy", prefix = "ac_")
@Registrant
public class AcademyCraft {
	
	@Instance("academy-craft")
    public static AcademyCraft INSTANCE;
    
	public static final boolean DEBUG_MODE = false;
	
	public static final String VERSION = "1.0a";

    public static final Logger log = (Logger) LogManager.getLogger("AcademyCraft");
    
    public static Configuration config;
    
    /**
     * The globally used script program.
     */
    private static ScriptProgram script;
    
    private static boolean scriptLoaded;
    
    /**
     * The globally used value pipeline.
     */
    public static final ValuePipeline pipeline = new ValuePipeline();
    
    public static RecipeRegistry recipes = new RecipeRegistry();

    @RegMessageHandler.WrapperInstance
    public static SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE
            .newSimpleChannel("academy-network");
    
    @RegItem
    @RegItem.UTName("logo")
    public static Item logo;
    
    public static CreativeTabs cct = new CreativeTabs("AcademyCraft") {
        @Override
        public Item getTabIconItem() {
            return logo;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log.info("Starting AcademyCraft");
        log.info("Copyright (c) Lambda Innovation, 2013-2015");
        log.info("http://ac.li-dev.cn/");
        
        config = new Configuration(event.getSuggestedConfigurationFile());
        
        reloadScript();
        
        RegistrationManager.INSTANCE.registerAll(this, "PreInit");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        RegistrationManager.INSTANCE.registerAll(this, "Init");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        RegistrationManager.INSTANCE.registerAll(this, "PostInit");
        
        try { 
        	Class.forName("ic2.core.IC2");
        	try {
        		Class.forName("cn.academy.support.ic2.IC2Support").getMethod("init").invoke(this);
        	} catch (Throwable e) {
        		log.error("Failed to initialize IC2 support", e);
        	}
        } catch(Throwable e) {}
        
        try { 
        	Class.forName("cofh.thermalexpansion.ThermalExpansion");
        	try {
        		Class.forName("cn.academy.support.te.TESupport").getMethod("init").invoke(this);
        	} catch (Throwable e) {
        		log.error("Failed to initialize TE support", e);
        	}
        } catch(Throwable e) {}
        
        recipes.addRecipeFromResourceLocation(new ResourceLocation("academy:recipes/default.recipe"));
        
        if(DEBUG_MODE) {
        	System.out.printf("|-------------------------------------------------------\n");
        	System.out.printf("| AC Recipe Name Mappings\n");
        	System.out.printf("|--------------------------|----------------------------\n");
        	System.out.printf(String.format("| %-25s| Object Name\n", "Recipe Name"));
        	System.out.printf("|--------------------------|----------------------------\n");
	        for(Entry<String, Object> entry : recipes.nameMapping.entrySet()) {
	        	Object obj = entry.getValue();
	        	String str1 = entry.getKey(), str2;
	        	if(obj instanceof Item) {
	        		str2 = StatCollector.translateToLocal(((Item)obj).getUnlocalizedName() + ".name");
	        	} else if(obj instanceof Block) {
	        		str2 = StatCollector.translateToLocal(((Block)obj).getUnlocalizedName() + ".name");
	        	} else {
	        		str2 = obj.toString();
	        	}
	        	System.out.printf(String.format("| %-25s| %s\n", str1, str2));
	        }
	        System.out.printf("|-------------------------------------------------------\n");
        }
        
        recipes = null; //Doesn't need it after loading
    }
    
    @SideOnly(Side.CLIENT)
    @EventHandler
    public void postInit2(FMLPostInitializationEvent event) {
    	ShaderProgram.releaseResources();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        RegistrationManager.INSTANCE.registerAll(this, "StartServer");
    }
    
    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
    	config.save();
    }
    
    /**
     * Reload all the scripts. Used for debugging.
     */
    public static void reloadScript() {
    	if(!scriptLoaded || DEBUG_MODE) {
    		scriptLoaded = true;
    		
    		// Load the scripts
    		ScriptProgram repl = new ScriptProgram();
            String[] scripts = {
            	"generic", "ability", "electromaster", "teleporter", "meltdowner",
            	"generic_skills"
            };
            for(String s : scripts)
            	repl.loadScript(new ResourceLocation("academy:scripts/" + s + ".r"));
            
            script = repl;
    	}
    }
    
    public static ScriptProgram getScript() {
    	return script;
    }
    
    public static void addToRecipe(Class klass) {
    	CustomMappingHelper.addMapping(recipes, klass);
    }
    
    public static ScriptFunction getFunction(String name) {
    	return script.root.getFunction("ac." + name);
    }
    
    public static double getDouble(String name) {
    	return script.root.getDouble("ac." + name);
    }
    
    public static double getInt(String name) {
    	return script.root.getInteger("ac." + name);
    }   
    
    public static float getFloat(String name) {
    	return script.root.getFloat("ac." + name);
    }

}