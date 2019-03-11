package com.lizongbo.codegentool.world;

import java.io.File;
import java.util.Properties;

import com.lizongbo.codegentool.LinuxRemoteCommandUtil;
import com.lizongbo.codegentool.LogUtil;

/**
 * 通过远程调用PERL来操作.
 */
public class RemotePerlCommandUtil {
	
	/**
	 * 暂时用于发布,所以这样写
	 * 2017-08-14 linyaoheng 世界服务器的帧同步服务器,也是用这个方式来发布
	 */
	public static void execPerlScript(String worldName, String cmdHost, String perlScriptName,
			int zoneId, String releaseTag){
		Properties deployProp = BilinGameWorldConfig.getGameEvnProp(worldName);
		Properties worldProp = BilinGameWorldConfig.downloadOrReadWorldProperties(worldName, deployProp);
		
		String releaseVersionUser = worldProp.getProperty("releaseVersionUser");
		String releaseVersionHost = worldProp.getProperty("releaseVersionHost");
		
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("execPerlScript|start|worldName|" + worldName + "|zoneId|" + zoneId + "|host|" + cmdHost);
		
		String loginUser = deployProp.getProperty("linuxUserName");
		String loginPwd = deployProp.getProperty("linuxUserPwd");
		
		String logFile = BilinGameWorldConfig.scriptRoot + "/forRemote/" + new File(perlScriptName).getName().replace(".pl", "") + ".log";
		
		LinuxRemoteCommandUtil.runCmd(cmdHost, 22, loginUser, loginPwd,
				"/usr/bin/perl " + BilinGameWorldConfig.scriptRoot + "/forRemote/" + perlScriptName 
				+ " " + worldName
				+ " " + zoneId
				+ " " + releaseTag
				+ " " + releaseVersionUser
				+ " " + releaseVersionHost + " >> " + logFile);
		
		LogUtil.printLog("execPerlScript|end|worldName|" + worldName + "|zoneId|" + zoneId + "|host|" + cmdHost + "|usedtime|" + (System.currentTimeMillis() - startTime));
	}
	
}
