package com.lizongbo.codegentool.world;

import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 初始化玩家服活动信息
 *
 */
public class ServerActivityInit {

	public static void main(String[] args) {
		//初始化到哪个玩家服
		String worldName = System.getenv("worldName");
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 0);
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateZoneConfig(worldName, zoneId);
		
		//初始化活动信息
		InitZoneActivityData(worldName, zoneId);
	}
	
	private static void InitZoneActivityData(String worldName, int zoneId){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("InitZoneActivityData|worldName|" + worldName + "|zoneId|" + zoneId + "|starting");
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String linuxUserName = deployProp.getProperty("linuxUserName").trim();
		String linuxUserPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		String javaBinPath = "/data/bilin/apps/jdk/bin/java";
		String appPath = "/data/bilin/apps/resin_8090/webapps/gecaoshoulie_operateconsole";
		
		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, linuxUserName, linuxUserPwd, 
				javaBinPath + " -Djavaserver.home=" +  appPath
				+ " -Djavaserver.zoneid=" + zoneId
				+ " -cp \"" + appPath + "/WEB-INF/classes:" + appPath + "/WEB-INF/lib/*\""
				+ " net.bilinkeji.gecaoshoulie.logic.activity.ServerStartUpInitWorkerV2 " + zoneId);
		
		LogUtil.printLog("InitZoneActivityData|worldName|" + worldName + "|zoneId|" + zoneId 
				+ "|usedtime|" + (System.currentTimeMillis() - startTime) + "ms");
	}
	
}
