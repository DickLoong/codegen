package com.lizongbo.codegentool.world;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;

/**
 * 支付服务器构建
 * @author linyaoheng
 */
public class PayServerReleaseVersion {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		genPayServerAndScpToVersionServer(worldName);
	}
	
	/**
	 * 生成支付服务器的文件,并上传到版本服务器
	 */
	private static void genPayServerAndScpToVersionServer(String worldName){
		//执行ant build
		String payServerBuildXmlPath = "/mgamedev/workspace/gecaoshoulie_proj/gecaoshoulie_pay_server/build.xml";
		ServerContainerGenTool.runShellCmd("/mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + payServerBuildXmlPath);
		
		LogUtil.printLog("genPayServerAndScpToVersionServer sqlfiles starting");
		ScpCommandUtil.ScpPayServerWarFile(worldName);
		LogUtil.printLog("genPayServerAndScpToVersionServer sqlfiles completely");
	}
	
}
