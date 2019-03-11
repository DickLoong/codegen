package com.lizongbo.codegentool.world;

import java.util.List;
import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;

/**
 * 支付服务器发布
 * @author linyaoheng
 */
public class PayServerDeploy {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		BilinGameWorldConfig.removeLocalWorldProperties(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
				
//		支付服务器发布
		LogUtil.printLog("pubPayServerWeb starting");
		pubPayServerWeb(worldName);
		LogUtil.printLog("pubPayServerWeb completely");
	}
	
	/**
	 * 支付服务器发布
	 */
	private static void pubPayServerWeb(String worldName) {
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("pubPayServerWeb|worldName|" + worldName + "|开始执行");
		
		//读取本地发布用的配置文件
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		
		List<String> allHttpHost = BilinGameWorldConfig.getPayServerHost(worldName);
		int httpPort = BilinGameWorldConfig.getPayServerPort(worldName);
		
		for (String httpHost : allHttpHost)
		{
			long hostStartTime = System.currentTimeMillis();
			
			//创建tmp目录
			LinuxRemoteCommandUtil.runCmd(httpHost, 22, 
					deployProp.getProperty("linuxUserName").trim(), deployProp.getProperty("linuxUserPwd").trim(),
					"mkdir -p " + BilinGameWorldConfig.getWorldTmpRoot(worldName));
			
			DeployCommandUtil.cpPayServerLib(worldName, httpHost);
			
			DeployCommandUtil.startResinServer(worldName, httpHost, httpPort);
			
			System.out.println("pubOperateWeb|worldName|" + worldName + "|host|" + httpHost + "|耗时|" + (System.currentTimeMillis() - hostStartTime));
		}
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("pubOperateWeb|worldName|" + worldName + "|All|耗时|" + (endTime - startTime) + "ms");
	}
	
}
