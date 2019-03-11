package com.lizongbo.codegentool.world;

import java.util.Properties;

/**
 * 将构建好的文件放到版本服务机器
 * @author linyaoheng
 *
 */
public class ScpClientAppOnlyToReleaseVersionMajiabao {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		String apkPath = System.getenv("APKPATH");
		String tag = System.getenv("BUILD_TIMESTAMP");
		String majiabaoName = System.getenv("majiabaoName");
		
		if (worldName == null || worldName.isEmpty()
				|| majiabaoName == null || majiabaoName.isEmpty()){
			System.out.println("env|" + System.getenv());
			System.out.println("please set worldName");
			System.out.println("please set APKPATH");
			System.out.println("please set BUILD_TIMESTAMP");
			System.out.println("please set majiabaoName");
			System.exit(1);
		}
		
		Properties clientDeployProp = BilinGameWorldConfig.getClientBuildProp(worldName);
		
		//将客户端的真机包放到版本服务器(scpClientTagToReleaseVersion.pl)
		ScpCommandUtil.ScpClientFileToReleaseVersionServer(majiabaoName, clientDeployProp, apkPath, tag);
	}
	
}
