package com.lizongbo.codegentool.world;

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import com.jcraft.jsch.Session;
import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 更新代码文件
 * @author linyaoheng
 *
 */
public class UpdateClassFiles {
	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		String zoneIds = System.getenv("zoneId");
		String classFiles = System.getenv("classFiles");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		updateClassFiles(worldName, zoneIds, classFiles);
	}
	
	/**
	 * 导TServer表到RootDB
	 */
	private static void updateClassFiles(String worldName, String zoneIds, String classFiles){
		//校验所有的zoneId先
		String[] zoneArray = StringUtil.split(zoneIds, " ");
		if (zoneArray == null){
			LogUtil.printLogErr("please type the zoneIds");
			System.exit(1);
		}
		
		if (classFiles == null || classFiles.trim().length() == 0)
		{
			LogUtil.printLogErr("please type the classFiles");
			System.exit(1);
		}
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		String[] updateFiles = StringUtil.split(classFiles.replace("\t", ""), "\n");
		
		TreeMap<String, List<ServerDeployConfig>> serverHostsMap = BilinGameWorldConfig.getHostsMap(worldName, zoneArray); //同个物理机的用同一个线程来跑
		
		// 添加退出钩子,用于在程序退出时,在版本服务器中 1.添加reload文件; 2.删除skip_zoneId.conf
		DeployCommandUtil.AddHookRemoveSkipFileOnVersionServerOnAppExit(zoneArray, deployProp);
		
		// 在版本服务器添加skip_zoneId.conf文件,给监控中心跳过
		DeployCommandUtil.CreateSkipFileOnVersionServer(zoneArray, deployProp);
		
		for (Entry<String, List<ServerDeployConfig>> hostItem : serverHostsMap.entrySet()){
			List<ServerDeployConfig> tmpList = hostItem.getValue();
			String theIP = hostItem.getKey();
			
			Thread t = new Thread(new Runnable() {
				public void run() {
					Session sshSession = LinuxRemoteCommandUtil.getSSHSession(theIP, 22, deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"));
					
					long startTime = System.currentTimeMillis();
					
					String localPath = "/tmp/lyh_server_update_" + System.currentTimeMillis() + ".zip";
					String unzipToPath = "/tmp/lyh_server_update_" + System.currentTimeMillis();
					
					String remotePath = "/data/bilin/release_version/" + worldName + "/latest/gecaoshoulie_game_server_pub.zip";
					
					DeployCommandUtil.copyFileFromVersionServer(worldName, theIP, remotePath, localPath);
					
					// 每个IP复制一次文件并解压
					LinuxRemoteCommandUtil.runCmd(sshSession,
							"mkdir -p " + unzipToPath);
					
					LinuxRemoteCommandUtil.runCmd(sshSession,
							"unzip -q -u -o " + localPath + " -d " + unzipToPath);
					
					for (ServerDeployConfig item : tmpList){
						//复制各个文件
						String fileFullPathToPrefix = null;
						if (BilinGameWorldConfig.isGameServer(item.zone_id)){
							fileFullPathToPrefix = "/data/bilin/apps/" + item.zone_id + "/javaserver_gecaoshoulie_server";
						}
						else if (BilinGameWorldConfig.isWorldServer(item.zone_id)){
							fileFullPathToPrefix = "/data/bilin/apps/javaserver_world_server/" + item.zone_id + "/javaserver_gecaoshoulie_server";
						}
						else if (BilinGameWorldConfig.isCommonServer(item.zone_id))
						{
							fileFullPathToPrefix = "/data/bilin/apps/javaserver_common_server/" + item.zone_id;
						}
						
						for (String f : updateFiles){
							String fileFullPathFrom = unzipToPath + "/WEB-INF/classes/" + f;
							
							if (fileFullPathToPrefix != null)
							{
								LinuxRemoteCommandUtil.runCmd(sshSession,
										"cp '" + fileFullPathFrom + "' '" + fileFullPathToPrefix + "/WEB-INF/classes/" + f + "'");
							}
						}
						
						LinuxRemoteCommandUtil.runCmd(sshSession,
								fileFullPathToPrefix + "/bin/bl_start.sh");
					}
					
					LinuxRemoteCommandUtil.runCmd(sshSession,
							"rm -rf " + unzipToPath);
					
					LinuxRemoteCommandUtil.runCmd(sshSession,
							"rm " + localPath);
					
					sshSession.disconnect();
					
					LogUtil.printLog("updateClassFiles|worldName|" + worldName + "|host|" + theIP + "|usedtime|" + (System.currentTimeMillis() - startTime));
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
	}
}
