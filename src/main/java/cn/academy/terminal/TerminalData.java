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
package cn.academy.terminal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import cn.academy.terminal.event.TerminalInstalledEvent;
import cn.annoreg.core.Registrant;
import cn.annoreg.mc.network.Future;
import cn.annoreg.mc.network.Future.FutureCallback;
import cn.annoreg.mc.network.RegNetworkCall;
import cn.annoreg.mc.s11n.StorageOption;
import cn.annoreg.mc.s11n.StorageOption.Data;
import cn.annoreg.mc.s11n.StorageOption.Target;
import cn.liutils.registry.RegDataPart;
import cn.liutils.util.helper.DataPart;
import cn.liutils.util.helper.PlayerData;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author WeAthFolD
 */
@Registrant
@RegDataPart("terminal")
public class TerminalData extends DataPart {
	
	private Set<Integer> installedList = new HashSet();
	private boolean isInstalled;
	
	public TerminalData() {
		int size = AppRegistry.enumeration().size();
	}
	
	public List<Integer> getInstalledApps() {
		return ImmutableList.copyOf(installedList);
	}
	
	public boolean isInstalled(int appid) {
		return installedList.contains(appid);
	}
	
	public boolean isInstalled(App app) {
		return isInstalled(app.getID());
	}
	
	public boolean isTerminalInstalled() {
		return isInstalled;
	}
	
	public void install() {
		checkSide(false);
		if(!isInstalled) {
			isInstalled = true;
			sync();
			
			MinecraftForge.EVENT_BUS.post(new TerminalInstalledEvent(getPlayer()));
			informInstallAtClient(getPlayer());
		}
	}
	
	/**
	 * Make a sync query from client and call the callback when received sync.
	 */
	@SideOnly(Side.CLIENT)
	public void querySync(final FutureCallback callback) {
		doQuerySync(Future.create(new FutureCallback() {

			@Override
			public void onReady(Object val) {
				callback.onReady(val);
				fromNBTSync((NBTTagCompound) val);
			}
			
		}));
	}
	
	public void installApp(App app) {
		installApp(app.getID());
	}
	
	/**
	 * Must called in SERVER side.
	 */
	public void installApp(int appid) {
		if(isRemote()) {
			throw new RuntimeException("Not allowed in client side!");
		}
		doInstall(appid);
		installSync(appid);
	}
	
	public static TerminalData get(EntityPlayer player) {
		return PlayerData.get(player).getPart(TerminalData.class);
	}

	@Override
	public void tick() {}

	@Override
	public void fromNBT(NBTTagCompound tag) {
		isInstalled = tag.getBoolean("i");
		
		int[] arr = tag.getIntArray("learned");
		if(arr.length == 0) {
			// Build the pre-install app list
			for(App a : AppRegistry.enumeration()) {
				if(a.isPreInstalled()) {
					installedList.add(a.appid);
				}
			}
		} else {
			for(int i = 0; i < arr.length; ++i)
				installedList.add(arr[i]);
		}
		
		NBTTagList list = (NBTTagList) tag.getTag("data");
		if(list == null)
			return;
	}

	@Override
	public NBTTagCompound toNBT() {
		NBTTagCompound ret = new NBTTagCompound();
		
		Integer[] iarr = installedList.toArray(new Integer[0]);
		int[] arr = new int[iarr.length];
		for(int i = 0; i < arr.length; ++i)
			arr[i] = iarr[i];
		
		ret.setIntArray("learned", arr);
		
		ret.setBoolean("i", isInstalled);
		
		return ret;
	}
	
	@RegNetworkCall(side = Side.SERVER, thisStorage = StorageOption.Option.INSTANCE)
	private void doQuerySync(@Data Future future) {
		future.setAndSync(toNBTSync());
	}
	
	@RegNetworkCall(side = Side.SERVER, thisStorage = StorageOption.Option.INSTANCE)
	public void clientSync(@Target EntityPlayer player, @Data NBTTagCompound tag) {
		fromNBT(tag);
	}
	
	@RegNetworkCall(side = Side.CLIENT, thisStorage = StorageOption.Option.INSTANCE)
	private void installSync(@Data Integer appid) {
		doInstall(appid);
	}
	
	@RegNetworkCall(side = Side.CLIENT)
	private static void informInstallAtClient(@Target EntityPlayer player) {
		MinecraftForge.EVENT_BUS.post(new TerminalInstalledEvent(player));
	}
	
	private void doInstall(Integer appid) {
		installedList.add(appid);
	}
}
