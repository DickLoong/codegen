package com.lizongbo.codegentool.csv2db;

import java.io.File;

import com.lizongbo.codegentool.ServerContainerGenTool;
import com.lizongbo.codegentool.csv2db.I18NUtil.SERVER_TYP;
import com.lizongbo.codegentool.db2java.GenAll;
import com.lizongbo.codegentool.world.BilinGameWorldConfig;

/**
 * 生成场景服务器目录结构和启动/停止文件
 * @author linyaoheng
 *
 */
public class MapServerGen4WorldUtil {
	public static void main(String[] args) {
		//genWorldMapServer("bilin_102", 40003);
	}

	/**
	 * 按世界读取世界目录下的csv生成对应的场景服务器目录结构和文件
	 */
	public static void genWorldMapServer(String worldName, int zoneId) {
		if (!I18NUtil.worldExists(worldName)) {
			return;
		}
		
		String mapServerFilePath = I18NUtil.worldRootDir + "/" 
				+ worldName + "/forServer/serverconfs/" + zoneId + "/javaserver_gecaoshoulie_map_server";
		
		String mainClassName = "net.bilinkeji.gecaoshoulie.mapsync.server.MapTCPServer";
		
		// 开始解析世界内的csv，生成对应的场景服务器目录结构和文件
		ServerDeployConfig deployConfig = BilinGameWorldConfig.getDeployConfig(worldName, zoneId);
		
		String bl_startShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/bl_start.sh", "UTF-8");
		String killShText = GenAll.readFile(ServerContainerGenTool.javaserverconftempdir + "/bin/kill.sh", "UTF-8");

		bl_startShText = ServerContainerGenTool.replaceConfigVar(bl_startShText, deployConfig, mainClassName,
				SERVER_TYP.MAP_SERVER);
		
		killShText = ServerContainerGenTool.replaceConfigVar(killShText, deployConfig, mainClassName,
				SERVER_TYP.MAP_SERVER);

		GameCSV2DB.writeFile(mapServerFilePath + "/bin/bl_start.sh", bl_startShText);
		GameCSV2DB.writeFile(mapServerFilePath + "/bin/kill.sh", killShText);
		
		GameCSV2DB.createDir(mapServerFilePath + "/businesslog");
		GameCSV2DB.createDir(mapServerFilePath + "/conf");
		
		GameCSV2DB.copyFile(new File(ServerContainerGenTool.javaserverconftempdir + "/endorsed/jmxext_lizongbo.jar"), 
				new File(mapServerFilePath + "/endorsed/jmxext_lizongbo.jar"));
		
		GameCSV2DB.createDir(mapServerFilePath + "/log");
		GameCSV2DB.createDir(mapServerFilePath + "/WEB-INF");
	}
		
}
