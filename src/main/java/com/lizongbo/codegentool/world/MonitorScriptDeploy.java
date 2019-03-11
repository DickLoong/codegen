package com.lizongbo.codegentool.world;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.csv2db.UploadAllPoolAndHosts;

/**
 * 发布监控脚本
 * @author linyaoheng
 */
public class MonitorScriptDeploy {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		pubMonitorScript(worldName);
	}
	
	/**
	 * 发布监控脚本
	 */
	public static void pubMonitorScript(String worldName) {
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("pubMonitorScript|worldName|" + worldName + "|Start");
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		//读取本地发布用的配置文件
		LogUtil.printLog("downloadTserverCSV starting");
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		BilinGameWorldConfig.downloadTserverCSV(worldName, deployProp);
		
		List<ServerDeployConfig> allDeployConfigs = BilinGameWorldConfig.getAllDeployConfigs(worldName);
		
		//将目录复制到/tmp/worldName/目录下
		String monitorNewDir = "/tmp/" + worldName + "/gecaoshoulie_monitor";
		new File(monitorNewDir).mkdirs();
		
		try {
			ServerContainerGenTool.copyDir(new File("/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_monitor"), new File(monitorNewDir));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		LogUtil.printLog("downloadTserverCSV completely");
		
		LogUtil.printLog("gen ps_monitor.conf starting");
		BilinGameWorldFileUtil.genPsMonitorConf(worldName);
		
		//删除.svn等目录
		ServerContainerGenTool.delAllFile4Need(monitorNewDir);
		
		String tmpZipFile = monitorNewDir + ".zip";
		ServerContainerGenTool.zipDir(monitorNewDir, tmpZipFile, ".");
		LogUtil.printLog("gen ps_monitor.conf completely");
		
		LogUtil.printLog("deploy ps_monitor.conf starting");
		//复制文件到版本服务器
		DeployCommandUtil.CreateVersionServerWorldTmpRoot(worldName);
		
		ScpCommandUtil.scpWorldFileToVersionServerTmp(worldName, tmpZipFile, "/" + new File(tmpZipFile).getName());
		
		//解压.zip文件
		DeployCommandUtil.UnzipVersionServerFile(worldName, 
				BilinGameWorldConfig.getWorldTmpRoot(worldName) + "/" + new File(tmpZipFile).getName(), 
				BilinGameWorldConfig.softwareRoot + "/" + new File(monitorNewDir).getName());
		
		// ip => crontab.conf
		TreeMap<String, String> serverMap = new TreeMap<>(); 
		
		//同时发布到备份机器,因为要用到统计脚本
		serverMap.put(BilinGameWorldConfig.getStatServerHost(worldName), null);
				
		//登录到所有机器从版本服务器SCP文件
		for (ServerDeployConfig item : allDeployConfigs){
			serverMap.put(item.server_public_ip, "game_server_crontab.conf");
		}
		
		//支付服务器
		List<String> allHttpHost = BilinGameWorldConfig.getPayServerHost(worldName);
		for (String httpHost : allHttpHost){
			serverMap.put(httpHost, "game_server_crontab.conf");
		}
		
		//ROOTDB
		String rootDBPubHost = BilinGameWorldConfig.getRootDBPubHost(worldName);
		if (rootDBPubHost != null && rootDBPubHost.length() > 0){
			serverMap.put(rootDBPubHost, "game_server_crontab.conf");
		}
		
		//同时发布到版本服务器(备份中心)
		serverMap.put(BilinGameWorldConfig.getVersionServerPublicIP(worldName), "backup_center_crontab.conf");
		
		for (Entry<String, String> item : serverMap.entrySet()){
			Thread t = new Thread(new Runnable() {
				public void run() {
					pubToServer(worldName, item.getKey(), item.getValue());
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
		
		LogUtil.printLog("deploy ps_monitor.conf completely");
		
		LogUtil.printLog("GenAndUploadServerConfigWithHosts starting");
		UploadAllPoolAndHosts.GenAndUploadServerConfigWithHosts(worldName);
		LogUtil.printLog("GenAndUploadServerConfigWithHosts completely");
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("pubMonitorScript|worldName|" + worldName + "|end|use|times|" + (endTime - startTime) + "ms");
	}
	
	private static void pubToServer(String worldName, String host, String crontabFile){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		DeployCommandUtil.copyFileFromVersionServer(worldName, host, 
				BilinGameWorldConfig.softwareRoot + "/gecaoshoulie_monitor/*", 
				BilinGameWorldConfig.scriptRoot + "/");
		
		DeployCommandUtil.ScpWorldPropertiest(worldName, host);
		
		String loginUser = deployProp.getProperty("linuxUserName").trim();
		String loginPwd = deployProp.getProperty("linuxUserPwd").trim();
		
//		//安装依赖的所有模块
//		LinuxRemoteCommandUtil.runCmd(host, 22, loginUser,  loginPwd, 
//				"yes | perl " + BilinGameWorldConfig.scriptRoot + "/installModule.pl;");
		
		if (crontabFile != null && !crontabFile.isEmpty()){
			LinuxRemoteCommandUtil.runCmd(host, 22, loginUser,  loginPwd, 
					"crontab " + BilinGameWorldConfig.scriptRoot + "/crontab_conf/" + crontabFile);
		}
	}
	
}
