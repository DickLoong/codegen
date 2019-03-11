package com.lizongbo.codegentool.world;

import java.io.File;

import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.SQLGen4WorldUtil;

/**
 * 功能是生成游戏服的SQL
 * 如果没有设置世界配置表的,则不上传到版本服务器
 * @author linyaoheng
 *
 */
public class GenTheZoneSQL {

	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		
		GenZoneSQL(worldName);
	}
	
	/**
	 * 生成游戏服的SQL
	 */
	private static void GenZoneSQL(String worldName){
		//根据CSV生成相应的SQL
		SQLGen4WorldUtil.genWorldSql(worldName);
		
		String sqlFilesPath = I18NUtil.worldRootDir + "/" + worldName + "/forServer/sqlfiles";
		String sqlFilesZipFile = sqlFilesPath + ".zip";
		ServerContainerGenTool.zipDir(sqlFilesPath, sqlFilesZipFile, "sqlfiles");
		
		// 尝试上传sql文件
		if (BilinGameWorldConfig.ExistsGameEnvProp(worldName)){
			ScpCommandUtil.scpServerReleaseFileToVersionServer(worldName, "latest", sqlFilesZipFile, new File(sqlFilesZipFile).getName());
		}
	}
	
}
