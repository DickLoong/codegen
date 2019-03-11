package com.lizongbo.codegentool.world;

import java.io.File;
import java.util.Properties;

import com.lizongbo.codegentool.ServerContainerGenTool;

/**
 * 从版本服务机器拉取指定TAG的AB到打包时指定的目录
 *
 */
public class ScpClientPackageFromReleaseVersion {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		String abPath = System.getenv("DEST_AB_PATH");
		String tag = System.getenv("BUILD_TIMESTAMP");
		String platform = System.getenv("platform");
		
		if (worldName == null || worldName.isEmpty()){
			System.out.println("env|" + System.getenv());
			System.out.println("please set worldName");
			System.out.println("please set DEST_AB_PATH");
			System.out.println("please set APKPATH");
			System.out.println("please set PB_TYPES_PATH");
			System.out.println("please set BUILD_TIMESTAMP");
			System.out.println("please set platform");
			System.exit(1);
		}
		
		Properties clientDeployProp = BilinGameWorldConfig.getClientBuildProp(worldName);
		
		//12.	将AB拉回本地解压后放到AB服务器(scpClientTagFromReleaseVersion.pl)
		new File("/tmp/" + worldName + "_FromVersionServer").mkdirs();
		String abZipFile = "/tmp/" + worldName + "_FromVersionServer/AB_" + platform + ".zip";
		ScpCommandUtil.ScpClientFileFromReleaseVersionServer(worldName, clientDeployProp, abZipFile, tag);
		ServerContainerGenTool.delAllFile(abPath);
	}
	
}
