package com.lizongbo.codegentool.csv2db;

import java.io.File;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;
import com.lizongbo.codegentool.world.DeployCommandUtil;
import com.lizongbo.codegentool.world.FrameSyncServerGen4WorldUtil;
import com.lizongbo.codegentool.world.ScpCommandUtil;

/**
 * 生成所有的模板文件,复制所有的模板文件到server
 * @author linyaoheng
 *
 */
public class GenAndCopyServerConfUtil {
	
	public static void GenAndCopyServerConfig(String worldName, int zoneId){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("生成模板文件|worldName|" + worldName + "|zoneId|" + zoneId + "|开始执行");
		
		//读取配置文件
		ServerDeployConfig zoneConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		FrameSyncServerGen4WorldUtil.genWorldFrameSyncServer(worldName, zoneId);
		GameServerGen4WorldUtil.genWorldGameServer(worldName, zoneId);
		MapServerGen4WorldUtil.genWorldMapServer(worldName, zoneId);
		RedisServerGen4WorldUtil.genWorldRedisServer(worldName, zoneId);
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("生成模板文件|worldName|" + worldName + "|zoneId|" + zoneId + "|耗时|" + (endTime - startTime) + "ms");
		
		//压缩目录
		startTime = System.currentTimeMillis();
		LogUtil.printLog("压缩模板文件|worldName|" + worldName + "|zoneId|" + zoneId + "|开始执行");
		
		String serverCfgPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/serverconfs/" + zoneId;
		String serverCfgZipFile = I18NUtil.worldRootDir + "/" + worldName + "/forServer/serverconfs/" + worldName + "_" + zoneId + ".zip";
		ServerContainerGenTool.zipDir(serverCfgPath, serverCfgZipFile, ".");
		
		endTime = System.currentTimeMillis();
		LogUtil.printLog("压缩模板文件|worldName|" + worldName + "|zoneId|" + zoneId + "|耗时|" + (endTime - startTime) + "ms");
		
		//复制到远程
		startTime = System.currentTimeMillis();
		LogUtil.printLog("复制模板文件|worldName|" + worldName + "|zoneId|" + zoneId + "|开始执行");
		
		//远程登录信息
		ScpCommandUtil.scpToGameServer(worldName, zoneId, serverCfgZipFile, 
				BilinGameWorldConfig.appsRoot + "/" + new File(serverCfgZipFile).getName());
		DeployCommandUtil.UnzipGameServerFile(worldName, zoneId, 
				BilinGameWorldConfig.appsRoot + "/" + new File(serverCfgZipFile).getName(), 
				BilinGameWorldConfig.getZoneAppRoot(zoneId));
		
		//添加执行权限
		String[] binPaths = new String[]{
			"javaserver_gecaoshoulie_framesync_server1",
			"javaserver_gecaoshoulie_framesync_server2",
			"javaserver_gecaoshoulie_framesync_server3",
			"javaserver_gecaoshoulie_framesync_server4",
			
			"javaserver_gecaoshoulie_map_server",
			"javaserver_gecaoshoulie_server",
			
			"redis_" + zoneConfig.redis1_port,
			"redis_" + zoneConfig.redis2_port,
			"redis_" + zoneConfig.redis3_port,
			"redis_" + zoneConfig.redis4_port,
		};
		
		for (String item : binPaths){
			DeployCommandUtil.zoneChmodAddX(worldName, zoneConfig.zone_id, 
					BilinGameWorldConfig.getZoneAppRoot(zoneId) + "/" + item + "/bin/*.sh");
		}
		
		endTime = System.currentTimeMillis();
		LogUtil.printLog("复制模板文件|worldName|" + worldName + "|zoneId|" + zoneId + "|耗时|" + (endTime - startTime) + "ms");
	}

}
