package com.lizongbo.codegentool.world;

import com.lizongbo.codegentool.LogUtil;

/**
 * 安装运营平台服务器的Resin
 * @author linyaoheng
 */
public class OperateServerResinInstall {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
//		安装Resin
		LogUtil.printLog("install resin starting");
		InstallResin.genOperateResinServer(worldName);
		LogUtil.printLog("install resin completely");
	}
	
}
