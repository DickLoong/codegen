package com.lizongbo.codegentool.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 导配置表到GameDB/WSDB
 * @author linyaoheng
 *
 */
public class ImportGameDB {
	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		String zoneIds = System.getenv("zoneId");
		String importFiles = System.getenv("importFiles");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		importFilesToGameDB(worldName, zoneIds, importFiles);
	}
	
	/**
	 * 导TServer表到RootDB
	 */
	private static void importFilesToGameDB(String worldName, String zoneIds, String importFiles){
		long startTime = System.currentTimeMillis();
		
		//校验所有的zoneId先
		String[] zoneArray = StringUtil.split(zoneIds, " ");
		if (zoneArray == null){
			LogUtil.printLogErr("please type the zoneIds");
			System.exit(1);
		}
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String[] sqlFiles = StringUtil.split(importFiles, " ");
		
		TreeMap<String, List<ServerDeployConfig>> serverHostsMap = new TreeMap<>(); //同个物理机的用同一个线程来跑
		
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
		
		String remoteFile = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/latest/sqlfiles.zip";
		String toPath = "/tmp/sqlfiles_jenkins_ImportGameDB/" + startTime;
		
		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"mkdir -p " + toPath);
		
		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"unzip -q -u -o " + remoteFile + " -d " + toPath);
		
		for (Entry<String, List<ServerDeployConfig>> hostItem : serverHostsMap.entrySet()){
			List<ServerDeployConfig> tmpList = hostItem.getValue();
			
			Thread t = new Thread(new Runnable() {
				public void run() {
					for (ServerDeployConfig item : tmpList){
						for (String f : sqlFiles){
							String sqlFileFullPath = toPath + "/sqlfiles/insertsqls/" + f;
							
							LogUtil.printLog("thread|" + Thread.currentThread().getId() + "|zoneId|" + item.zone_id);
							
							if (BilinGameWorldConfig.isGameServer(item.zone_id)){
								DeployCommandUtil.ImportGameDatabase(worldName, item.zone_id, sqlFileFullPath);
							}
							else if (BilinGameWorldConfig.isWorldServer(item.zone_id)){
								ServerDeployConfig worldServerConfig = BilinGameWorldConfig.getTheWorldServerConfig(worldName, item.zone_id);
								String tmpHost = BilinGameWorldConfig.getWorldServerDBHost(worldName, item.zone_id);
								String tmpPort = BilinGameWorldConfig.getWorldServerDBPort(worldName, item.zone_id);
								
								DeployCommandUtil.ImportDatabase(worldName, tmpHost, tmpPort, worldServerConfig.getDbName(), sqlFileFullPath);
							}
						}
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
		
//		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
//				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
//				"rm -rf " + toPath);
		
		LogUtil.printLog("importTServer|worldName|" + worldName + "|use|time|" + (System.currentTimeMillis() - startTime));
	}
}
