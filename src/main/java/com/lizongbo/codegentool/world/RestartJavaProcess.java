package com.lizongbo.codegentool.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import com.jcraft.jsch.Session;
import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;
import com.lizongbo.codegentool.world.porter.WorldServerPorter;

/**
 * Jekins来重启各个CS,GS,WS,同时重启帧同步,场景服务器的,但不包括REDIS
 * @author linyaoheng
 *
 */
public class RestartJavaProcess {
	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		String zoneIds = System.getenv("zoneId");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		restartJavaProcess(worldName, zoneIds);
	}
	
	/**
	 * Jekins来重启各个CS,GS,WS
	 */
	private static void restartJavaProcess(String worldName, String zoneIds){
		long startTime = System.currentTimeMillis();
		
		//校验所有的zoneId先
		String[] zoneArray = StringUtil.split(zoneIds, " ");
		if (zoneArray == null){
			LogUtil.printLogErr("please type the zoneIds");
			System.exit(1);
		}
		
		//读取配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		String linuxUserName = deployProp.getProperty("linuxUserName").trim();
		String linuxUserPwd = deployProp.getProperty("linuxUserPwd").trim();
		
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
		
		// 添加退出钩子,用于在程序退出时,在版本服务器中 1.添加reload文件; 2.删除skip_zoneId.conf
		DeployCommandUtil.AddHookRemoveSkipFileOnVersionServerOnAppExit(zoneArray, deployProp);
		
		// 在版本服务器添加skip_zoneId.conf文件,给监控中心跳过
		DeployCommandUtil.CreateSkipFileOnVersionServer(zoneArray, deployProp);
		
		for (Entry<String, List<ServerDeployConfig>> hostItem : serverHostsMap.entrySet()){
			List<ServerDeployConfig> tmpList = hostItem.getValue();
			
			Thread t = new Thread(new Runnable() {
				public void run() {
					Session sshSession = LinuxRemoteCommandUtil.getSSHSession(hostItem.getKey(), 22, linuxUserName, linuxUserPwd);
					
					for (ServerDeployConfig dc : tmpList){
						if (BilinGameWorldConfig.isGameServer(dc.zone_id))
						{
							String[] binPaths = new String[]{
								"javaserver_gecaoshoulie_framesync_server1",
								"javaserver_gecaoshoulie_framesync_server2",
								"javaserver_gecaoshoulie_framesync_server3",
								"javaserver_gecaoshoulie_framesync_server4",
								
								"javaserver_gecaoshoulie_map_server",
								"javaserver_gecaoshoulie_server",
							};
							
							for (String item : binPaths){
								LinuxRemoteCommandUtil.runCmd(sshSession,
										BilinGameWorldConfig.getZoneAppRoot(dc.zone_id) + "/" + item + "/bin/kill.sh");
							}
							
							int frameServerCount = BilinGameWorldConfig.getFrameServerCountByZoneId(worldName, dc.zone_id);
							
							List<String> binPathList = new ArrayList<>();
							binPathList.add("javaserver_gecaoshoulie_framesync_server1");
							
							if (frameServerCount >=2)
							{
								binPathList.add("javaserver_gecaoshoulie_framesync_server2");
							}
							
							if (frameServerCount >=3)
							{
								binPathList.add("javaserver_gecaoshoulie_framesync_server3");
							}
							
							if (frameServerCount >=4)
							{
								binPathList.add("javaserver_gecaoshoulie_framesync_server4");
							}
							
							binPathList.add("javaserver_gecaoshoulie_map_server");
							binPathList.add("javaserver_gecaoshoulie_server");
							
							for (String item : binPathList){
								LinuxRemoteCommandUtil.runCmd(sshSession,
										BilinGameWorldConfig.getZoneAppRoot(dc.zone_id) + "/" + item + "/bin/bl_start.sh");
							}
						}
						else if (BilinGameWorldConfig.isWorldServer(dc.zone_id))
						{
							WorldServerPorter worldServerPorter = new WorldServerPorter(worldName, dc);
							
							String[] binPaths = new String[]{
								"javaserver_gecaoshoulie_framesync_server1",
								"javaserver_gecaoshoulie_framesync_server2",
								"javaserver_gecaoshoulie_framesync_server3",
								"javaserver_gecaoshoulie_framesync_server4",
								
								"javaserver_gecaoshoulie_server",
							};
							
							for (String item : binPaths){
								LinuxRemoteCommandUtil.runCmd(sshSession,
										worldServerPorter.GetAppRemoteRoot() + "/" + item + "/bin/kill.sh");
							}
							
							int frameServerCount = BilinGameWorldConfig.getFrameServerCountByZoneId(worldName, dc.zone_id);
							
							List<String> binPathList = new ArrayList<>();
							binPathList.add("javaserver_gecaoshoulie_framesync_server1");
							
							if (frameServerCount >= 2)
							{
								binPathList.add("javaserver_gecaoshoulie_framesync_server2");
							}
							
							if (frameServerCount >= 3)
							{
								binPathList.add("javaserver_gecaoshoulie_framesync_server3");
							}
							
							if (frameServerCount >= 4)
							{
								binPathList.add("javaserver_gecaoshoulie_framesync_server4");
							}
							
							binPathList.add("javaserver_gecaoshoulie_server");
							for (String item : binPathList){
								LinuxRemoteCommandUtil.runCmd(sshSession,
										worldServerPorter.GetAppRemoteRoot() + "/" + item + "/bin/bl_start.sh");
							}
						}
						else if (BilinGameWorldConfig.isCommonServer(dc.zone_id))
						{
							LinuxRemoteCommandUtil.runCmd(sshSession,
									BilinGameWorldConfig.getCommonServerRoot(dc.zone_id) + "/bin/bl_start.sh");
						}
					}
					
					sshSession.disconnect();
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
		
		LogUtil.printLog("restartJavaProcess|worldName|" + worldName + "|use|time|" + (System.currentTimeMillis() - startTime));
	}
}
