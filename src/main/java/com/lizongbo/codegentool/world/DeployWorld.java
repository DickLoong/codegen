package com.lizongbo.codegentool.world;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.csv2db.SQLGen4WorldUtil;

/**
 * 发布世界
 * 包括以下操作 

2.	创建root db
3.	导root db tables
 * @author linyaoheng
 *
 */
public class DeployWorld {
	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		deployWorld(worldName);
	}
	
	/**
	 * 发布世界表
	 */
	public static void deployWorld(String worldName){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("DeployWorld|worldName|" + worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
				
		//生成sqlfiles
		genWorldSQLAndScpToVersionServer(worldName);
		
//		2.	创建root db
//		3.	导root db tables
		LogUtil.printLog("CreateAndImportRootDB rootDB starting");
		DeployCommandUtil.CreateAndImportRootDB(worldName);
		LogUtil.printLog("CreateAndImportRootDB rootDB completely");
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("DeployWorld|worldName|" + worldName + "|use|time|" + (endTime - startTime) + "ms");
	}
	
	/**
	 * 生成ROOT DB的SQL文件,并上传到版本服务器
	 */
	public static void genWorldSQLAndScpToVersionServer(String worldName){
		LogUtil.printLog("genWorldSQLAndScpToVersionServer sqlfiles starting");
		SQLGen4WorldUtil.createOnlyWorldSql(worldName);
		ScpCommandUtil.ScpCreateInsertWorldDbSql(worldName);
		LogUtil.printLog("genWorldSQLAndScpToVersionServer sqlfiles completely");
	}
}
