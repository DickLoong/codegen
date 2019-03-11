package com.lizongbo.codegentool.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.GenAndCopyServerConfUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;

/**
 * 发布游戏服务器的启动脚本
 * @author linyaoheng
 */
public class UpdateAllGameServerStartupScript {

	public static void main(String[] args) {
		// 部署到相应的服务器
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		long timeStart = System.currentTimeMillis();
		
		//读取配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		//1.	读取版本服务器下相应世界的TServer到本地
		LogUtil.printLog("downloadTserverCSV starting");
		BilinGameWorldConfig.downloadTserverCSV(worldName, deployProp);
		LogUtil.printLog("downloadTserverCSV completely");
		
		TreeMap<String, List<ServerDeployConfig>> serverHostsMap = new TreeMap<>(); //同个物理机的用同一个线程来跑
		
		List<ServerDeployConfig> gameServerHosts = BilinGameWorldConfig.getGameServerHosts(worldName);
		for (ServerDeployConfig item : gameServerHosts){
			if (item.idShow <= 0){
				continue;
			}
			
			//数据校验
			BilinGameWorldConfig.validateZoneConfig(worldName, item);
			
			String tmpIP = item.server_public_ip;

			if (!serverHostsMap.containsKey(tmpIP)){
				serverHostsMap.put(tmpIP, new ArrayList<ServerDeployConfig>());
			}
			
			List<ServerDeployConfig> tmpList = serverHostsMap.get(tmpIP);
			tmpList.add(item);
		}
		
		for (Entry<String, List<ServerDeployConfig>> item : serverHostsMap.entrySet()){
			List<ServerDeployConfig> tmpList = item.getValue();
			Thread t = new Thread(new Runnable() {
				public void run() {
					for (ServerDeployConfig tmpConfig : tmpList){
						int zoneId = tmpConfig.zone_id;
						
						long timeGenAndCopyServerConfig = System.currentTimeMillis();
						LogUtil.printLog("GenAndCopyServerConfig start");
						GenAndCopyServerConfUtil.GenAndCopyServerConfig(worldName, zoneId);
						LogUtil.printLog("GenAndCopyServerConfig completely|use|time|" + (System.currentTimeMillis() - timeGenAndCopyServerConfig));
					}
				}
			});
			
			try {
				Thread.sleep(800);
				t.start();
			} catch (InterruptedException e) {
				e.printStackTrace();
				
				//如果有出错则直接退出了
				System.exit(1);
			}			
		}
		
		LogUtil.printLog("UpdateAllGameServerStartupScript completely|use|time|" + (System.currentTimeMillis() - timeStart));
	}
	
}
