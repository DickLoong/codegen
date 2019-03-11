package com.lizongbo.codegentool.world;

import java.io.File;
import java.util.List;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.world.porter.WorldServerPorter;

/**
 * 有关世界的文件操作
 * @author linyaoheng
 *
 */
public class BilinGameWorldFileUtil {

	/**
	 * 生成ps_monitor.conf
	 */
	public static void genPsMonitorConf(String worldName){
		
		String monitorNewDir = "/tmp/" + worldName + "/gecaoshoulie_monitor";
		new File(monitorNewDir).mkdirs();
		
		LogUtil.printLog("gen ps_monitor.conf starting");
		
		StringBuilder psMonitorConf = new StringBuilder();
		
		//生成ps_monitor.conf文件
		//40001:::10.0.0.16:::game_server:::/data/bilin/apps/40001/javaserver_gecaoshoulie_server/
		List<ServerDeployConfig> gameServerHosts = BilinGameWorldConfig.getGameServerHosts(worldName);
		for (ServerDeployConfig item : gameServerHosts){
			if (item.idShow > 0){
				psMonitorConf.append(item.zone_id
						+ ":::" + item.server_inner_ip 
						+ ":::" + I18NUtil.SERVER_TYP.GAME_SERVER 
						+ ":::" + BilinGameWorldConfig.getRemoteZoneGameServerRoot(item.zone_id) + "/" 
						+ "\n");
				
				psMonitorConf.append(item.zone_id
						+ ":::" + item.server_inner_ip 
						+ ":::" + I18NUtil.SERVER_TYP.MAP_SERVER 
						+ ":::" + BilinGameWorldConfig.getRemoteZoneMapServerServerRoot(item.zone_id) + "/" 
						+ "\n");
				
				psMonitorConf.append(item.zone_id
						+ ":::" + item.server_inner_ip 
						+ ":::" + I18NUtil.SERVER_TYP.FRAME_SYNC_SERVER1
						+ ":::" + BilinGameWorldConfig.getRemoteZoneFrameServerRoot(item.zone_id, 1) + "/" 
						+ "\n");
				
				int frameServerCount = BilinGameWorldConfig.getFrameServerCountByZoneId(worldName, item.zone_id);
				
				if (frameServerCount >= 2) {
					psMonitorConf.append(item.zone_id + ":::" + item.server_inner_ip + ":::"
							+ I18NUtil.SERVER_TYP.FRAME_SYNC_SERVER2 + ":::"
							+ BilinGameWorldConfig.getRemoteZoneFrameServerRoot(item.zone_id, 2) + "/" + "\n");
				}
				
				if (frameServerCount >= 3) {
					psMonitorConf.append(item.zone_id + ":::" + item.server_inner_ip + ":::"
							+ I18NUtil.SERVER_TYP.FRAME_SYNC_SERVER3 + ":::"
							+ BilinGameWorldConfig.getRemoteZoneFrameServerRoot(item.zone_id, 3) + "/" + "\n");
				}
				
				if (frameServerCount >= 4) {
					psMonitorConf.append(item.zone_id + ":::" + item.server_inner_ip + ":::"
							+ I18NUtil.SERVER_TYP.FRAME_SYNC_SERVER4 + ":::"
							+ BilinGameWorldConfig.getRemoteZoneFrameServerRoot(item.zone_id, 4) + "/" + "\n");
				}
				
				psMonitorConf.append(item.zone_id
						+ ":::" + item.server_inner_ip 
						+ ":::" + I18NUtil.SERVER_TYP.REDIS_DBCACHE 
						+ ":::" + BilinGameWorldConfig.getRemoteZoneRedisRoot(item.zone_id, item.redis1_port) + "/" 
						+ "\n");
				
				psMonitorConf.append(item.zone_id
						+ ":::" + item.server_inner_ip 
						+ ":::" + I18NUtil.SERVER_TYP.REDIS_COUNTER 
						+ ":::" + BilinGameWorldConfig.getRemoteZoneRedisRoot(item.zone_id, item.redis2_port) + "/" 
						+ "\n");
				
				psMonitorConf.append(item.zone_id
						+ ":::" + item.server_inner_ip 
						+ ":::" + I18NUtil.SERVER_TYP.REDIS_RANKLIST 
						+ ":::" + BilinGameWorldConfig.getRemoteZoneRedisRoot(item.zone_id, item.redis3_port) + "/" 
						+ "\n");
				
				psMonitorConf.append(item.zone_id
						+ ":::" + item.server_inner_ip 
						+ ":::" + I18NUtil.SERVER_TYP.REDIS_COMMON 
						+ ":::" + BilinGameWorldConfig.getRemoteZoneRedisRoot(item.zone_id, item.redis4_port) + "/" 
						+ "\n");
			}
		}
		
		List<ServerDeployConfig> commonServerHosts = BilinGameWorldConfig.getCommonServerHosts(worldName);
		for (ServerDeployConfig item : commonServerHosts){
			psMonitorConf.append(item.zone_id
					+ ":::" + item.server_inner_ip 
					+ ":::" + I18NUtil.SERVER_TYP.COMMON_SERVER 
					+ ":::" + BilinGameWorldConfig.getCommonServerRoot(item.zone_id) + "/" 
					+ "\n");
		}
		
		List<ServerDeployConfig> allWorldServers = BilinGameWorldConfig.getWorldServerHost(worldName);
		for (ServerDeployConfig ws : allWorldServers){
			WorldServerPorter worldServerPorter = new WorldServerPorter(worldName, ws);
			psMonitorConf.append(worldServerPorter.toPsMonitorConfString());
		}
		
		GameCSV2DB.writeFile(monitorNewDir + "/ps_monitor/ps_monitor.conf", psMonitorConf.toString());
		
		LogUtil.printLog("gen ps_monitor.conf completely");
	}
	
}
