package com.lizongbo.codegentool.world;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.world.porter.CommonServerPorter;

/**
 * 发布公共服务器
 * @author linyaoheng
 */
public class CommonServerDeploy {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		TreeMap<String, List<ServerDeployConfig>> serverHostsMap = new TreeMap<>(); //同个物理机的用同一个线程来跑
		
		List<ServerDeployConfig> commonServerHosts = BilinGameWorldConfig.getCommonServerHosts(worldName);
		
		for (ServerDeployConfig item : commonServerHosts){
			//数据校验
			BilinGameWorldConfig.validateZoneConfig(worldName, item);
			
			String tmpIP = item.server_public_ip;

			if (!serverHostsMap.containsKey(tmpIP)){
				serverHostsMap.put(tmpIP, new ArrayList<ServerDeployConfig>());
			}
			
			List<ServerDeployConfig> tmpList = serverHostsMap.get(tmpIP);
			tmpList.add(item);
		}
		
		for (Entry<String, List<ServerDeployConfig>> item : serverHostsMap.entrySet())
		{
			List<ServerDeployConfig> tmpList = item.getValue();
			
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					for (ServerDeployConfig cs : tmpList){
						new CommonServerPorter(worldName, cs).DeployVersion();
					}
				}
			});
			
			try {
				Thread.sleep(500);
				t.start();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
}
