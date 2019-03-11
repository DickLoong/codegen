package com.lizongbo.codegentool.world;

import java.util.Properties;

import com.lizongbo.codegentool.DeployUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.GenAndCopyServerConfUtil;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 开服操作
 * 包括以下操作 

1.	读取版本服务器下相应世界的TServer到本地
3.	装Redis
4.	生成各Server目录结构,启动脚本
5.	生成hosts(hostfiles目录)
6.	生成数据库连接配置文件(poolcfgfiles/dbpoolcfg)
7.	Redis连接配置文件(poolcfgfiles/redispoolcfg)
8.	更新所有GS机器配置文件(hosts/dbpoolcfg/redispoolcfg)及Hosts,还有敏感词文件(keywords/keywords.txt)

 * @author linyaoheng
 *
 */
public class ServerOpenOP {

	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 0);
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		openWorld(worldName, zoneId);
	}
	
	/**
	 * 开服
	 */
	private static void openWorld(String worldName, int zoneId){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("开服操作|worldName|" + worldName + "|zoneId|" + zoneId);
		
		DeployUtil.ShowEnv();
		
		BilinGameWorldConfig.validateZoneConfig(worldName, zoneId);
		
		//读取配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		//1.	读取版本服务器下相应世界的TServer到本地
		BilinGameWorldConfig.downloadTserverCSV(worldName, deployProp);
		
		//2.	装Redis
		LogUtil.printLog("install Redis starting");
		DeployCommandUtil.InstallGameServerRedis(worldName, zoneId);
		LogUtil.printLog("install Redis completely");
		
		//2017-08-24 linyaoheng 开服的世界表发布,改为手动
		//DeployWorld.deployWorld(worldName);
		
		//3.	生成各Server目录结构,启动脚本
		//生成所有的模板文件,复制所有的模板文件到server
		LogUtil.printLog("GenAndCopyServerConfig starting");
		GenAndCopyServerConfUtil.GenAndCopyServerConfig(worldName, zoneId);
		LogUtil.printLog("GenAndCopyServerConfig completely");
		
		//9.	创建游戏服数据库
		LogUtil.printLog("create game database starting");
		DeployCommandUtil.CreateGameDatabase(worldName, zoneId);
		LogUtil.printLog("create game database  completely");
		
		//启动游戏服Redis
		DeployCommandUtil.StartGameRedisServer(worldName, zoneId);
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("ServerOpenOP|worldName|" + worldName + "|zoneId|" + zoneId + "|usedTime|" + (endTime - startTime) + "ms");
		LogUtil.printLog("ServerOpenOP completely!");
	}
	
}
