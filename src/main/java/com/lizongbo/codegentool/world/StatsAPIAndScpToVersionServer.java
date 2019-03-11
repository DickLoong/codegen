package com.lizongbo.codegentool.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 统计接口的调用
 * @author linyaoheng
 *
 */
public class StatsAPIAndScpToVersionServer {
	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		String zoneIds = System.getenv("zoneId");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		statAPITextAndScpToVersionServer(worldName, zoneIds);
	}
	
	private static void statAPITextAndScpToVersionServer(String worldName, String zoneIds){
		long startTime = System.currentTimeMillis();
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String linuxUserName = deployProp.getProperty("linuxUserName").trim();
		String linuxUserPwd = deployProp.getProperty("linuxUserPwd").trim();
		
		//校验所有的zoneId先
		String[] zoneArray = StringUtil.split(zoneIds, " ");
		if (zoneArray == null){
			LogUtil.printLogErr("please type the zoneIds");
			System.exit(1);
		}
		
		List<ServerDeployConfig> servers = new ArrayList<>();
		for (String item : zoneArray){
			ServerDeployConfig tmpDeployConfig = BilinGameWorldConfig.getDeployConfig(worldName, Integer.valueOf(item));
			if (tmpDeployConfig.zone_id <= 0){
				LogUtil.printLogErr("no|zoneId|found|" + item);
				System.exit(1);
			}
			
			servers.add(tmpDeployConfig);
		}
		
		int sshPort = LinuxRemoteCommandUtil.GetSSHPort();
		
		for (ServerDeployConfig dc : servers){
			if (BilinGameWorldConfig.isGameServer(dc.zone_id)){
				
				String[] dates = new String[]{
						"2017-10-06",
						"2017-10-07",
						"2017-10-08",
				};
				
				for (String tmpDate : dates){
					String outfile = "api_" + dc.zone_id + "_" + tmpDate.replace("-", "") + ".txt";
					
					String cmd = "zcat /data/bilin/backup_center/server_log/game_server/" 
							+ tmpDate + "/" + dc.zone_id + "/access_log.log." + tmpDate 
							+ "_* | awk -F \'|\'  \'{print $3 \" \" $5 \" " + dc.zone_id + " " + tmpDate + "\"}\' | sort | uniq -c"
							+ " > /tmp/" + outfile 
							+ " && scp -P " + sshPort +  " /tmp/" + outfile + " " + worldProp.getProperty("releaseVersionUser") + "@" + worldProp.getProperty("backupCenterHost") + ":/home/ubuntu/api_haomabao/";
					
					LinuxRemoteCommandUtil.runCmd(dc.server_public_ip, 22, linuxUserName, linuxUserPwd, cmd);
				}
			}
		}
		
		LogUtil.printLog("StatsAPIAndScpToVersionServer|worldName|" + worldName + "|use|time|" + (System.currentTimeMillis() - startTime));
	}
}
