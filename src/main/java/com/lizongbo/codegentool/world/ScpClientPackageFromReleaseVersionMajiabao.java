package com.lizongbo.codegentool.world;

import java.io.File;
import java.util.Properties;

import com.lizongbo.codegentool.ServerContainerGenTool;

/**
 * 从版本服务机器拉取指定TAG的AB到打包时指定的目录
 *
 */
public class ScpClientPackageFromReleaseVersionMajiabao {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		String abPath = System.getenv("DEST_AB_PATH");
		String tag = System.getenv("BUILD_TIMESTAMP");
		String abLocalSavePath = System.getenv("AB_LOCAL_SAVE_PATH");
		String majiabaoName = System.getenv("majiabaoName");
		
		if (worldName == null || worldName.isEmpty()
				|| abLocalSavePath == null || abLocalSavePath.isEmpty())
		{
			System.out.println("env|" + System.getenv());
			System.out.println("please set worldName");
			System.out.println("please set DEST_AB_PATH");
			System.out.println("please set BUILD_TIMESTAMP");
			System.out.println("please set AB_LOCAL_SAVE_PATH");
			System.out.println("please set majiabaoName");
			System.exit(1);
		}
		
		Properties clientDeployProp = BilinGameWorldConfig.getClientBuildProp(worldName);
		
		new File(abLocalSavePath).getParentFile().mkdirs();
		ScpCommandUtil.ScpClientFileFromReleaseVersionServer(majiabaoName, clientDeployProp, abLocalSavePath, tag);
		ServerContainerGenTool.delAllFile(abPath);
	}
	
}
