package com.lizongbo.codegentool.tools;

import java.io.File;
import java.util.Properties;

import com.lizongbo.codegentool.DeployUtil;
import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.SQLGen4WorldUtil;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;
import com.lizongbo.codegentool.world.DeployCommandUtil;
import com.lizongbo.codegentool.world.ScpCommandUtil;

/**
 * 导配置表
 * @author linyaoheng
 *
 */
public class GameZoneDbImportV2 {

	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 0);
		
		DeployUtil.ShowEnv();
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateZoneConfig(worldName, zoneId);
		
		DbImport(worldName, zoneId);
	}
	
	/**
	 * 导配置表
	 */
	private static void DbImport(String worldName, int zoneId){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("DbImport|worldName|" + worldName + "|zoneId|" + zoneId + "|start");
		
		//根据CSV生成相应的SQL
		SQLGen4WorldUtil.genWorldSql(worldName);
		
		String sqlFilesPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/sqlfiles";
		String sqlFilesZipFile = sqlFilesPath + ".zip";
		ServerContainerGenTool.zipDir(sqlFilesPath, sqlFilesZipFile, "sqlfiles");
		
		String remoteFile = "/tmp/" + new File(sqlFilesZipFile).getName();
		
		// 上传sql文件
		ScpCommandUtil.ScpGameDbSqlToTmp(worldName, sqlFilesZipFile, remoteFile);
		
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		LinuxRemoteCommandUtil.runCmd(BilinGameWorldConfig.getVersionServerPublicIP(worldName), 22, 
				deployProp.getProperty("releaseVersionUser"), deployProp.getProperty("releaseVersionPwd"),
				"unzip -q -u -o " + remoteFile + " -d /tmp");
		
		DeployCommandUtil.ImportGameDatabase(worldName, zoneId, "/tmp/sqlfiles/createdb4user_" + worldName + ".sql");
		DeployCommandUtil.ImportGameDatabase(worldName, zoneId, "/tmp/sqlfiles/dropandcreatedb_" + worldName + ".sql");
		DeployCommandUtil.ImportGameDatabase(worldName, zoneId, "/tmp/sqlfiles/insertdb_" + worldName + ".sql");
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("DbImport|worldName|" + worldName + "|zoneId|" + zoneId + "|usetime|" + (endTime - startTime));
	}
	
}
