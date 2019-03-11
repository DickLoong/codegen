package com.lizongbo.codegentool;

import java.io.File;
import java.io.IOException;

import com.lizongbo.codegentool.csv2db.GameCSV2DB;
import com.lizongbo.codegentool.csv2db.I18NUtil;
import com.lizongbo.codegentool.csv2db.ServerDeployConfig;
import com.lizongbo.codegentool.tools.StringUtil;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;

/**
 * 构建游戏服务器
 * @author linyaoheng
 *
 */
public class ServerContainerGenToolV2 {

	public static void main(String[] args) {
		//构建到相应的服务器
		String worldName = System.getenv("worldName");
		int zoneId = StringUtil.toInt(System.getenv("zoneId"), 0);
		
		if (zoneId == 0){
			System.out.println("请在环境变量指定zoneId");
			System.exit(1);
		}
		
		//构建的环境
		genJavaServer(worldName, zoneId);
	}
	
	/**
	 * 构建游戏服务器
	 */
	private static void genJavaServer(String worldName, int zoneId) {
		long startTime = System.currentTimeMillis();
		
		System.out.println("构建游戏服务器|worldName|" + worldName + "|zoneId|" + zoneId + "|开始执行");
		
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
		
		String localAppDir = I18NUtil.gameServerSrcPath;
		
		if (!new File(localAppDir).isDirectory()) {
			return;
		}
		if (!new File(new File(localAppDir), "WEB-INF").isDirectory()) {
			return;
		}
		
		// 前期做成传本地目录的，后面做成直接拉svn的代码来发布
		String appDirName = new File(localAppDir).getName();

		String resinNewDir = I18NUtil.getGameServerbuildPath(worldName);

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
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/lib");
		
		//删除构建包不需要包含的文件
		I18NUtil.delNoNeedDir(resinNewDir);
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/pojoclasses");
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/build.xml");
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/buildPojo.xml");
		ServerContainerGenTool.delAllFile(resinNewDir + "/WEB-INF/buildPojoV2.xml");
		
		ServerContainerGenTool.zipDir(resinNewDir, tmpZipFile, new File(resinRemoteDir).getName());
		
		long endTime = System.currentTimeMillis();
		System.out.println("构建游戏服务器|worldName|" + worldName + "|zoneId|" + zoneId + "|耗时|" + (endTime - startTime) + "ms");
		
		System.out.println("构建游戏服务器|完成");
	}

}
