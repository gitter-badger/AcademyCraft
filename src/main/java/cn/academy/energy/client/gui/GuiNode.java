/**
 * 
 */
package cn.academy.energy.client.gui;

import java.util.List;

import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cn.academy.core.AcademyCraft;
import cn.academy.core.client.ACLangs;
import cn.academy.core.proxy.ACClientProps;
import cn.academy.energy.block.tile.impl.ContainerNode;
import cn.academy.energy.block.tile.impl.TileNode;
import cn.academy.energy.msg.node.MsgInitNode;
import cn.liutils.api.gui.LIGui;
import cn.liutils.api.gui.LIGuiContainer;
import cn.liutils.api.gui.Widget;
import cn.liutils.api.gui.widget.ListVertical;
import cn.liutils.util.HudUtils;
import cn.liutils.util.RenderUtils;
import cn.liutils.util.render.LambdaFont.Align;

/**
 * @author WeathFolD
 *
 */
public class GuiNode extends LIGuiContainer {
	
	ResourceLocation 
		TEX = new ResourceLocation("academy:textures/guis/wireless_node.png"),
		TEX_SELECT = new ResourceLocation("academy:textures/guis/node_choose_net.png");
	
	static final int[] COLOR = { 133, 240, 240 };
	
	final ContainerNode node;
	final TileNode tile;
	
	public boolean synced; //flag set by Messages indicating the packet was sent
	//Sync data
	public List<String> channels;
	public int nNodes;
	public int nGens;

	Page mainPage;
	Choose choosePage;
	
	public GuiNode(ContainerNode c) {
		super(c);
		node = c;
		tile = c.node;
		
		reinit();
	}
	
	private void reinit() {
		gui = new LIGui();
		mainPage = null;
		choosePage = null;
		gui.addWidget(mainPage = new Page());
	}
	
	public boolean isSlotActive() {
		return mainPage.doesListenKey;
	}
	
	//---Main page
	private class Page extends Widget {
		public Page() {
			this.alignStyle = AlignStyle.CENTER;
			this.setSize(207.5, 205.333333);
			this.initTexDraw(TEX, 0, 0, 313, 308);
			this.setTexResolution(384, 384);
		}
		
		public void draw(double mx, double my, boolean hov) {
			GL11.glPushMatrix();
			GL11.glTranslated(-.2, 0.5, 0);
			super.draw(mx, my, hov);
			
			//44.7 40 35 308 120.67 8.67 181 13
			GL11.glColor4d(1, 1, 1, 1);
			double prog = tile.getEnergy() / tile.getMaxEnergy();
			HudUtils.drawRect(45.8, 54.8, 35, 308, 120.67 * prog, 8.67, 181 * prog, 13);
			GL11.glPopMatrix();
			
			RenderUtils.bindColor(COLOR);
			String channel = tile.isConnected() ? tile.getChannel() : ACLangs.notConnected();
			drawText(channel, 81, 14.5, 7, Align.LEFT);
			
			RenderUtils.bindIdentity();
		}
		
		@Override
		public void onAdded() {
			addWidget(new Indicator());
		}
	}
	
	private class Indicator extends Widget {
		public Indicator() {
			setPos(170.5, 11);
			setSize(18 / 1.5, 24 / 1.5);
		}
		
		@Override
		public void draw(double mx, double my, boolean hov) {
			final double tw = 18, th = 24;
			RenderUtils.loadTexture(TEX);
			if(tile.isConnected()) {
				HudUtils.drawRect(0, 0, 315, 64, tw / 1.5, th / 1.5, tw, th);
			} else {
				HudUtils.drawRect(0, 0, 315, 22, tw / 1.5, th / 1.5, tw, th);
			}
		}
		
		@Override
		public void onMouseDown(double mx, double my) {
			mainPage.doesListenKey = false;
			gui.addWidget(choosePage = new Choose());
		}
	}
	
	//---Choose page
	private class Choose extends Widget {
		public Choose() {
			this.setSize(150, 216.5);
			this.initTexDraw(TEX_SELECT, 0, 0, 300, 433);
			this.setTexResolution(512, 512);
			
			choosePage = this;
			this.alignStyle = AlignStyle.CENTER;
		}
		
		@Override
		public void onAdded() {
			addWidget(new ChannelList());
		}
		
		public void draw(double mx, double my, boolean hover) {
			GL11.glPushMatrix();
			GL11.glTranslated(-this.getNode().x, -this.getNode().y, 0);
			GL11.glColor4d(0, 0, 0, .7);
			HudUtils.drawModalRect(0, 0, GuiNode.this.width, GuiNode.this.height);
			GL11.glPopMatrix();
			
			GL11.glColor4d(1, 1, 1, 1);
			super.draw(mx, my, hover);
		}
		
		public void onClose() {
			mainPage.doesListenKey = true;
			dispose();
		}
	}
	
	private class ChannelList extends ListVertical {
		
		boolean loaded;

		public ChannelList() {
			super("channel", 3.5, 38, 128.5, 172.5);
		}
		
		@Override
		public void draw(double mx, double my, boolean h) {
			super.draw(mx, my, h);
			if(!loaded && synced) {
				loaded = true;
				init();
			}
		}
		
		@Override
		public void onAdded() {
			if(synced) {
				loaded = true;
				init();
			}
		}
		
		private void init() {
			for(String s : channels) {
				this.addWidget(new OneChannel(s));
			}
		}
		
	}
	
	private class OneChannel extends Widget {
		private final int 
			ACTIVE_COLOR[] = { 57, 150, 150, 180 },
			FONT_COLOR[] = { 142, 255, 255, 233 };
		final String channel;
		
		public OneChannel(String st) {
			setSize(129.5, 16);

			channel = st;
		}
		
		@Override
		public void draw(double mx, double my, boolean h) {
			if(h) {
				RenderUtils.bindColor(ACTIVE_COLOR);
				HudUtils.drawModalRect(0, 0, width, height);
			}
			drawText(channel, 5, 4, 5);
		}
		
		@Override
		public void onMouseDown(double mx, double my) {
			AcademyCraft.netHandler.sendToServer(new MsgInitNode(GuiNode.this.tile, channel));
			choosePage.dispose();
		}
	}
	
	private static void drawText(String str, double x, double y, double size) {
		ACClientProps.FONT_YAHEI_32.draw(str, x, y, size);
	}
	
	private static void drawText(String str, double x, double y, double size, Align align) {
		ACClientProps.FONT_YAHEI_32.draw(str, x, y, size, align);
	}
	
}