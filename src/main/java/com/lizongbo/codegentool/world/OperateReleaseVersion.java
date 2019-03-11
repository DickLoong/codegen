package com.lizongbo.codegentool.world;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.SQLGen4WorldUtil;

/**
 * 构建运营平台
 * @author linyaoheng
 */
public class OperateReleaseVersion {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		genOperateSQLAndScpToVersionServer(worldName);
	}
	
	/**
	 * 生成运营 DB的SQL文件,并上传到版本服务器
	 */
	public static void genOperateSQLAndScpToVersionServer(String worldName){
		//执行ant build
		String operateBuildXmlPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_operateconsole/build.xml";
		ServerContainerGenTool.runShellCmd("/mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + operateBuildXmlPath);
		
		LogUtil.printLog("genOperateSQLAndScpToVersionServer sqlfiles starting");
		SQLGen4WorldUtil.createOperateDBSql(worldName);
		ScpCommandUtil.ScpCreateInsertOperateDbSql(worldName);
		LogUtil.printLog("genOperateSQLAndScpToVersionServer sqlfiles completely");
	}
	
}
