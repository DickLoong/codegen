package com.lizongbo.codegentool.world;

import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 导配置表到RootDB
 * @author linyaoheng
 *
 */
public class ImportRootDB {
	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		String importFiles = System.getenv("importFiles");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		importFilesToGameDB(worldName, importFiles);
	}
	
	/**
	 * 导配置表到RootDB
	 */
	private static void importFilesToGameDB(String worldName, String importFiles){
		long startTime = System.currentTimeMillis();
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String[] sqlFiles = StringUtil.split(importFiles, " ");
		
		String remoteFile = BilinGameWorldConfig.getWorldReleaseVersionRoot(worldName) + "/worldSqlFile.zip";
		String toPath = "/tmp/worldSqlFile_jenkins_ImportRootDB/" + startTime;
		
		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"mkdir -p " + toPath);
		
		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"unzip -q -u -o " + remoteFile + " -d " + toPath);
		
		String mysqlHost = BilinGameWorldConfig.getRootDBHost(worldName);
		String mysqlPort = worldProp.getProperty("rootDBPort");
		String dbName = worldProp.getProperty("rootDBName");
		
		for (String f : sqlFiles){
			String sqlFileFullPath = toPath + "/insertsqls/" + f;
			
			DeployCommandUtil.ImportDatabase(worldName, mysqlHost, mysqlPort, dbName, sqlFileFullPath);
		}
		
		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"rm -rf " + toPath);
		
		LogUtil.printLog("importTServer|worldName|" + worldName + "|use|time|" + (System.currentTimeMillis() - startTime));
	}
}
