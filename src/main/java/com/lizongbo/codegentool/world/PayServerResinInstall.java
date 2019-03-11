package com.lizongbo.codegentool.world;

import com.lizongbo.codegentool.LogUtil;

/**
 * 安装支付服务器的Resin,由于支付服务器的日志比较重要,所以此Resin不能通过此脚本来重复安装
 * @author linyaoheng
 */
public class PayServerResinInstall {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		// 安装Resin
		LogUtil.printLog("PayServerResinInstall starting");
		InstallResin.genPayServerResinServer(worldName);
		LogUtil.printLog("PayServerResinInstall completely");
	}
	
}
