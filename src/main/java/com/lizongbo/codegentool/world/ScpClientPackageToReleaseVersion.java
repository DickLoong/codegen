package com.lizongbo.codegentool.world;

import java.io.File;
import java.util.Properties;

import com.lizongbo.codegentool.LogUtil;
import com.lizongbo.codegentool.ServerContainerGenTool;

/**
 * 将构建好的文件放到版本服务机器
	将AB压缩后放到版本服务器
	将.pb.bytes放到版本服务器
 * @author linyaoheng
 *
 */
public class ScpClientPackageToReleaseVersion {

	public static void main(String[] args) {
		String worldName = System.getenv("worldName");
		String abPath = System.getenv("DEST_AB_PATH");
		
		String pbBytesPath = System.getenv("PB_TYPES_PATH");
		String tag = System.getenv("BUILD_TIMESTAMP");
		String platform = System.getenv("platform");
		
		String apkPath = System.getenv("APKPATH");
		if ("iOS".equals(platform)){
			apkPath = System.getenv("APKPATH");
		}
		
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
		
		//创建目录
		DeployCommandUtil.CreateClientReleaseTagRoot(worldName, clientDeployProp, tag);
		
		//将AB压缩后放到版本服务器(scpClientTagToReleaseVersion.pl)
		if (new File(abPath).exists()){
			String abZipFile = "/tmp/" + worldName + "/AB_" + platform + ".zip";
			ServerContainerGenTool.zipDir(abPath, abZipFile, ".");
			ScpCommandUtil.ScpClientFileToReleaseVersionServer(worldName, clientDeployProp, abZipFile, tag);
		}
		else{
			LogUtil.printLog("path|does|not|exist|skip|abPath|" + abPath);
		}
		
		//将客户端的真机包放到版本服务器(scpClientTagToReleaseVersion.pl)
		ScpCommandUtil.ScpClientFileToReleaseVersionServer(worldName, clientDeployProp, apkPath, tag);
		
		//将.pb.bytes放到版本服务器(scpClientTagToReleaseVersion.pl)
		ScpCommandUtil.ScpClientFileToReleaseVersionServer(worldName, clientDeployProp, pbBytesPath, tag);
	}
	
}
