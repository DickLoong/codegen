package com.lizongbo.codegentool;

import java.io.File;
import java.io.IOException;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;

/**
 * 构建场景服务器
 * @author linyaoheng
 *
 */
public class ServerContainerGenTool4MapServerV2 {
	
	public static void main(String[] args) {
		//发布到哪个环境去
		String worldName = System.getenv("worldName");
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 0);
		
		if (zoneId == 0){
			System.out.println("请在环境变量指定zoneId");
			System.exit(1);
		}
		
		genMapJavaServer(worldName, zoneId);
	}
	
	/**
	 * 构建场景服务器
	 */
	private static void genMapJavaServer(String worldName, int zoneId) {
		long startTime = System.currentTimeMillis();
		
		System.out.println("构建场景服务器|worldName|" + worldName + "|zoneId|" + zoneId + "|开始执行");
		
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		if (deployConfig.server_public_ip.isEmpty() || deployConfig.server_inner_ip.isEmpty()){
			System.out.println("zoneId=" + zoneId + "|没有指定IP");
			System.exit(1);
		}
				
		String serverPubIp = deployConfig.server_public_ip;
		int serverPort = deployConfig.game_server_port;
		
		if (serverPubIp == null || serverPubIp.length() < 4 || serverPort < 1024) {
			return;
		}
		
		// 构建到相应目录
		String localAppDir = I18NUtil.mapServerSrcPath;
				
		if (!new File(localAppDir).isDirectory()) {
			return;
		}
		if (!new File(new File(localAppDir), "WEB-INF").isDirectory()) {
			return;
		}
		
		// 前期做成传本地目录的，后面做成直接拉svn的代码来发布
		String appDirName = new File(localAppDir).getName();
		
		String resinNewDir = I18NUtil.getMapServerBuildPath(worldName);

		String resinRemoteDir = I18NUtil.getServerRemotePath(appDirName, zoneId);

		String tmpZipFile = resinNewDir + ".zip";
		
		System.out.println("try Copy!!!!!!!");

		try {
			ServerContainerGenTool.delAllFile(resinNewDir);
			ServerContainerGenTool.copyDir(new File(localAppDir), new File(resinNewDir));
			ServerContainerGenTool.copyDir(new File(ServerContainerGenTool.javaserverconftempdir), new File(resinNewDir));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Process process;
		try {
			System.out.println("try /mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + resinNewDir + "/WEB-INF/build.xml");

			ServerContainerGenTool
					.runShellCmd("/mgamedev/tools/apache-ant-1.9.6/bin/ant -f " + resinNewDir + "/WEB-INF/build.xml");

		} catch (Throwable e) {
			e.printStackTrace();
		}

		ServerContainerGenTool.delAllFile4Need(resinNewDir);
		
		//删除构建包不需要包含的文件
		I18NUtil.delNoNeedDir(resinNewDir);
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/build.xml");
		
		ServerContainerGenTool.zipDir(resinNewDir, tmpZipFile, new File(resinRemoteDir).getName());
		
		long endTime = System.currentTimeMillis();
		System.out.println("构建场景服务器|worldName|" + worldName + "|zoneId|" + zoneId + "|耗时|" + (endTime - startTime) + "ms");
		
		System.out.println("构建场景服务器|完成");
	}

}
