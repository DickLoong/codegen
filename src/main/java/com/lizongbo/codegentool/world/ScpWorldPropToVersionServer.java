package com.lizongbo.codegentool.world;

import java.io.File;
import java.io.IOException;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.I18NUtil;

/**
 * 将本地生成的的World.properties文件放到版本服务机器,用于创建世界之前
 * @author linyaoheng
 *
 */
public class ScpWorldPropToVersionServer {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		
		createVersionServerSoftwareFiles();
		
		ScpCommandUtil.ScpWorldPropToVersionServer(worldName);
		
		BilinGameWorldConfig.validateWorldConfig(worldName);
	}
	
	private static void createVersionServerSoftwareFiles(){
		long startTime = System.currentTimeMillis();
		
		LogUtil.printLog("createVersionServerSoftwareFiles|starting");
		
		String localAppDir = I18NUtil.versionServerSoftwareFilesPath;
		
		if (!new File(localAppDir).isDirectory()) {
			return;
		}
		
		String tmpDir = "/tmp/" + new File(localAppDir).getName();

		String tmpZipFile = tmpDir + ".zip";

		LogUtil.printLog("try Copy!!!!!!!");

		try {
			ServerContainerGenTool.delAllFile(tmpDir);
			ServerContainerGenTool.copyDir(new File(localAppDir), new File(tmpDir));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		ServerContainerGenTool.delAllFile4Need(tmpDir);
		I18NUtil.delNoNeedDir(tmpDir);
		
		ServerContainerGenTool.zipDir(tmpDir, tmpZipFile, ".");
		
		LogUtil.printLog("tmpZipFile|" + tmpZipFile);
		
		long endTime = System.currentTimeMillis();
		LogUtil.printLog("createVersionServerSoftwareFiles|usedtime|" + (endTime - startTime) + "ms");
	}
	
}
