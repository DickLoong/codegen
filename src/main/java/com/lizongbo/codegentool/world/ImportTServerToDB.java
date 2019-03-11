package com.lizongbo.codegentool.world;

import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;

/**
 * 导TServer表到RootDB,只在TServer内容更改后才使用这个功能,使用此功能前,确保是已经生成了最新的SQL
 * @author linyaoheng
 *
 */
public class ImportTServerToDB {
	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		importTServer(worldName);
	}
	
	/**
	 * 导TServer表到RootDB
	 */
	private static void importTServer(String worldName){
		long startTime = System.currentTimeMillis();
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String remoteFile = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/worldSqlFile.zip";
		String toPath = "/tmp/worldSqlFile/" + startTime;
		
		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"mkdir -p " + toPath);
		
		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"unzip -q -u -o " + remoteFile + " -d " + toPath);
		
		String mysqlHost = BilinGameWorldConfig.getRootDBHost(worldName);
		String mysqlPort = worldProp.getProperty("rootDBPort");
		String dbName = worldProp.getProperty("rootDBName");
		
		DeployCommandUtil.ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, toPath + "/insertsqls/tserver_insert.sql");
		
		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"rm -rf " + toPath);
		
		LogUtil.printLog("importTServer|worldName|" + worldName + "|use|time|" + (System.currentTimeMillis() - startTime));
	}
}
