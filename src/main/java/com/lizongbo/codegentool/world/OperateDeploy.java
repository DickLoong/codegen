package com.lizongbo.codegentool.world;

import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.tools.StringUtil;

/**
 * 发布运营平台
 * @author linyaoheng
 */
public class OperateDeploy {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
		
		LogUtil.printLog("CreateAndImportOperateDB operate starting");
		DeployCommandUtil.CreateAndImportOperateDB(worldName);
		LogUtil.printLog("CreateAndImportOperateDB operate completely");
		
		//创建统计DB
		DeployCommandUtil.CreateReportDatabase(worldName);
		
//		11.	发布运营平台
		LogUtil.printLog("pubOperateWeb starting");
		pubOperateWeb(worldName);
		LogUtil.printLog("pubOperateWeb completely");
	}
	
	/**
	 * 发布运营平台
	 */
	private static void pubOperateWeb(String worldName) {
		long startTime = System.currentTimeMillis();
		
		System.out.println("pubOperateWeb|worldName|" + worldName + "|开始执行");
		
		//读取本地发布用的配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		int httpPort = StringUtil.toInt(worldProp.getProperty("operateServerPort"));
		
		//创建tmp目录
		LinuxRemoteCommandUtil.runCmd(worldProp.getProperty("operateServerHost"), 22, 
				deployProp.getProperty("linuxUserName").trim(), deployProp.getProperty("linuxUserPwd").trim(),
				"mkdir -p " + BilinGameWorldConfig.getWorldTmpRoot(worldName));
		
		DeployCommandUtil.cpOperateLib(worldName);
		
		DeployCommandUtil.startResinServer(worldName, worldProp.getProperty("operateServerHost"), httpPort);
		
		long endTime = System.currentTimeMillis();
		System.out.println("pubOperateWeb|worldName|" + worldName + "|耗时|" + (endTime - startTime) + "ms");
	}
	
}
