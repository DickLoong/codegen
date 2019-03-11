package com.lizongbo.codegentool.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import com.lizongbo.codegentool.CodeGenConsts;
import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.SCPUtil;
import com.lizongbo.codegentool.csv2db.CSVUtil;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;

public class BilinGameWorldConfig {
	
	public static String bilinRoot = "/data/bilin";
	public static String releaseVersionRoot = "/data/bilin/release_version";
	public static String appsRoot = "/data/bilin/apps";
	public static String scriptRoot = "/data/bilin/script";
	public static String softwareRoot = "/data/bilin/software";
	public static String opConfRoot = "/data/bilin/opconf";
	
	private static String tserverGameZoneCSVPath = null;
	private static String tserverWarZoneCSVPath = null;
	
	/**
	 * 将Server按IP汇总到一个Map
	 */
	public static TreeMap<String, List<ServerDeployConfig>> getHostsMap(String worldName, String[] zoneArray)
	{
		TreeMap<String, List<ServerDeployConfig>> serverHostsMap = new TreeMap<>();
		
		for (String item : zoneArray){
			ServerDeployConfig tmpDeployConfig = BilinGameWorldConfig.getDeployConfig(worldName, Integer.valueOf(item));
			
			//数据校验
			BilinGameWorldConfig.validateZoneConfig(worldName, tmpDeployConfig);
			
			String tmpIP = tmpDeployConfig.server_public_ip;
			
			if (!serverHostsMap.containsKey(tmpIP)){
				serverHostsMap.put(tmpIP, new ArrayList<ServerDeployConfig>());
			}
			List<ServerDeployConfig> tmpList = serverHostsMap.get(tmpIP);
			tmpList.add(tmpDeployConfig);
		}
		
		return serverHostsMap;
	}
	
	/**
	 * 世界服务器的DB Host
	 */
	public static String getWorldServerDBHost(String worldName, int zoneId){
		String serverMySQLHost = getServerMySQLHost(worldName, zoneId);
		if (serverMySQLHost != null && serverMySQLHost.length() > 0){
			LogUtil.printLog("zoneId|" + zoneId + "|use|gamezone|sql_host|" + serverMySQLHost);
			return serverMySQLHost;
		}
		else{
			LogUtil.printLog("zoneId|" + zoneId + "|use|rootDBHost|sql_host|");
			return getRootDBHost(worldName);
		}
	}
	
	/**
	 * 世界服务器的DB Port
	 */
	public static String getWorldServerDBPort(String worldName, int zoneId){
		int serverMySQLPort = getServerMySQLPort(worldName, zoneId);
		if (serverMySQLPort > 0){
			LogUtil.printLog("zoneId|" + zoneId + "|use|gamezone|sql_port|" + serverMySQLPort);
			return "" + serverMySQLPort;
		}
		else{
			Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
			Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
			
			LogUtil.printLog("zoneId|" + zoneId + "|use|rootDBPort|sql_port|");
			return worldProp.getProperty("rootDBPort");
		}
	}
	
	/**
	 * ROOT DB Pub Host/公网IP
	 */
	public static String getRootDBPubHost(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		return worldProp.getProperty("rootDBPubHost");
	}
	
	/**
	 * ROOT DB Host
	 */
	public static String getRootDBHost(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		return worldProp.getProperty("rootDBHost");
	}
	
	/**
	 * ROOT DB Port
	 */
	public static String getRootDBPort(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		return worldProp.getProperty("rootDBPort");
	}
	
	/**
	 * ROOT DB 从库 Host
	 */
	public static String getRootDBSlaveHost(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String slaveHost = worldProp.getProperty("rootDBSlaveHost");
		if (slaveHost == null || slaveHost.length() == 0){
			return getRootDBHost(worldName);
		}
		
		return slaveHost;
	}
	
	/**
	 * 支付服务器的外网IP
	 */
	public static List<String> getPayServerHost(String worldName){
		//本地发布用的配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		//世界配置文件
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		//支付服务器1 IP
		String payServer1 = worldProp.getProperty("payServer1Host");
		
		//支付服务器2 IP
		String payServer2 = worldProp.getProperty("payServer2Host");
		
		List<String> res = new ArrayList<>();
		if (payServer1 != null && payServer1.length() > 0){
			LogUtil.printLog("has|payServer1|" + payServer1);
			res.add(payServer1);
		}
		
		if (payServer2 != null && payServer2.length() > 0){
			LogUtil.printLog("has|payServer2|" + payServer2);
			res.add(payServer2);
		}
		
		if (res.size() == 0){
			// 默认的IP
			String httpHost = deployProp.getProperty("releaseVersionHost");
			
			LogUtil.printLog("has|payServerDefault|" + httpHost);
			res.add(httpHost);
		}
		
		return res;
	}
	
	/**
	 * 支付服务器的端口
	 */
	public static int getPayServerPort(String worldName){
		return 9090;
	}
	
	/**
	 * 统计DB,报表DB的HOST
	 */
	public static String getStatDBHost(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		return worldProp.getProperty("statDBHost");
	}
	
	/**
	 * 统计DB的Port
	 */
	public static String getStatDBPort(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		return worldProp.getProperty("statDBPort");
	}
	
	/**
	 * 统计DB的数据库名
	 */
	public static String getStatReportDBName(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		return worldProp.getProperty("statReportDBName");
	}
	
	/**
	 * 返回帧同步服务器的数量
	 */
	public static int getFrameServerCountByZoneId(String worldName, int zoneId)
	{
		if (worldName == null || worldName.isEmpty())
		{
			LogUtil.printLogErr("no|worldName");
			System.exit(1);
		}
		
		int frameServerNum = 4;
		
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		if (deployConfig.zone_id == zoneId)
		{
			frameServerNum = Math.max(1, deployConfig.frameServerNum);
			LogUtil.printLog("getFrameServerCountByZoneId|zoneId|" + zoneId + "|has|config|from|tserver|num|" + frameServerNum);
		}
		else
		{
			LogUtil.printLog("getFrameServerCountByZoneId|zoneId|" + zoneId + "|no|config|use|default|num|" + frameServerNum);
		}
		
		return frameServerNum;
	}
	
	/**
	 * 运营DB的HOST
	 */
	public static String getOperateDBHost(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		return worldProp.getProperty("operateDBHost");
	}
	
	/**
	 * 读取指定的世界服务器信息,没有则返回null
	 */
	public static ServerDeployConfig getTheWorldServerConfig(String worldName, int zoneId){
		List<ServerDeployConfig> allWorldServers = getWorldServerHost(worldName);
		for (ServerDeployConfig item : allWorldServers){
			if (item.zone_id == zoneId){
				return item;
			}
		}
		
		return null;
	}
	
	/**
	 * 读取世界服务器信息
	 * 2017-09-23 linyaoheng 现在世界可以支持多个世界服务器了
	 */
	public static List<ServerDeployConfig> getWorldServerHost(String worldName){
		List<ServerDeployConfig> allWorldServers = new ArrayList<>();
		
		List<ServerDeployConfig> allZones = getAllDeployConfigs(worldName);
		for (ServerDeployConfig item : allZones){
			if (isWorldServer(item.zone_id)){
				item.game_server_port = item.zone_id;
				item.game_server_jmx_port = item.zone_id + 200;
				item.game_server_hessian_port = item.zone_id + 100;
				
				item.frame_sync_server1_port = 25500 + (item.zone_id % 1000);
				item.frame_sync_server1_jmx_port = 25600 + (item.zone_id % 1000);
				item.frame_sync_server1_hessian_port = 25700 + (item.zone_id % 1000);
				item.frame_sync_server2_port = 26500 + (item.zone_id % 1000);
				item.frame_sync_server2_jmx_port = 26600 + (item.zone_id % 1000);
				item.frame_sync_server2_hessian_port = 26700 + (item.zone_id % 1000);
				item.frame_sync_server3_port = 27500 + (item.zone_id % 1000);
				item.frame_sync_server3_jmx_port = 27600 + (item.zone_id % 1000);
				item.frame_sync_server3_hessian_port = 27700 + (item.zone_id % 1000);
				item.frame_sync_server4_port = 28500 + (item.zone_id % 1000);
				item.frame_sync_server4_jmx_port = 28600 + (item.zone_id % 1000);
				item.frame_sync_server4_hessian_port = 28700 + (item.zone_id % 1000);
				
				allWorldServers.add(item);
			}
		}
		
		return allWorldServers;
	}
	
	/**
	 * 读取公用服务器列表
	 */
	public static List<ServerDeployConfig> getCommonServerHosts(String worldName){
		List<ServerDeployConfig> commonServers = new ArrayList<>();
		
		List<ServerDeployConfig> allZones = getAllDeployConfigs(worldName);
		for (ServerDeployConfig item : allZones){
			if (isCommonServer(item.zone_id)){
				item.game_server_port = item.zone_id;
				item.game_server_jmx_port = item.zone_id + 200;
				commonServers.add(item);
			}
		}
		
		return commonServers;
	}
	
	/**
	 * 读取游戏服务器列表
	 */
	public static List<ServerDeployConfig> getGameServerHosts(String worldName){
		List<ServerDeployConfig> gameServers = new ArrayList<>();
		
		List<ServerDeployConfig> allZones = getAllDeployConfigs(worldName);
		for (ServerDeployConfig item : allZones){
			if (isGameServer(item.zone_id)){
				gameServers.add(item);
			}
		}
		
		return gameServers;
	}
	
	/**
	 * 读取运营服务器的外网IP
	 */
	public static String getOperateServerHost(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		return worldProp.getProperty("operateServerHost");
	}
	
	/**
	 * 读取统计服务器的外网IP
	 */
	public static String getStatServerHost(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		return worldProp.getProperty("statServerHost");
	}
	
	/**
	 * 读取统计服务器的外网IP
	 */
	public static String getVersionServerPublicIP(String worldName){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		return deployProp.getProperty("releaseVersionHost");
	}
	
	/**
	 * javalib4maperver的完整路径
	 */
	public static String getJavaLib4MapServerPath(){
		return softwareRoot + "/javalib4mapserver";
	}
	
	/**
	 * 游戏服发布需要的文件, 这些文件统一放在release_version目录下的
	 */
	public static String[] getNeedFiles()
	{
		return new String[]{
			"gecaoshoulie_game_server_pub.zip",
			"gecaoshoulie_map_server_pub.zip",
			"sqlfiles.zip",
			I18NUtil.getServercodegenJarName(),
		};
	}
	
	/**
	 * 是否世界服务器
	 */
	public static boolean isWorldServer(int zoneId){
		return zoneId >= 4001 && zoneId <= 4099;
	}
	
	/**
	 * 是否公共服务器
	 */
	public static boolean isCommonServer(int zoneId){
		return zoneId >= 3000 && zoneId < 4000;
	}
	
	/**
	 * 是否游戏服务器
	 */
	public static boolean isGameServer(int zoneId){
		return zoneId >= 40000 && zoneId < 50000;
	}
	
	/**
	 * 校验世界的配置
	 */
	public static void validateWorldConfig(String worldName){
		if (worldName == null || worldName.isEmpty()){
			LogUtil.printLog("validateWorldConfig|noworldName");
			System.exit(1);
		}
		
		if (!I18NUtil.worldExists(worldName)) {
			LogUtil.printLogErr("No such world");
			System.exit(1);
		}
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		validateGameEnvProp(worldName, deployProp);
		
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		validateWorldProp(worldProp);
		
		BilinGameWorldConfig.downloadTserverCSV(worldName, deployProp);
		
		int commonServerCount = 0; //公共服务器数量
		int worldServerCount = 0; //世界服务器数量
		int zoneServerCount = 0; //玩家服数量
		
		TreeMap<Integer, Integer> zoneIdCountMap = new TreeMap<>();
		
		List<ServerDeployConfig> allDeployConfigs = BilinGameWorldConfig.getAllDeployConfigs(worldName);
		for (ServerDeployConfig item : allDeployConfigs){
			if (isCommonServer(item.zone_id)){
				commonServerCount++;
			}
			
			if (isWorldServer(item.zone_id)){
				worldServerCount++;
			}
			
			if (isGameServer(item.zone_id)){
				zoneServerCount++;
			}
			
			Integer theKey = Integer.valueOf(item.zone_id);
			zoneIdCountMap.put(theKey, zoneIdCountMap.getOrDefault(theKey, 1));
		}
		
		LogUtil.printLog("validateWorldConfig|commonServerCount|" + commonServerCount);
		LogUtil.printLog("validateWorldConfig|worldServerCount|" + worldServerCount);
		LogUtil.printLog("validateWorldConfig|zoneServerCount|" + zoneServerCount);
		
		if (commonServerCount == 0 || zoneServerCount == 0){
			LogUtil.printLogErr("validateWorldConfig|TServerGameZone|No CommonServer Or ZoneServer");
			System.exit(1);
		}
		
		// gameZoneId是否有重复
		boolean hasDuplicateGameZoneId = false;
		for (Entry<Integer, Integer> entry : zoneIdCountMap.entrySet()){
			if (entry.getValue() > 1){
				hasDuplicateGameZoneId = true;
				
				LogUtil.printLogErr("validateWorldConfig|TServerGameZone|DuplicateZoneId|zoneId|" + entry.getKey() + "|count|" + entry.getValue());
			}
		}
		
		if (hasDuplicateGameZoneId){
			System.exit(1);
		}
		
		validateWarZoneCSV(worldName);
	}
	
	/**
	 * 校验TServer_WarZone配置表
	 */
	private static void validateWarZoneCSV(String worldName){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("validateWarZoneCSV|Start");
		
		File csvFile = new File(BilinGameWorldConfig.getTServerWarZoneCSVPath(worldName));
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		//异常的配置表文件
		if (colList.size() <= 4) {
			LogUtil.printLogErr("WarZoneCSV|colList|" + colList);
			System.exit(1);
		}

		Map<String, Integer> colIndexMap = new TreeMap<String, Integer>();
		for (int i = 0; i < colList.get(0).length; i++) {
			colIndexMap.put(colList.get(0)[i], i);
		}

		TreeMap<Integer, Integer> warZoneIdCountMap = new TreeMap<>();
		for (int i = 4; i < colList.size(); i++) {
			String[] colItem = colList.get(i);

			Integer theKey = Integer.parseInt(colItem[colIndexMap.get("zone_id")]);
			warZoneIdCountMap.put(theKey, warZoneIdCountMap.getOrDefault(theKey, 1));
		}
		
		// warZoneId是否有重复
		boolean hasDuplicateWarZoneId = false;
		for (Entry<Integer, Integer> entry : warZoneIdCountMap.entrySet()){
			if (entry.getValue() > 1){
				hasDuplicateWarZoneId = true;
				
				LogUtil.printLogErr("validateWarZoneCSV|TServerWarZone|DuplicateZoneId|zoneId|" + entry.getKey() + "|count|" + entry.getValue());
			}
		}
		
		if (hasDuplicateWarZoneId){
			System.exit(1);
		}
		
		//GameZone所需的warZoneId在WarZone是否存在
		List<ServerDeployConfig> allDeployConfigs = BilinGameWorldConfig.getAllDeployConfigs(worldName);
		for (ServerDeployConfig item : allDeployConfigs){
			if (isGameServer(item.zone_id)){
				if (!warZoneIdCountMap.containsKey(item.warzone_id)){
					LogUtil.printLogErr("validateWarZoneCSV|no|warZoneId|" +  item.warzone_id + "|of|zoneId|" + item.zone_id);
					System.exit(1);
				}
			}
		}
		
		LogUtil.printLog("validateWarZoneCSV|end|usedtime|" + (System.currentTimeMillis()-startTime));
	}

	/**
	 * 校验各个World.properties的配置
	 * @param worldProp
	 */
	private static void validateWorldProp(Properties worldProp) {
		String[] checkKeys = new String[]{
				"rootDBHost", "rootDBPort", "rootDBName",
				"operateServerHost", "operateServerPort", "operateDBHost", "operateDBName", "operateDBPort",
				"statServerHost", "statDBHost", "statBusinessDBPrefixName", "statRedisSnapshotDBPrefixName", "statDBPort", "statReportDBName",
				"mailto", "mailhost", "mailport", "mailuser", "mailpassword", "maildebug",
				"backupCenterHost", "backupCenterLoginUser",
				"releaseVersionHost", "releaseVersionUser",
				"sshPort", "worldName",
		};
		
		StringBuilder missingKeysStr = new StringBuilder();
		for (String item : checkKeys){
			if (!worldProp.containsKey(item)){
				missingKeysStr.append("miss " + item + "\n");
			}
		}
		
		if (missingKeysStr.length() > 0)
		{
			LogUtil.printLogErr("World.properties nees these keys:" + checkKeys);
			System.exit(1);
		}
	}

	/**
	 * 校验游戏服的配置
	 */
	public static void validateZoneConfig(String worldName, int zoneId){
		validateWorldConfig(worldName);
		
		if (zoneId == 0){
			LogUtil.printLog("请在环境变量指定zoneId");
			System.exit(1);
		}
		
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		validateZoneConfig(worldName, deployConfig);
	}
	
	public static void validateZoneConfig(String worldName, ServerDeployConfig deployConfig)
	{
		if (deployConfig.server_public_ip.isEmpty() || deployConfig.server_inner_ip.isEmpty()){
			LogUtil.printLog("zoneId=" + deployConfig.zone_id + "|没有指定IP");
			System.exit(1);
		}
	}
	
	/**
	 * 校验GameEnv内容是否正常
	 */
	private static void validateGameEnvProp(String worldName, Properties deployProp){
		String[] checkKeys = new String[]{
				"linuxUserName", "linuxUserPwd", 
				"mysqlPwd", "mysqlUser", "mysqlPort",
				"redisPwd", 
				"releaseVersionHost", "releaseVersionUser", "releaseVersionPwd",
				"sshPort",
		};
		
		StringBuilder missingKeysStr = new StringBuilder(); 
		for (String item : checkKeys){
			if (!deployProp.containsKey(item)){
				missingKeysStr.append("miss " + item + "\n");
			}
		}
		
		if (missingKeysStr.length() > 0){
			LogUtil.printLogErr("GameEnvConfig.YourWorld.properties\nworldName|" + worldName + "\n" + missingKeysStr);
			System.exit(1);
		}
	}
	
	/**
	 * 是否在Jenkins机器上存在世界的配置表,如果不存在,则认为这个这个世界没有版本服务器这些.
	 * 因为在I18N下面世界目录是可以随便创建的
	 */
	public static boolean ExistsGameEnvProp(String worldName){
		File overideProp = new File("/mgamedev/WorldPubConf/GameEnvConfig." + worldName + ".properties");
		return overideProp.exists();
	}
	
	/**
	 * 读取不同的游戏环境中的配置,如一些安全性配置,本地运行代码需要用到
	 */
	public static Properties getGameEvnProp(String gameEnvName){
		Properties prop = new Properties();
		
		InputStream in = null;
		File overideProp = new File("/mgamedev/WorldPubConf/GameEnvConfig." + gameEnvName + ".properties");
		if (overideProp.exists()){
			try {
				in = new FileInputStream(overideProp);
			} catch (FileNotFoundException e) {
				LogUtil.printLogErr("NotFound " + overideProp.getAbsolutePath());
				System.exit(1);
			}
		}
		else
		{
			LogUtil.printLogErr("NotFound " + overideProp.getAbsolutePath());
			System.exit(1);
		}
		
		try {
			prop.load(in);
		} catch (Throwable e) {
			LogUtil.printLogErr(e.getMessage());
			System.exit(1);
		}
		
		// 2017-09-10 linyaoheng 添加sshPort端口的处理,通过System.setProperty来传递,默认是22
		LinuxRemoteCommandUtil.SetSSHPort(prop.getProperty("sshPort"));
		
		return prop;
	}
	
	/**
	 * 读取不同的游戏环境中的配置,如一些安全性配置,本地运行代码需要用到
	 */
	public static Properties getClientBuildProp(String worldName){
		Properties prop = new Properties();
		
		InputStream in = null;
		File overideProp = new File("/mgamedev/WorldPubConf/ClientBuildConfig." + worldName + ".properties");
		if (overideProp.exists()){
			try {
				in = new FileInputStream(overideProp);
			} catch (FileNotFoundException e) {
				LogUtil.printLogErr("NotFound " + overideProp.getAbsolutePath());
				System.exit(1);
			}
		}
		else
		{
			LogUtil.printLogErr("NotFound " + overideProp.getAbsolutePath());
			System.exit(1);
		}
		
		try {
			prop.load(in);
		} catch (Throwable e) {
			LogUtil.printLogErr(e.getMessage());
			System.exit(1);
		}
		
		return prop;
	}
	
	/**
	 * 远程服务器的resin路径
	 */
	public static String getRemoteResinRoot(int httpPort){
		return appsRoot + "/resin_" + httpPort;
	}
	
	/**
	 * 世界服务器部署在服务器上的目录
	 * TODO 删除这个方法
	 */
	public static String getWorldServerRoot(int zoneId){
		return appsRoot + "/javaserver_world_server/" + zoneId;
	}
	
	/**
	 * 公共服务器部署在服务器上的目录
	 */
	public static String getCommonServerRoot(int zoneId){
		return appsRoot + "/javaserver_common_server/" + zoneId;
	}
	
	/**
	 * 游戏服务器部署在服务器上的目录
	 */
	public static String getRemoteZoneGameServerRoot(int zoneId){
		return getZoneAppRoot(zoneId) + "/javaserver_gecaoshoulie_server";
	}
	
	/**
	 * 场景服务器部署在服务器上的目录
	 */
	public static String getRemoteZoneMapServerServerRoot(int zoneId){
		return getZoneAppRoot(zoneId) + "/javaserver_gecaoshoulie_map_server";
	}
	
	/**
	 * 帧同步服务器部署在服务器上的目录
	 */
	public static String getRemoteZoneFrameServerRoot(int zoneId, int seq){
		return getZoneAppRoot(zoneId) + "/javaserver_gecaoshoulie_framesync_server" + seq;
	}
	
	/**
	 * Redis服务器部署在服务器上的目录
	 */
	public static String getRemoteWorldServerRedisRoot(int zoneId, int redisPort){
		return getWorldServerRoot(zoneId) + "/redis_" + redisPort;
	}

	/**
	 * Redis服务器部署在服务器上的目录
	 */
	public static String getRemoteZoneRedisRoot(int zoneId, int redisPort){
		return getZoneAppRoot(zoneId) + "/redis_" + redisPort;
	}
	
	/**
	 * 玩家服的统一根目录
	 */
	public static String getZoneAppRoot(int zoneId){
		return appsRoot + "/" + zoneId;
	}
	
	/**
	 * 读取版本服务器上的对应世界的目录
	 * @param worldName
	 */
	public static String getWorldReleaseVersionRoot(String worldName){
		return releaseVersionRoot + "/" + worldName;
	}
	
	/**
	 * 读取版本服务器上的对应世界的某一版本的目录
	 */
	public static String getWorldReleaseTagRoot(String worldName, String tag){
		return releaseVersionRoot + "/" + worldName + "/" + tag;
	}
	
	/**
	 * 读取服务器上的对应世界的临时目录
	 * @param worldName
	 */
	public static String getWorldTmpRoot(String worldName){
		return bilinRoot + "/" + worldName + "/tmp";
	}
	
	/**
	 * 删除本地存储的世界Properties(World.properties)
	 */
	public static void removeLocalWorldProperties(String worldName){
		String savePath = "/tmp/" + worldName;
		
		String gameZoneSavePath = savePath + "/World.properties";
		File worldPropFile = new File(gameZoneSavePath);
		if (worldPropFile.exists()){
			worldPropFile.delete();
		}
	}
	
	/**
	 * 读取版本服务器下相应世界的World.properties到本地,并读取
	 */
	public static Properties downloadOrReadWorldProperties(String worldName, Properties deployProp){
		String savePath = "/tmp/" + worldName;
		new File(savePath).mkdirs();
		
		String gameZoneSavePath = savePath + "/World.properties";

		//没有本地文件才下载
		if (!new File(gameZoneSavePath).exists()){
			SCPUtil.doSCPFrom(deployProp.getProperty("releaseVersionUser"), 
					deployProp.getProperty("releaseVersionPwd"), 
					deployProp.getProperty("releaseVersionHost"), 
					BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/World.properties", 
					gameZoneSavePath);
		}
		
		Properties prop = new Properties();
		InputStream in = null;
		File overideProp = new File(gameZoneSavePath);
		if (overideProp.exists()){
			try {
				in = new FileInputStream(overideProp);
			} catch (FileNotFoundException e) {
				LogUtil.printLogErr("NotFound " + overideProp.getAbsolutePath());
				System.exit(1);
			}
		}
		else
		{
			LogUtil.printLogErr("NotFound " + overideProp.getAbsolutePath());
			System.exit(1);
		}
		
		try {
			prop.load(in);
		} catch (Throwable e) {
			LogUtil.printLogErr(e.getMessage());
			System.exit(1);
		}
		
		return prop;
		
	}
	
	/**
	 * 读取版本服务器下相应世界的TServer_Warzone.CSV到本地,同时设置tserverCSVPath
	 * 读取版本服务器下相应世界的TServer_Gamezone.CSV到本地,同时设置tserverCSVPath
	 * @param worldProp
	 */
	public static void downloadTserverCSV(String worldName, Properties worldProp){
		String savePath = "/tmp/" + worldName;
		new File(savePath).mkdirs();
		
		String gameZoneSavePath = savePath + "/TServer_Gamezone.csv";
		
		SCPUtil.doSCPFrom(worldProp.getProperty("releaseVersionUser"), 
				worldProp.getProperty("releaseVersionPwd"), 
				worldProp.getProperty("releaseVersionHost"), 
				BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/TServer_Gamezone.csv", 
				gameZoneSavePath);
		
		tserverGameZoneCSVPath = gameZoneSavePath;
		LogUtil.printLog("tserverCSVPath|" + tserverGameZoneCSVPath);
		
		String warZoneSavePath = savePath + "/TServer_Warzone.csv";
		
		SCPUtil.doSCPFrom(worldProp.getProperty("releaseVersionUser"), 
				worldProp.getProperty("releaseVersionPwd"), 
				worldProp.getProperty("releaseVersionHost"), 
				BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/TServer_Warzone.csv", 
				warZoneSavePath);
		
		tserverWarZoneCSVPath = warZoneSavePath;
		LogUtil.printLog("tserverCSVPath|" + tserverWarZoneCSVPath);
	}
	
	/**
	 * 读取世界用的TServer_Warzone.csv文件
	 * @param worldName
	 */
	public static String getTServerWarZoneCSVPath(String worldName){
		if (new File(tserverWarZoneCSVPath).exists()){
			return tserverWarZoneCSVPath;
		}
		else{
			return I18NUtil.worldRootDir + "/" + worldName
					+ "/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_WarZone(游戏服务区).csv";
		}
	}
	
	/**
	 * 根据zoneId来取战区Id
	 */
	public static int getWarZoneIdByGameZoneId(String worldName, int zoneId){
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		return deployConfig.warzone_id;
	}
	
	/**
	 * 读取TServer_GameZone的server_mysql_host
	 */
	private static String getServerMySQLHost(String worldName, int zoneId) {
		long startTime = System.currentTimeMillis();
		CodeGenConsts.switchPlat();
		
		ServerDeployConfig tmpDeployConfig = getDeployConfig(worldName, zoneId);
		if (tmpDeployConfig.zone_id == 0){
			//没有找到相应的信息
			LogUtil.printLogErr("tserver|no|zoneId|found|for|" + zoneId);
			System.exit(1);
		}

		File csvFile = new File(BilinGameWorldConfig.getTServerGameZoneCSVPath(worldName));
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		Map<String, Integer> colIndexMap = new TreeMap<String, Integer>();
		for (int i = 0; i < colList.get(0).length; i++) {
			colIndexMap.put(colList.get(0)[i], i);
		}
		
		String serverMySQLHost = null;

		for (int i = 4; i < colList.size(); i++) {
			String[] colItem = colList.get(i);

			int configZoneId = StringUtil.toInt(colItem[colIndexMap.get("zone_id")]);
			if (configZoneId == zoneId && colIndexMap.containsKey("server_mysql_host")){
				serverMySQLHost = colItem[colIndexMap.get("server_mysql_host")];
				break;
			}
		}

		long endTime = System.currentTimeMillis();
		LogUtil.printLog("getServerMySQLHost use time:" + (endTime - startTime));

		return serverMySQLHost;
	}
	
	/**
	 * 读取TServer_GameZone的server_mysql_port
	 */
	private static int getServerMySQLPort(String worldName, int zoneId) {
		long startTime = System.currentTimeMillis();
		CodeGenConsts.switchPlat();
		
		ServerDeployConfig tmpDeployConfig = getDeployConfig(worldName, zoneId);
		if (tmpDeployConfig.zone_id == 0){
			//没有找到相应的信息
			LogUtil.printLogErr("tserver|no|zoneId|found|for|" + zoneId);
			System.exit(1);
		}

		File csvFile = new File(BilinGameWorldConfig.getTServerGameZoneCSVPath(worldName));
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		Map<String, Integer> colIndexMap = new TreeMap<String, Integer>();
		for (int i = 0; i < colList.get(0).length; i++) {
			colIndexMap.put(colList.get(0)[i], i);
		}
		
		int serverMySQLPort = -3306;

		for (int i = 4; i < colList.size(); i++) {
			String[] colItem = colList.get(i);

			int configZoneId = StringUtil.toInt(colItem[colIndexMap.get("zone_id")]);
			if (configZoneId == zoneId && colIndexMap.containsKey("server_mysql_port")){
				serverMySQLPort = StringUtil.toInt(colItem[colIndexMap.get("server_mysql_port")]);
				break;
			}
		}

		long endTime = System.currentTimeMillis();
		LogUtil.printLog("getServerMySQLPort use time:" + (endTime - startTime));

		return serverMySQLPort;
	}
	
	/**
	 * 读取玩家服所使用的DB HOST
	 */
	public static String getDBHostByZoneId(String worldName, int zoneId){
		String serverMySQLHost = getServerMySQLHost(worldName, zoneId);
		if (serverMySQLHost != null && serverMySQLHost.length() > 0){
			LogUtil.printLog("zoneId|" + zoneId + "|use|gamezone|sql_host|" + serverMySQLHost);
			return serverMySQLHost;
		}
		else{
			LogUtil.printLog("zoneId|" + zoneId + "|use|warzone|sql_host|");
			return getDbInfoByZoneId(worldName, zoneId, "mysql_host");
		}
	}
	
	/**
	 * 读取玩家服所使用的DB Port
	 */
	public static String getDBPortByZoneId(String worldName, int zoneId){
		int serverMySQLPort = getServerMySQLPort(worldName, zoneId);
		if (serverMySQLPort > 0){
			LogUtil.printLog("zoneId|" + zoneId + "|use|gamezone|sql_port|" + serverMySQLPort);
			return "" + serverMySQLPort;
		}
		else{
			LogUtil.printLog("zoneId|" + zoneId + "|use|warzone|sql_port|");
			return getDbInfoByZoneId(worldName, zoneId, "mysql_port");
		}
	}
	
	private static String getDbInfoByZoneId(String worldName, int zoneId, String colName){
		int testWarZoneId = getWarZoneIdByGameZoneId(worldName, zoneId);
		
		CodeGenConsts.switchPlat();

		File csvFile = new File(BilinGameWorldConfig.getTServerWarZoneCSVPath(worldName));
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		if (colList.size() <= 4) {
			return "";
		}

		Map<String, Integer> colIndexMap = new TreeMap<String, Integer>();
		for (int i = 0; i < colList.get(0).length; i++) {
			colIndexMap.put(colList.get(0)[i], i);
		}

		for (int i = 4; i < colList.size(); i++) {
			String[] colItem = colList.get(i);

			int configWarZoneId = Integer.parseInt(colItem[colIndexMap.get("zone_id")]);
			if (configWarZoneId == testWarZoneId) {
				return colItem[colIndexMap.get(colName)];
			}
		}
		
		return "";
	}
	
	/**
	 * 返回玩家服所使用的DB NAME
	 * @param zoneId
	 * @return
	 */
	public static String getDBNameByZoneId(int zoneId){
		if (zoneId == 0){
			return "mgamedb_gecaoshoulie";
		}
		
		return "mgamedb_gecaoshoulie_maindb_zone" + zoneId;
	}
	
	/**
	 * 读取世界用的TServer_Gamezone.csv文件
	 * @param worldName
	 */
	public static String getTServerGameZoneCSVPath(String worldName){
		if (new File(tserverGameZoneCSVPath).exists()){
			return tserverGameZoneCSVPath;
		}
		else{
			return I18NUtil.worldRootDir + "/" + worldName
					+ "/gecaoshoulie_configs/csvfiles/Public/World/TServer(分区分服配置)_GameZone(游戏服务区).csv";
		}
	}
	
	/**
	 * 所有服务器配置
	 */
	public static List<ServerDeployConfig> getAllDeployConfigs(String worldName) {
		List<ServerDeployConfig> deployConfigs = new ArrayList<ServerDeployConfig>();

		long startTime = System.currentTimeMillis();
		CodeGenConsts.switchPlat();

		File csvFile = new File(BilinGameWorldConfig.getTServerGameZoneCSVPath(worldName));
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		if (colList.size() <= 4) {
			return deployConfigs;
		}

		Map<String, Integer> colIndexMap = new TreeMap<String, Integer>();
		for (int i = 0; i < colList.get(0).length; i++) {
			colIndexMap.put(colList.get(0)[i], i);
		}

		for (int i = 4; i < colList.size(); i++) {
			String[] colItem = colList.get(i);

			int configZoneId = StringUtil.toInt(colItem[colIndexMap.get("zone_id")]);

			ServerDeployConfig tmpDeployConfig = getDeployConfig(worldName, configZoneId);
			deployConfigs.add(tmpDeployConfig);
		}

		long endTime = System.currentTimeMillis();
		LogUtil.printLog("getAllDeployConfigs use time:" + (endTime - startTime) + "ms");

		return deployConfigs;
	}

	public static ServerDeployConfig getDeployConfig(String worldName, int zoneId) {
		ServerDeployConfig deployConfig = new ServerDeployConfig();

		CodeGenConsts.switchPlat();

		File csvFile = new File(BilinGameWorldConfig.getTServerGameZoneCSVPath(worldName));
		List<String[]> colList = CSVUtil.getDataFromCSV2(csvFile.getAbsolutePath());

		if (colList.size() <= 4) {
			return deployConfig;
		}

		Map<String, Integer> colIndexMap = new TreeMap<String, Integer>();
		for (int i = 0; i < colList.get(0).length; i++) {
			colIndexMap.put(colList.get(0)[i], i);
		}

		for (int i = 4; i < colList.size(); i++) {
			String[] colItem = colList.get(i);

			int configZoneId = Integer.parseInt(colItem[colIndexMap.get("zone_id")]);
			if (configZoneId == zoneId) {
				deployConfig.zone_id = configZoneId;
				deployConfig.game_server_memory = Integer.parseInt(colItem[colIndexMap.get("game_server_memory")]);
				deployConfig.frame_sync_server_memory = Integer.parseInt(colItem[colIndexMap.get("frame_sync_server_memory")]);
				deployConfig.scene_map_server_memory = Integer.parseInt(colItem[colIndexMap.get("scene_map_server_memory")]);
				deployConfig.redis_memory = Integer.parseInt(colItem[colIndexMap.get("redis_memory")]);
				deployConfig.server_inner_ip = colItem[colIndexMap.get("server_inner_ip")];
				deployConfig.server_public_ip = colItem[colIndexMap.get("server_public_ip")];
				deployConfig.game_server_port = get_game_server_port(configZoneId);
				deployConfig.game_server_jmx_port = get_game_server_jmx_port(configZoneId);
				deployConfig.game_server_hessian_port = get_game_server_hessian_port(configZoneId);
				deployConfig.frame_sync_server1_port = get_frame_sync_server1_port(configZoneId);
				deployConfig.frame_sync_server1_jmx_port = get_frame_sync_server1_jmx_port(configZoneId);
				deployConfig.frame_sync_server1_hessian_port = get_frame_sync_server1_hessian_port(configZoneId);
				deployConfig.frame_sync_server2_port = get_frame_sync_server2_port(configZoneId);
				deployConfig.frame_sync_server2_jmx_port = get_frame_sync_server2_jmx_port(configZoneId);
				deployConfig.frame_sync_server2_hessian_port = get_frame_sync_server2_hessian_port(configZoneId);
				deployConfig.frame_sync_server3_port = get_frame_sync_server3_port(configZoneId);
				deployConfig.frame_sync_server3_jmx_port = get_frame_sync_server3_jmx_port(configZoneId);
				deployConfig.frame_sync_server3_hessian_port = get_frame_sync_server3_hessian_port(configZoneId);
				deployConfig.frame_sync_server4_port = get_frame_sync_server4_port(configZoneId);
				deployConfig.frame_sync_server4_jmx_port = get_frame_sync_server4_jmx_port(configZoneId);
				deployConfig.frame_sync_server4_hessian_port = get_frame_sync_server4_hessian_port(configZoneId);
				deployConfig.scene_map_server_port = get_scene_map_server_port(configZoneId);
				deployConfig.secne_map_server_jmx_port = get_secne_map_server_jmx_port(configZoneId);
				deployConfig.secne_map_server_hessian_port = get_secne_map_server_hessian_port(configZoneId);
				
				if (isWorldServer(configZoneId)){
					deployConfig.redis1_port = get_world_server_redis1_port(configZoneId);
					deployConfig.redis2_port = get_world_server_redis2_port(configZoneId);
					deployConfig.redis3_port = get_world_server_redis3_port(configZoneId);
					deployConfig.redis4_port = get_world_server_redis4_port(configZoneId);
				}
				else{
					deployConfig.redis1_port = get_redis1_port(configZoneId);
					deployConfig.redis2_port = get_redis2_port(configZoneId);
					deployConfig.redis3_port = get_redis3_port(configZoneId);
					deployConfig.redis4_port = get_redis4_port(configZoneId);
				}

				deployConfig.idShow = StringUtil.toInt(colItem[colIndexMap.get("zone_id_show")]);
				
				deployConfig.warzone_id = StringUtil.toInt(colItem[colIndexMap.get("warzone_id")]);
				
				deployConfig.frameServerNum = StringUtil.toInt(colItem[colIndexMap.get("frame_sync_server_num")]);

				break;
			}
		}

		return deployConfig;
	}
	
	private static int get_game_server_port(int zoneId){
		return 40000 + (zoneId % 10000);
	}
	
	private static int get_game_server_jmx_port(int zoneId){
		return 50000+(zoneId % 10000);
	}	
	
	private static int get_game_server_hessian_port(int zoneId){
		return 10000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server1_port(int zoneId){
		return 35000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server1_jmx_port	(int zoneId){
		return 51000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server1_hessian_port	(int zoneId){
		return 11000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server2_port	(int zoneId){
		return 36000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server2_jmx_port	(int zoneId){
		return 52000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server2_hessian_port	(int zoneId){
		return 12000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server3_port	(int zoneId){
		return 37000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server3_jmx_port	(int zoneId){
		return 53000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server3_hessian_port	(int zoneId){
		return 13000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server4_port	(int zoneId){
		return 38000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server4_jmx_port	(int zoneId){
		return 54000+(zoneId % 10000);
	}
	
	private static int get_frame_sync_server4_hessian_port	(int zoneId){
		return 14000+(zoneId % 10000);
	}
	
	private static int get_scene_map_server_port	(int zoneId){
		return 39000+(zoneId % 10000);
	}
	
	private static int get_secne_map_server_jmx_port	(int zoneId){
		return 55000+(zoneId % 10000);
	}
	
	private static int get_secne_map_server_hessian_port(int zoneId){
		return 15000+(zoneId % 10000);
	}
	
	private static int get_redis1_port(int zoneId){
		return 44000+(zoneId % 10000);
	}
	
	private static int get_redis2_port	(int zoneId){
		return 45000+(zoneId % 10000);
	}
	
	private static int get_redis3_port	(int zoneId){
		return 46000+(zoneId % 10000);
	}
	
	private static int get_redis4_port(int zoneId){
		return 47000+(zoneId % 10000);
	}
	
	private static int get_world_server_redis1_port(int zoneId){
		return 4400+(zoneId % 1000);
	}
	
	private static int get_world_server_redis2_port	(int zoneId){
		return 4500+(zoneId % 1000);
	}
	
	private static int get_world_server_redis3_port	(int zoneId){
		return 4600+(zoneId % 1000);
	}
	
	private static int get_world_server_redis4_port(int zoneId){
		return 4700+(zoneId % 1000);
	}

}
