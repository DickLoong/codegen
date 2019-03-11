package com.lizongbo.codegentool.csv2db;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;
import com.lizongbo.codegentool.world.DeployCommandUtil;

/**
 * 生成所有的模板/Hosts文件,复制所有的模板/Hosts文件到所有server
 * @author linyaoheng
 *
 */
public class UploadAllPoolAndHosts {
	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		GenAndUploadServerConfigWithHosts(worldName);
	}
	
	/**
	 * 生成所有配置文件,hosts,poolcfgfiles,keywords,还有一个character.txt到所有服务器
	 * @param worldName
	 */
	public static void GenAndUploadServerConfigWithHosts(String worldName){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("GenAndUploadServerConfigWithHosts|PoolAndHosts|worldName|" + worldName + "|start");
		
		//更新Hosts文件
		HostsGen4WorldUtil.genWorldHosts(worldName);
		PoolCfgGen4WorldUtil.genWorldPoolCfg(worldName);
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("GenAndUploadServerConfigWithHosts|PoolAndHosts|worldName|" + worldName + "|use|time|" + (endTime - startTime));
		
		//压缩目录
		startTime = System.currentTimeMillis();
		LogUtil.printLog("Compress|PoolAndHosts|worldName|" + worldName + "|start");
				
		boolean willDeleKeywordsText = false; //如果是复制过去的,操作完毕要删除
		String outputKeyworldsTextPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/keywords/keywords.txt";
		if (!new File(outputKeyworldsTextPath).exists()){
			String keywordsText = GenAll.readFile(I18NUtil.gameServerSrcPath + "/WEB-INF/src/keywords/keywords.txt", "UTF-8");
			GameCSV2DB.writeFile(outputKeyworldsTextPath, keywordsText);
			
			willDeleKeywordsText = true;
		}
		
		String keywordsPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/keywords";
		String keywordZipFile = I18NUtil.worldRootDir + "/" + worldName + "/forServer/keywords_" + worldName + ".zip";
		ServerContainerGenTool.zipDir(keywordsPath, keywordZipFile, "keywords");
		
		if (willDeleKeywordsText){
			if (new File(outputKeyworldsTextPath).exists()){
				new File(outputKeyworldsTextPath).delete();
			}
		}
		
		String poolcfgPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/poolcfgfiles";
		String poolcfgZipFile = I18NUtil.worldRootDir + "/" + worldName + "/forServer/poolcfgfiles_" + worldName + ".zip";
		ServerContainerGenTool.zipDir(poolcfgPath, poolcfgZipFile, "poolcfgfiles");
		
		endTime = System.currentTimeMillis();
		LogUtil.printLog("Compress|PoolAndHosts|worldName|" + worldName + "|usedtimes|" + (endTime - startTime));
		
		// ip => crontab.conf
		TreeMap<String, String> serverMap = new TreeMap<>();
		
		List<ServerDeployConfig> allGameServerList = BilinGameWorldConfig.getGameServerHosts(worldName);
		for (ServerDeployConfig deployConfig : allGameServerList){
			serverMap.put(deployConfig.server_public_ip, "1");
		}
		
		//公用服务器
		List<ServerDeployConfig> commonServerHosts = BilinGameWorldConfig.getCommonServerHosts(worldName);
		for (ServerDeployConfig item : commonServerHosts){
			serverMap.put(item.server_public_ip, "1");
		}
		
		List<ServerDeployConfig> allWorldServers = BilinGameWorldConfig.getWorldServerHost(worldName);
		for (ServerDeployConfig ws : allWorldServers){
			serverMap.put(ws.server_public_ip, "1");
		}
		
		//运营平台
		String operateServerHost = BilinGameWorldConfig.getOperateServerHost(worldName);
		serverMap.put(operateServerHost, "1");
		
		//统计平台
		String statServerHost = BilinGameWorldConfig.getStatServerHost(worldName);
		serverMap.put(statServerHost, "1");
		
		//版本服务器
		String versionServerHost = BilinGameWorldConfig.getVersionServerPublicIP(worldName);
		serverMap.put(versionServerHost, "1");
		
		//支付服务器
		List<String> allHttpHost = BilinGameWorldConfig.getPayServerHost(worldName);
		for (String httpHost : allHttpHost){
			serverMap.put(httpHost, "1");
		}
		
		//ROOTDB
		String rootDBPubHost = BilinGameWorldConfig.getRootDBPubHost(worldName);
		if (rootDBPubHost != null && rootDBPubHost.length() > 0){
			serverMap.put(rootDBPubHost, "1");
		}
		
		for (Entry<String, String> item : serverMap.entrySet()){
			Thread t = new Thread(new Runnable() {
				public void run() {
					DeployCommandUtil.updateServerHosts(worldName, item.getKey());
					DeployCommandUtil.updateOtherCfgFiles(worldName, item.getKey(), poolcfgZipFile, keywordZipFile);
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
		
		endTime = System.currentTimeMillis();
		LogUtil.printLog("GenAndUploadServerConfigWithHosts|PoolAndHosts|worldName|" + worldName + "|usedtimes|" + (endTime - startTime));
	}

}
