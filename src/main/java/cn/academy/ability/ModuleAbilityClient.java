package cn.academy.ability;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

import cn.academy.ability.api.preset.PresetData;
import cn.academy.ability.client.ui.PresetEditUI;
import cn.academy.core.registry.RegACKeyHandler;
import cn.annoreg.core.Registrant;
import cn.annoreg.mc.RegSubmoduleInit;
import cn.liutils.util.helper.KeyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Registrant
@RegSubmoduleInit(side = RegSubmoduleInit.Side.CLIENT_ONLY)
@SideOnly(Side.CLIENT)
public class ModuleAbilityClient {
	
	@RegACKeyHandler(name = "Edit Preset", defaultKey = Keyboard.KEY_N)
	public static KeyHandler keyEditPreset = new KeyHandler() {
		@Override
		public void onKeyDown() {
			PresetData data = PresetData.get(Minecraft.getMinecraft().thePlayer);
			if(data.isActive()) {
				PresetEditUI.guiHandler.openClientGui();
			} else {
				System.out.println("Not active!");
			}
			System.out.println("awefrawef");
		}
	};

	public static void init() {
		
	}
	
}
