package com.lizongbo.codegentool.tools;

import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.SQLGen4WorldUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;
import com.lizongbo.codegentool.world.DeployCommandUtil;

/**
 * 初始化玩家服信息
 * 包括初始化机器人等
 * 初始化活动信息等
 *
 */
public class GameZoneInitCmdV2 {

	public static void main(String[] args) {
		//初始化到哪个玩家服
		String worldName = System.getenv("worldName");
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 0);
		int arenaRobotCount = StringUtil.toInt(System.getenv("arenaRobotCount"), 10000);
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateZoneConfig(worldName, zoneId);
		
		//生成data/bilin/opconf/skip_zoneId.conf,用于跳过监控
		DeployCommandUtil.CreateSkipFile(worldName, zoneId);
		
		InitZoneData(worldName, zoneId, arenaRobotCount);
		
		// 先注释掉
		//InitMonitorAccount(worldName, zoneId);
		LogUtil.printLog("InitMonitorAccount|was|disable|now|please|run|command|by|hand|");
		
		//删除跳过监控的占位文件
		DeployCommandUtil.RemoveSkipFile(worldName, zoneId);
	}
	
	private static void InitZoneActivityData(String worldName, int zoneId){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("InitZoneActivityData|worldName|" + worldName + "|zoneId|" + zoneId + "|starting");
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String linuxUserName = deployProp.getProperty("linuxUserName").trim();
		String linuxUserPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		// 2017-07-25 linyaoheng 先注释
//		String javaBinPath = "/data/bilin/apps/jdk/bin/java";
//		String appPath = "/data/bilin/apps/" + zoneId + "/javaserver_gecaoshoulie_server";
//		
//		LinuxRemoteCommandUtil.runCmd(deployConfig.server_public_ip, 22, linuxUserName, linuxUserPwd, 
//				javaBinPath + " -Djavaserver.home=" +  appPath
//				+ " -Djavaserver.zoneid=" + zoneId
//				+ " -cp \"" + appPath + "/WEB-INF/classes:" + appPath + "/WEB-INF/lib/*\""
//				+ " net.bilinkeji.gecaoshoulie.mgameprotorpc.logics.activity.util.ServerStartUpInitWorker " + zoneId);
		
		LogUtil.printLog("InitZoneActivityData|worldName|" + worldName + "|zoneId|" + zoneId 
				+ "|usedtime|" + (System.currentTimeMillis() - startTime) + "ms");
	}
	
	private static void InitMonitorAccount(String worldName, int zoneId){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("InitMonitorAccount|worldName|" + worldName + "|zoneId|" + zoneId + "|starting");
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String linuxUserName = deployProp.getProperty("linuxUserName").trim();
		String linuxUserPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String javaBinPath = "/data/bilin/apps/jdk/bin/java";
		String appPath = "/data/bilin/apps/" + zoneId + "/javaserver_gecaoshoulie_server";
		
//		LinuxRemoteCommandUtil.runCmd(deployConfig.server_public_ip, 22, linuxUserName, linuxUserPwd, 
//				javaBinPath + " -Djavaserver.home=" +  appPath
//				+ " -Djavaserver.zoneid=" + zoneId
//				+ " -cp \"" + appPath + "/WEB-INF/classes:" + appPath + "/WEB-INF/lib/*\""
//				+ " net.bilinkeji.gecaoshoulie.mgameprotorpc.MonitorCenter.InitMonitorPlayer " 
//				+ deployConfig.server_inner_ip + " " + zoneId);
		
		LogUtil.printLog("InitMonitorAccount|worldName|" + worldName + "|zoneId|" + zoneId 
				+ "|usedtime|" + (System.currentTimeMillis() - startTime) + "ms");
	}
	
	/**
	 * 初始化玩家服机器人信息
	 */
	public static void InitZoneData(String worldName, int zoneId, int arenaRobotCount){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("InitZoneData|worldName|" + worldName + "|zoneId|" + zoneId + "|starting");
		
		//读取配置文件
		Properties prop = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		//1.	读取版本服务器下相应世界的到本地
		BilinGameWorldConfig.downloadTserverCSV(worldName, prop);
		
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String linuxUserName = prop.getProperty("linuxUserName").trim();
		String linuxUserPwd = prop.getProperty("linuxUserPwd").trim();
		
		String alterSql = SQLGen4WorldUtil.getAlterStatement(zoneId);
		DeployCommandUtil.ExecSQLStatement(worldName, 
				BilinGameWorldConfig.getDBHostByZoneId(worldName, zoneId),
				BilinGameWorldConfig.getDBPortByZoneId(worldName, zoneId),
				BilinGameWorldConfig.getDBNameByZoneId(zoneId), alterSql);
		
		LinuxRemoteCommandUtil.runCmd(deployConfig.server_public_ip, 22, linuxUserName, linuxUserPwd, "export arenaRobotCount="
				+ arenaRobotCount + ";sh " +BilinGameWorldConfig.getRemoteZoneGameServerRoot(zoneId) + "/bin/bl_reinit.sh");
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("InitZoneData|worldName|" + worldName + "|zoneId|" + zoneId + "|usedtime|" + (endTime - startTime) + "ms");
		
	}

}
