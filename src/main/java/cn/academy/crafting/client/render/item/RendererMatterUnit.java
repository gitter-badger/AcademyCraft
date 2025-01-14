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
package cn.academy.crafting.client.render.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cn.academy.core.client.Resources;
import cn.academy.crafting.ModuleCrafting;
import cn.academy.crafting.item.ItemMatterUnit;
import cn.academy.crafting.item.ItemMatterUnit.MatterMaterial;
import cn.liutils.util.client.HudUtils;
import cn.liutils.util.client.RenderUtils;
import cn.liutils.util.helper.GameTimer;

/**
 * @author WeAthFolD
 *
 */
public class RendererMatterUnit implements IItemRenderer {
	
	ResourceLocation texMask;
	
	public RendererMatterUnit() {
		texMask = Resources.getTexture("items/matter_unit/mask");
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		ItemMatterUnit item = ModuleCrafting.matterUnit;
		GL11.glColor4d(1, 1, 1, 1);
		if(type != ItemRenderType.INVENTORY) {
			RenderUtils.drawEquippedItem(stack, 0.0625f);
			GL11.glPushMatrix(); {
				GL11.glColorMask(false,false,false,false);
				RenderUtils.drawEquippedItem(0.0626f, texMask, texMask);
				GL11.glColorMask(true, true, true, true);
				
				GL11.glDepthFunc(GL11.GL_EQUAL);
				MatterMaterial mat = item.getMaterial(stack);
				RenderUtils.drawEquippedItemOverlay(0.0626f, mat.texture);
				GL11.glDepthFunc(GL11.GL_LEQUAL);
			} GL11.glPopMatrix();
		} else {
			RenderUtils.renderItemInventory(stack);
			
			GL11.glPushMatrix();
			GL11.glTranslated(0, 0, 10);
			
			GL11.glColor4d(1, 1, 1, 1);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glColorMask(false, false, false, false);
			GL11.glDepthMask(true);
			RenderUtils.loadTexture(texMask);
			HudUtils.rect(16, 16);
			GL11.glColorMask(true, true, true, true);
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			GL11.glColor4d(1, 1, 1, 1);
			GL11.glDepthMask(false);
			GL11.glDepthFunc(GL11.GL_EQUAL);
			RenderUtils.loadTexture(item.getMaterial(stack).texture);
			double du = -(GameTimer.getAbsTime() / 100000.0) % 1.0, dv = (GameTimer.getAbsTime() / 10000.0) % 1.0;
			HudUtils.rawRect(0, 0, du, dv, 16, 16, 1, 1);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			GL11.glPopMatrix();
		}
	}

}
