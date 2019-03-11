package com.lizongbo.codegentool.world;

import com.lizongbo.codegentool.LogUtil;

/**
 * 创建世界
 * 包括以下操作 

1.	todo装MySQL, 
2.	创建root db
3.	导root db tables

 * @author linyaoheng
 *
 */
public class WorldCreate {

	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		worldCreate(worldName);
	}
	
	/**
	 * 创建世界
	 */
	private static void worldCreate(String worldName){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("worldCreate|worldName|" + worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		//生成sqlfiles
		DeployWorld.genWorldSQLAndScpToVersionServer(worldName);
				
//		1.	todo装MySQL, 
//		2.	创建root db
//		3.	导root db tables
		
		LogUtil.printLog("CreateAndImportRootDB root starting");
		DeployCommandUtil.CreateAndImportRootDB(worldName);
		LogUtil.printLog("CreateAndImportRootDB root completely");
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("worldCreate|worldName|" + worldName + "|usedtime|" + (endTime - startTime) + " ms");
	}
	
}
