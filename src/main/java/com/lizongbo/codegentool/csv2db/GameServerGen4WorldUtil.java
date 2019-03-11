package com.lizongbo.codegentool.csv2db;

import java.io.File;

import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.I18NUtil.SERVER_TYP;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;

/**
 * 生成游戏服务器目录结构和启动/停止文件
 * @author linyaoheng
 *
 */
public class GameServerGen4WorldUtil {
	public static void main(String[] args) {
		genWorldGameServer("bilin_102", 40003);
	}

	/**
	 * 按世界读取世界目录下的csv生成对应的游戏服务器目录结构和文件
	 */
	public static void genWorldGameServer(String worldName, int zoneId) {
		if (!I18NUtil.worldExists(worldName)) {
			return;
		}
		
		String gameServerFilePath = I18NUtil.worldRootDir + "/" 
				+ worldName + "/forServer/serverconfs/" + zoneId + "/javaserver_gecaoshoulie_server";
		
		String mainClassName = "net.bilinkeji.gecaoshoulie.mgameprotorpc.apachemina.tcp.ProtobufRPCTCPServer";
		
		// 开始解析世界内的csv，生成对应的游戏服务器目录结构和文件
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String bl_startShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/bl_start.sh", "UTF-8");
		String bl_reinitShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/bl_reinit2.sh", "UTF-8");
		String killShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/kill.sh", "UTF-8");
		
		bl_startShText = ServerContainerGenTool.replaceConfigVar(bl_startShText, deployConfig, mainClassName,
				SERVER_TYP.GAME_SERVER);
		
		bl_reinitShText = ServerContainerGenTool.replaceConfigVar(bl_reinitShText, deployConfig, 
				"net.bilinkeji.gecaoshoulie.mgameprotorpc.apachemina.tcp.GameZoneReInit", 
				SERVER_TYP.GAME_SERVER);
		
		killShText = ServerContainerGenTool.replaceConfigVar(killShText, deployConfig, mainClassName,
				SERVER_TYP.GAME_SERVER);

		GameCSV2DB.writeFile(gameServerFilePath + "/bin/bl_start.sh", bl_startShText);
		GameCSV2DB.writeFile(gameServerFilePath + "/bin/bl_reinit.sh", bl_reinitShText);
		GameCSV2DB.writeFile(gameServerFilePath + "/bin/kill.sh", killShText);
		
		GameCSV2DB.createDir(gameServerFilePath + "/businesslog");
		GameCSV2DB.createDir(gameServerFilePath + "/conf");
		
		GameCSV2DB.copyFile(new File(ServerContainerGenTool.javaserverconftempdir + "/endorsed/jmxext_lizongbo.jar"), 
				new File(gameServerFilePath + "/endorsed/jmxext_lizongbo.jar"));
		
		GameCSV2DB.createDir(gameServerFilePath + "/log");
		GameCSV2DB.createDir(gameServerFilePath + "/WEB-INF");
	}
	
}
